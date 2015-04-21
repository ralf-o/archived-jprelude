package org.jprelude.csv;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jprelude.core.io.PathLister;
import org.jprelude.core.io.TextReader;
import org.jprelude.core.io.TextWriter;
import org.jprelude.core.util.Seq;
import org.jprelude.csv.base.CsvFormat;
import org.jprelude.csv.base.CsvRecord;
import org.jprelude.csv.base.CsvValidationException;
import org.jprelude.csv.base.CsvValidator;

public class PriceFilesTransformer {
    private final Path inputFolder;
    private final String inputFilesPattern;
    private final Path outputFile;
    private final boolean failOnDataViolation;
    private final List<Visitor> visitors;
    
    public PriceFilesTransformer(
             final Path inputFolder,
             final String inputFilesPattern,
             final Path outputFile,
             final boolean failOnDataViolation,
             final Visitor... visitors) {
        
        Objects.requireNonNull(inputFolder);
        Objects.requireNonNull(inputFilesPattern);
        Objects.requireNonNull(outputFile);
        
        this.inputFolder = inputFolder;
        this.inputFilesPattern = inputFilesPattern;
        this.outputFile = outputFile;
        this.failOnDataViolation = failOnDataViolation;
        this.visitors = Arrays.asList(visitors);
    }
    
    public void run() {
        CsvFormat csvInputFormat = CsvFormat.builder()
            .columns("Artikelnummer", "Preis/Einheit")
            .delimiter(';')
            .autoTrim(true)
            .build();

        CsvFormat csvOutputFormat = CsvFormat.builder()
            .columns("ARTICLE_NO", "PRICE")
            .delimiter(',')
            .autoTrim(true)
            .build();

        CsvValidator inputValidator = CsvValidator.builder()
             .validateColumn(
                  "Artikelnummer",
                  "Must have a length of exactly 10 characters",
                  artNo -> artNo.hasLength(10))
             .validateColumn(
                  "Preis/Einheit",
                  "Must be a floating point number greater than 0",
                  price -> price.isFloat() && price.isGreater(0))
            .build();
    
        Seq<Path> inputFiles = PathLister.builder()
            .addFilter("glob:" + this.inputFilesPattern)
            .addFilter(Files::isRegularFile)
            .build()
            .list(this.inputFolder);
            //.sortedReverse(path -> path.getUri());

        Seq<CsvRecord> recs = inputFiles
            .peek(path -> visitors.forEach(
                    visitor -> visitor.onProcessingFile(path)))
            .flatMap(path -> 
                csvInputFormat.parse(TextReader.forFile(path)));
           // .unique(rec -> rec.get("Artikelnummer"));

        Seq<CsvRecord> validatedRecs = recs
                .filter(inputValidator.asFilter(
                        e -> {
                            this.visitors.forEach(
                               visitor -> visitor.onDataViolation(e));
    
                            if (this.failOnDataViolation) {
                                throw e;
                            }
                        }))
                .peek(rec -> this.visitors.forEach(
                        visitor -> visitor.onNextRecord(rec)));

        Seq<List<?>> outputRows = validatedRecs
            .map(rec -> 
                    Arrays.asList(rec.get("Artikelnummer"), rec.get("Preis/Einheit")));

        Seq<String> outputLines = csvOutputFormat.map(outputRows);

        this.visitors.forEach(Visitor::onStart);
        
        try {
            TextWriter.forFile(this.outputFile)
                    .writeLines(outputLines, csvOutputFormat.getRecordSeparator());
                
            this.visitors.forEach(Visitor::onSuccess);
        } catch (final Throwable t) {
            this.visitors.forEach(visitor -> visitor.onError(t));
        }
    }

    public static interface Visitor {
        void onStart();        
        void onProcessingFile(final Path path);
        void onNextRecord(final CsvRecord rec);
        void onDataViolation(final CsvValidationException e);
        void onSuccess();
        void onError(Throwable t);
    }
}
