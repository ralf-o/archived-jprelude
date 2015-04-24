package org.jprelude.core.util.function;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface CheckedBiConsumer<T1, T2> {
    void accept(T1 v1, T2 v2) throws Throwable;
   
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

    static <T1, T2> BiConsumer<T1, T2> unchecked(
            final CheckedBiConsumer<T1, T2> consumer) {
        
        Objects.requireNonNull(consumer);
        
        return consumer.unchecked();
    }
}
