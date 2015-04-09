package org.jprelude.common.io.function;

import java.io.IOException;

public interface IOFunction<T, R> {
    R apply(T t) throws IOException;
}

