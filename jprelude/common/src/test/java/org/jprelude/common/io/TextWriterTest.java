package org.jprelude.common.io;


import java.io.IOException;
import org.jprelude.common.util.Seq;
import org.junit.Test;

public class TextWriterTest {
    @Test
    public void testWritingToStdOut() throws IOException {
        final Seq<?> seq = Seq.range(1, 11).map(n -> "Line " + n);
        final TextWriter textWriter = TextWriter.from(System.out);
        textWriter.writeLines(seq);
    }
}
