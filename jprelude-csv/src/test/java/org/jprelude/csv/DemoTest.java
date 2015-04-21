package org.jprelude.csv;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jprelude.csv.PriceFilesTransformer.Visitor;
import org.jprelude.csv.base.CsvRecord;
import org.jprelude.csv.base.CsvValidationException;
import org.junit.Test;

public class DemoTest {
    @Test 
    public void testPriceFilesTransformation() {
        final String inputFolder = "/home/kenny/tests/input/prices/";
        final String inputFilesPattern = "**/prices-????-??-??.csv";    
        final String outputFile = "/home/kenny/tests/output/prices.txt";
        
        Visitor visitor = new Visitor() {
            final PrintStream out = System.out;
            @Override
            public void onStart() {
                this.out.println();
                this.out.println("Price file transformation");
                this.out.println("-------------------------");
                this.out.println();
                this.out.print("Input folder: ");
                this.out.println(inputFolder);
                this.out.print("Input files pattern: ");
                this.out.println(inputFilesPattern);
                this.out.print("Output file: ");
                this.out.println(outputFile);
                this.out.println();
            }

            @Override
            public void onProcessingFile(Path path) {
                this.out.println("Processing file " + path.toUri() + " ...");
            }

            @Override
            public void onNextRecord(CsvRecord rec) {
            }

            @Override
            public void onDataViolation(final CsvValidationException e) {
                this.out.println();
                this.out.print("Invalid CSV record #" );
                this.out.print(e.getRecordIndex() + 1);
                this.out.print(", source: ");
                this.out.println(e.getSource());
                this.out.println();
                
                this.out.println("Violation details:");
                this.out.println();
                
                e.getViolations().stream().forEach(violation -> {
                    this.out.print("  - ");
                    this.out.println(violation);
                });
                
                this.out.println();
            }

            @Override
            public void onSuccess() {
                this.out.println();
                this.out.println("Success: The price files have been successfully transformed.");
            }

            @Override
            public void onError(final Throwable t) {
                this.out.println();
                t.printStackTrace(this.out);
                this.out.println();
                this.out.print("Error: The price files could not been transformed. ");
                this.out.print("Cause: ");
                this.out.println(t.getMessage());
            }
        };
        
        PriceFilesTransformer transformer = new PriceFilesTransformer(
                Paths.get(inputFolder),
                inputFilesPattern,
                Paths.get(outputFile),
                true,
                visitor);
        
        transformer.run();
    }
}
