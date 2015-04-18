package org.jprelude.core.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface IOConsumer<T> {
    void accept(T t) throws IOException;
   
    default Consumer<T> unchecked() {
        return value -> {
            try {
                IOConsumer.this.accept(value);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    static <T> Consumer<T> unchecked(final IOConsumer<T> ioConsumer) {
        Objects.requireNonNull(ioConsumer);
        
        return ioConsumer.unchecked();
    }
}
