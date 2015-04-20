package org.jprelude.core.io;


import java.io.IOException;
import org.jprelude.core.util.Seq;
import org.jprelude.core.util.Try;
import org.junit.Test;

public class TextWriterTest {
    @Test
    public void testWritingToStdOut() throws IOException {
        final Seq<?> seq = Seq.range(1, 11).map(n -> "Line " + n);
        final TextWriter textWriter = TextWriter.forOutputStream(System.out);
        
        Try
          .tryToRun(() -> textWriter.writeLines(seq))
          .ifCertainErrorThrow(IOException.class)
          .ifErrorThrowUnchecked();
    }
}
