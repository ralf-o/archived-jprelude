package org.jprelude.common.csv;

import java.io.IOException;
import java.util.Arrays;
import org.jprelude.common.io.TextWriter;
import org.jprelude.common.util.Seq;
import org.junit.Test;

public class CsvFormatTest {
    @Test
    public void testSomething() throws IOException  {
        final Seq<Integer> records = Seq.range(0, 10);
        
        CsvFormat.builder()
            .columns(
                "COLUMN-1",
                "COLUMN-2",
                "COLUMN-3"
            )
            .escape('\\')
            .quoteMode(CsvQuoteMode.NONE)
            .build()
            .forOutputTo(TextWriter.from(System.out))
            .apply(records.map(n -> 
                Arrays.asList("a" + n, "b\r\\\"" + n, "c" + n)));               
    }
}
