package org.jprelude.common.csv;

import java.io.IOException;
import java.util.Arrays;
import org.jprelude.common.io.TextReader;
import org.jprelude.common.io.TextWriter;
import org.jprelude.common.util.Seq;
import org.junit.Test;

public class CsvFormatTest {
    //@Test
    public void testOutput() throws IOException  {
        final Seq<Integer> records = Seq.range(0, 10);
        
        CsvFormat.builder()
            .columns(
                "COLUMN-1",
                "COLUMN-2",
                "COLUMN-3"
            )
            .autoTrim(true)
            .escape(null)
            .quoteMode(CsvQuoteMode.ALL)
            .build()
            .forOutputTo(TextWriter.from(System.out))
            .apply(records.map(n -> 
                Arrays.asList("   a" + n, "b" + n, "c" + n)));               
    }
    
    @Test
    public void testInput() throws IOException  {
        final Seq<Integer> records = Seq.range(0, 10);
        
        final String csvData =
                "COL1,COL2,COL3\n"
                + "a1,b1,c1\n"
                + "a2,b2,c2\n"
                + "a3,b3,c3\n";
        
        CsvFormat.builder()
            .columns(
                "Col1",
                "Col2",
                "Col3"
            )
            .autoTrim(true)
            .escape(null)
            .quoteMode(CsvQuoteMode.ALL)
            .build()
            .forInputFrom(TextReader.from(csvData))
            .apply(rec -> rec.get("Col1") + "-" + rec.get("Col2") + "-" + rec.get("Col3"))
            .forEach(System.out::println);
    }    
}
