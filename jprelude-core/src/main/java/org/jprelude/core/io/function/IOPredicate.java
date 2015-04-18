
package org.jprelude.core.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface IOPredicate<T> {
    boolean test(T t) throws IOException;

    default Predicate<T> unchecked() {
        return value -> {
            boolean ret;
            
            try {
                ret = IOPredicate.this.test(value);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
            
            return ret;
        };
    }
    
    static <T> Predicate<T> unchecked(final IOPredicate<T> ioPredicate) {
        Objects.requireNonNull(ioPredicate);
        
        return ioPredicate.unchecked();
    }
}
