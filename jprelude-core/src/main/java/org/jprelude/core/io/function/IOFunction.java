package org.jprelude.core.io.function;

import java.io.IOException;
import java.util.Objects;
import org.jprelude.core.util.function.CheckedFunction;
import org.jprelude.core.util.function.CheckedTriFunction;

@FunctionalInterface
public interface IOFunction<T, R> extends CheckedFunction<T, R> {
    @Override
    R apply(T t) throws IOException;    
}

