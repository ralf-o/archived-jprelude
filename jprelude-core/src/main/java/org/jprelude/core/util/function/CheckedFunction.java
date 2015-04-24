package org.jprelude.core.util.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction<T, R> {
    R apply(T t) throws Throwable;
    
    default Function<T, R> unchecked() {
        return value -> {
            R ret;
            
            try {
                ret =this.apply(value);
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
    
    static <T, R> Function<T, R> unchecked(final CheckedFunction<T, R> function) {
        Objects.requireNonNull(function);
        
        return function.unchecked();
    }

    default <U> CheckedFunction<U, R> compose(CheckedFunction<? super U, ? extends T> before) {
        Objects.requireNonNull(before);
        
        return u -> apply(before.apply(u));
    }

    default <U> CheckedFunction<T, U> andThen(Function<? super R, ? extends U> after) {
        Objects.requireNonNull(after);
        
        return t -> after.apply(this.apply(t));
    }

    static <T> CheckedFunction<T, T> identity() {
        return v -> v;
    }
}

