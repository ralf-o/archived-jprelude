package org.jprelude.core.util.function;

import java.util.Objects;
import java.util.function.Supplier;

@FunctionalInterface
public interface CheckedSupplier<T, E extends Exception> {
    T get() throws E;
    
    default Supplier<T> unchecked() {
         return () -> {
            T ret;

            try {
                ret = CheckedSupplier.this.get();
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }

            return ret;
        };
    }
    
    static <T> Supplier<T> unchecked(final CheckedSupplier<T, ?> supplier) {
        Objects.requireNonNull(supplier);
        
        return supplier.unchecked();
    }
}
