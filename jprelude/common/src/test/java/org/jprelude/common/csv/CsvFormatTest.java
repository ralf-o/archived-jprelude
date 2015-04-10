package org.jprelude.common.csv;

import org.jprelude.common.util.LineSeparator;
import java.io.IOException;
import java.util.Arrays;
import org.jprelude.common.io.TextReader;
import org.jprelude.common.io.TextWriter;
import org.jprelude.common.util.Seq;
import org.junit.Test;

public class CsvFormatTest {
    private enum Column {
        COLUMN1, COLUMN2, COLUMN3;
    }
    
    @Test
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

    @Test
    public void testOutput() throws IOException  {
        final Seq<Integer> records = Seq.range(0, 10);
        
        CsvFormat.builder()
            .columns(
                "COLUMN1",
                "COLUMN2",
                "COLUMN3"
            )
            .autoTrim(true)
            .escape(null)
            .recordSeparator(LineSeparator.SYSTEM)
            .quoteMode(CsvQuoteMode.ALL)
            .build()
            .prepareOutputTo(TextWriter.from(System.out))
            .apply(records.map(n -> 
                Arrays.asList("   a" + n, "b" + n, "c" + n)));               
    }
    
    @Test
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
            .parse(TextReader.from(csvData))   
            .forEach(rec -> System.out.println(rec.index() + ": " + rec.get(Column.COLUMN1) + "-" + rec.get(Column.COLUMN2) + "-" + rec.get(Column.COLUMN3)));
    }    
}
