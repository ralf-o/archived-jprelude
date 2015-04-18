package org.jprelude.core.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface IOSupplier<T> {
    T get() throws IOException;
    
    default Supplier<T> unchecked() {
        return () -> {
            T ret;
            
            try {
                ret = IOSupplier.this.get();
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
            
            return ret;
        };
    }
    
    static <T> Supplier<T> unchecked(final IOSupplier<T> ioSupplier) {
        Objects.requireNonNull(ioSupplier);
        
        return ioSupplier.unchecked();
    }
}
