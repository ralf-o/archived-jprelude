package org.jprelude.core.io.function;

import java.io.IOException;
import org.jprelude.core.util.function.CheckedBiFunction;

@FunctionalInterface
public interface IOBiFunction<T1, T2, R> extends CheckedBiFunction<T1, T2, R> {
    @Override
    R apply(T1 v1, T2 v2) throws IOException;
}
