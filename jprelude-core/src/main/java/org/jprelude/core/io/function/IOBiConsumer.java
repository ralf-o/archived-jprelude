package org.jprelude.core.io.function;

import java.io.IOException;
import org.jprelude.core.util.function.CheckedBiConsumer;

@FunctionalInterface
public interface IOBiConsumer<T1, T2> extends CheckedBiConsumer<T1, T2> {
    @Override
    void accept(T1 v1, T2 v2) throws IOException;
}
