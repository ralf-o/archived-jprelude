package org.jprelude.core.io;

import java.io.IOException;

public interface TextPrinter {
    void print(final Object item) throws IOException;
    void println(final Object item) throws IOException;
}
