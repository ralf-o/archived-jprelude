package org.jprelude.core.util.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Supplier;

@FunctionalInterface
public interface CheckedSupplier<T> {
    T get() throws Throwable;
    
    default Supplier<T> unchecked() {
         return () -> {
            T ret;

            try {
                ret = CheckedSupplier.this.get();
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }

            return ret;
        };
    }
    
    static <T> Supplier<T> unchekched(final CheckedSupplier<T> supplier) {
        Objects.requireNonNull(supplier);
        
        return supplier.unchecked();
    }
}
