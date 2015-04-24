package org.jprelude.core.io.function;

import org.jprelude.core.util.function.CheckedTriConsumer;

@FunctionalInterface
public interface IOTriConsumer<T1, T2, T3> extends CheckedTriConsumer<T1, T2, T3> {
    @Override
    void accept(T1 v1, T2 v2, T3 v3);
}
