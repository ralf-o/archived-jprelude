package org.jprelude.core.util.function;

import java.util.Objects;

@FunctionalInterface
public interface TriConsumer<T1, T2, T3> {
    void accept(T1 v1, T2 v2, T3 v3);

    default TriConsumer<T1, T2, T3> andThen(final TriConsumer<? super T1, ? super T2, ? super T3> after) {
        Objects.requireNonNull(after);
        
        return (v1, v2, v3) -> {
            accept(v1, v2, v3);
            after.accept(v1, v2, v3);
        };
    }
}
