package org.jprelude.core.io.function;

import java.io.IOException;
import org.jprelude.core.util.function.CheckedFunction;

@FunctionalInterface
public interface IOFunction<T, R> extends CheckedFunction<T, R> {
    @Override
    R apply(T t) throws IOException;
}

