package org.jprelude.core.io.function;

import java.io.IOException;
import java.util.Objects;
import org.jprelude.core.util.function.CheckedBiConsumer;

@FunctionalInterface
public interface IOBiConsumer<T1, T2> extends CheckedBiConsumer<T1, T2> {
    @Override
    void accept(T1 v1, T2 v2) throws IOException;

    default IOBiConsumer<T1, T2> andThen(final IOBiConsumer<? super T1, ? super T2> after) {
        Objects.requireNonNull(after);
        
        return (v1, v2) -> {
            accept(v1, v2);
            after.accept(v1, v2);
        };
    }

}
