package org.jprelude.common.csv;

import java.io.IOException;
import java.util.Arrays;
import org.jprelude.common.io.TextWriter;
import org.jprelude.common.util.Seq;
import org.junit.Test;

public class CsvExportTest {
    @Test
    public void TestDodo() throws IOException {
        final Seq<Integer> records = Seq.range(0, 100);
        
        CsvExport.builder()
            .recordMultiMapper(n -> Seq.of(
                    Arrays.asList("a" + n, "b" + n, "c" + n),
                    Arrays.asList("A" + n, "B" + n, "C" + n)))
            .build()
            .write(records, TextWriter.from(System.out));
            //.map(records)
            //.forEach(System.out::println);
        }
}
