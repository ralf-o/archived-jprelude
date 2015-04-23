package org.jprelude.csv.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jprelude.core.io.PathLister;
import org.jprelude.core.io.TextReader;
import org.jprelude.core.io.TextWriter;
import org.jprelude.core.util.Seq;
import org.jprelude.core.util.SortDirection;
import org.junit.Test;

public class DemoTest {
    @Test 
    public void testPriceFilesTransformation() {
        final String inputFolder = "/home/kenny/tests/input/prices/";
        final String inputFilesPattern = "**/prices-????-??-??.csv";    
        final String outputFile = "/home/kenny/tests/output/prices.txt";
        
        final PriceFilesTransformer transformer = new PriceFilesTransformer(
                Paths.get(inputFolder),
                inputFilesPattern,
                TextWriter.forFile(Paths.get(outputFile)),
                //TextWriter.forOutputStream(System.out),
                
                //TextWriter.forFile(Paths.get("/dev/null")),
                
                false,
                System.out::println
                );
        
        transformer.run();
    }
 
    public static class PriceFilesTransformer {
        private final Path inputFolder;
        private final String inputFilesPattern;
        private final TextWriter target;
        private final boolean failOnDataViolation;
        private final List<Observer> observers;

        public PriceFilesTransformer(
                 final Path inputFolder,
                 final String inputFilesPattern,
                 final TextWriter target,
                 final boolean failOnDataViolation,
                 final Consumer<String> infoTextConsumer,
                 final Observer... observers) {

            Objects.requireNonNull(inputFolder);
            Objects.requireNonNull(inputFilesPattern);
            Objects.requireNonNull(target);

            this.inputFolder = inputFolder;
            this.inputFilesPattern = inputFilesPattern;
            this.target = target;
            this.failOnDataViolation = failOnDataViolation;

            this.observers = Seq
                    .from(observers)
                    .prepend(infoTextConsumer == null
                                ? null
                                : this.createInfoObserver(infoTextConsumer))
                    .rejectNulls()
                    .toList();
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

            CsvImporter<CsvRecord> importer = CsvImporter.<CsvRecord>builder()
                    .format(csvInputFormat)
                    .failOnValidationError(false)
                    .validator(inputValidator)
                    .mapper(rec -> rec)
                    .build();

            CsvExporter<CsvRecord> exporter = CsvExporter.<CsvRecord>builder()
                    .format(csvOutputFormat)
                    .mapper(rec -> Arrays.asList(
                            rec.get("Artikelnummer"),
                            rec.get("Preis/Einheit")))
                    .build();

            Seq<Path> inputFiles = PathLister.builder()
                    .addFilter("glob:" + this.inputFilesPattern)
                    .addFilter(Files::isRegularFile)
                    .build()
                    .list(this.inputFolder)
                    .forceOnDemand() // caches the path entries on first demand (not now!),
                                     // will not traverse the diretory a second time then
                    .sorted(Path::getFileName, SortDirection.DESCENDING);

            Seq<CsvRecord> recs = inputFiles
                .peek(path -> observers.forEach(
                        visitor -> visitor.onProcessingFile(path)))
                .flatMap(path -> importer.parse(TextReader.forFile(path)))
                .distinct(rec -> rec.get("Artikelnummer"));

            this.observers.forEach(Observer::onStart);

            exporter.tryToExport(recs, target)
                    .ifSuccess(() -> this.observers.forEach(obs -> obs.onSuccess(inputFiles)))
                    .ifError(error -> this.observers.forEach(obs -> obs.onError(error)));
        }

        public static interface Observer {
            void onStart();        
            void onProcessingFile(final Path path);
            void onNextRecord(final CsvRecord rec);
            void onDataViolation(final CsvValidationException e);
            void onSuccess(final Seq<Path> files);
            void onError(Throwable t);
        }

        private Observer createInfoObserver(final Consumer<String> println) {
            assert println != null;

            return new Observer() {
                @Override
                public void onStart() {
                    println.accept("");
                    println.accept("Price file transformation");
                    println.accept("-------------------------");
                    println.accept("");
                    println.accept("Input folder: " + PriceFilesTransformer.this.inputFolder);
                    println.accept("Input files pattern: " + PriceFilesTransformer.this.inputFilesPattern);
                    println.accept("Output file: " + PriceFilesTransformer.this.target);
                    println.accept("");
                }

                @Override
                public void onProcessingFile(Path path) {
                    println.accept("Processing  " + path.toUri() + " ...");
                }

                @Override
                public void onNextRecord(CsvRecord rec) {
                }

                @Override
                public void onDataViolation(final CsvValidationException e) {
                    println.accept("");

                    println.accept(
                            "Invalid CSV record #"
                            + (e.getRecordIndex() + 1)
                            + ", source: ");

                    println.accept(e.getSource());
                    println.accept("");
                    println.accept("Violation details:");
                    println.accept("");

                    e.getViolations().stream().forEach(violation -> {
                        println.accept("  - ");
                        println.accept(violation);
                    });

                    println.accept("");
                }

                @Override
                public void onSuccess(final Seq<Path> inputFiles) {
                    println.accept("");
                    
                    println.accept("Success: "
                                + inputFiles.length()
                                + " price file(s) have been transformed.");
                }

                @Override
                public void onError(final Throwable t) {
                    println.accept("");
                    
                    // Print stack trace
                    final Writer writer = new StringWriter();
                    final PrintWriter printWriter = new PrintWriter(writer);
                    t.printStackTrace(printWriter);
                    println.accept(writer.toString());
                    println.accept("");

                    println.accept(
                            "Error: The price files could not been transformed. "
                            + "Cause: "
                            + t.getMessage());

                    println.accept("");
                }
            };
        }
    }
}
