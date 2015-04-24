package org.jprelude.core.io.function;

import java.util.Objects;
import org.jprelude.core.util.function.CheckedTriConsumer;

@FunctionalInterface
public interface IOTriConsumer<T1, T2, T3> extends CheckedTriConsumer<T1, T2, T3> {
    @Override
    void accept(T1 v1, T2 v2, T3 v3);
    
    default IOTriConsumer<T1, T2, T3> andThen(final IOTriConsumer<? super T1, ? super T2, ? super T3> after) {
        Objects.requireNonNull(after);
        
        return (v1, v2, v3) -> {
            accept(v1, v2, v3);
            after.accept(v1, v2, v3);
        };
    }
}
