package org.jprelude.core.util.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface CheckedConsumer<T> {
    void accept(T t) throws Exception;
   
    default Consumer<T> unchecked() {
        return value -> {
            try {
                CheckedConsumer.this.accept(value);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    default CheckedConsumer<T> andThen(final CheckedConsumer<? super T> after) {
        Objects.requireNonNull(after);
        
        return v -> {
            accept(v);
            after.accept(v);
        };
    }

    static <T> Consumer<T> unchecked(final CheckedConsumer<T> consumer) {
        Objects.requireNonNull(consumer);
        
        return consumer.unchecked();
    }
}
