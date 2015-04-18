package org.jprelude.core.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IOFunction<T, R> {
    R apply(T t) throws IOException;
    
    default Function<T, R> unchecked() {
        return value -> {
            R ret;
            
            try {
                ret = IOFunction.this.apply(value);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
            
            return ret;
        };
    }
    
    static <T, R> Function<T, R> unchecked(final IOFunction<T, R> ioFunction) {
        Objects.requireNonNull(ioFunction);
        
        return ioFunction.unchecked();
    }
}

