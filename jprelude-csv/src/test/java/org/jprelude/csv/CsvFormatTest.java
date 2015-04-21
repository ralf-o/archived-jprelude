package org.jprelude.csv;

import org.jprelude.csv.base.CsvQuoteMode;
import org.jprelude.csv.base.CsvFormat;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import org.jprelude.core.io.TextReader;
import org.jprelude.core.io.TextWriter;
import org.jprelude.core.util.LineSeparator;
import org.jprelude.core.util.Seq;
import org.jprelude.csv.base.CsvValidator;
import org.junit.Test;

public class CsvFormatTest {
    private enum Column {
        COLUMN1, COLUMN2, COLUMN3;
    }
    
    //@Test
    public void testApplyOn() throws IOException  {
        final Seq<Integer> records = Seq.range(0, 10);
        
        CsvFormat.builder()
            .columns(
                "COLUMN1",
                "COLUMN2",
                "COLUMN3"
            )
            .autoTrim(true)
            .escape(null)
            .quoteMode(CsvQuoteMode.ALL)
            .build()
            .map(records.map(n -> 
                Arrays.asList("==>   a" + n, "b" + n, "c" + n)))
            .forEach(System.out::println);
    }

    //@Test
    public void testOutputOfSeq() throws Throwable  {
        final Seq<Integer> records = Seq.range(0, 10);
        
        CsvFormat.builder()
            .columns(
                "COLUMN1y",
                "COLUMN2",
                "COLUMN3"
            )
            .autoTrim(true)
            .escape(null)
            .recordSeparator(LineSeparator.SYSTEM)
            .quoteMode(CsvQuoteMode.ALL)
            .build()
            .prepareExportTo(TextWriter.forFile(Paths.get("/home/kenny/test.juhu")))
            .apply(records.map(n -> 
                Arrays.asList("   a" + n, "b" + n, "c" + n)))
            .ifErrorFail()
            .ifSuccess(result -> System.out.println(String.format(">>>> Exported %d records successfully", result.getSourceRecordCount())));
    }

    
    //@Test
    public void testInput() throws IOException  {
        final Seq<Integer> records = Seq.range(0, 10);
        
        final String csvData =
                "COLUMN1,COLUMN2,COLUMN3\n"
                + "a1,b1,c1\n"
                + "a2,b2,c2\n"
                + "a3,b3,c3\n";
        
        CsvFormat.builder()
            .columns(
                "COLUMN2",
                "COLUMN1",
                "COLUMN3"
            )
            .autoTrim(true)
            .escape(null)
            .recordSeparator(LineSeparator.SYSTEM)
            .quoteMode(CsvQuoteMode.ALL)
            .build()
            .parse(TextReader.forString(csvData))
            .forEach(rec -> System.out.println(rec.getIndex() + ": " + rec.get(Column.COLUMN1) + "-" + rec.get(Column.COLUMN2) + "-" + rec.get(Column.COLUMN3)));
    }    


    @Test
    public void testInput2() throws IOException  {
        final Seq<Integer> records = Seq.range(0, 10);
        
        final String csvData =
                "ART_NO,PRICE\n"
                + "a123,10.99\n"
                + "b234,2d0.90\n"
                + "c345,30x50\n";
        
        CsvValidator validator = CsvValidator.builder()
                .checkColumn(
                        "ART_NO",
                        "Must have a length of 4",
                        artNo -> !artNo.isNull())
                .checkColumn(
                        "PRICE",
                        "Must be a positive floating number",
                        price -> price.isFloat())
                .build();
        
        CsvFormat.builder()
            .columns(
                "ART_NO",
                "PRICE"
            )
            .autoTrim(true)
            .escape(null)
            .recordSeparator(LineSeparator.SYSTEM)
            .quoteMode(CsvQuoteMode.ALL)
            .build()
            .parse(TextReader.forString(csvData))
            //.peek(validator.asConsumer())
            .filter(validator.asFilter())
             //.forEach(System.out::println);
            .forEach(rec -> System.out.println(rec.getIndex() + ": " + rec.get("ART_NO") + "-" + rec.get("PRICE") ));
    }    

}
