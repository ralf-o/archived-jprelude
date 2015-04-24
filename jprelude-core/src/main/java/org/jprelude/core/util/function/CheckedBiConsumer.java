package org.jprelude.core.util.function;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface CheckedBiConsumer<T1, T2, E extends Exception> {
    void accept(T1 v1, T2 v2) throws E;
   
    default BiConsumer<T1, T2> unchecked() {
        return (v1, v2) -> {
            try {
                CheckedBiConsumer.this.accept(v1, v2);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    default CheckedBiConsumer<T1, T2, E> andThen(final CheckedBiConsumer<? super T1, ? super T2, E> after) {
        Objects.requireNonNull(after);
        
        return (v1, v2) -> {
            accept(v1, v2);
            after.accept(v1, v2);
        };
    }

    static <T1, T2, E extends Exception> BiConsumer<T1, T2> unchecked(
            final CheckedBiConsumer<T1, T2, E> consumer) {
        
        Objects.requireNonNull(consumer);
        
        return consumer.unchecked();
    }
}
