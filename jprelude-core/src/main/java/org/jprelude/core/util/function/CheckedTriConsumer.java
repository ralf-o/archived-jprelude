package org.jprelude.core.util.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedTriConsumer<T1, T2, T3, E extends Exception> {
    void accept(T1 v1, T2 v2, T3 v3) throws E;
   
    default TriConsumer<T1, T2, T3> unchecked() {
        return (v1, v2, v3) -> {
            try {
                CheckedTriConsumer.this.accept(v1, v2, v3);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    default CheckedTriConsumer<T1, T2, T3, E> andThen(final CheckedTriConsumer<? super T1, ? super T2, ? super T3, ? extends E> after) {
        Objects.requireNonNull(after);
        
        return (v1, v2,v3) -> {
            accept(v1, v2, v3);
            after.accept(v1, v2, v3);
        };
    }

    static <T1, T2, T3> TriConsumer<T1, T2, T3> unchecked(
            final CheckedTriConsumer<T1, T2, T3, ?> consumer) {
        
        Objects.requireNonNull(consumer);
        
        return consumer.unchecked();
    }
}
