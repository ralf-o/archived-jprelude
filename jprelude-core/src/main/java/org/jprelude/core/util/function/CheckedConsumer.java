package org.jprelude.core.util.function;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception> {
    void accept(T t) throws E;
   
    default Consumer<T> unchecked() {
        return value -> {
            try {
                CheckedConsumer.this.accept(value);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    default CheckedConsumer<T, E> andThen(final CheckedConsumer<? super T, ? extends E> after) {
        Objects.requireNonNull(after);
        
        return v -> {
            accept(v);
            after.accept(v);
        };
    }

    static <T> Consumer<T> unchecked(final CheckedConsumer<T, ?> consumer) {
        Objects.requireNonNull(consumer);
        
        return consumer.unchecked();
    }
}
