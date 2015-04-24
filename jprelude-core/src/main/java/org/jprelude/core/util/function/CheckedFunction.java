package org.jprelude.core.util.function;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
    
    default Function<T, R> unchecked() {
        return value -> {
            R ret;
            
            try {
                ret =this.apply(value);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            
            return ret;
        };
    }
    
    static <T, R> Function<T, R> unchecked(final CheckedFunction<T, R, ?> function) {
        Objects.requireNonNull(function);
        
        return function.unchecked();
    }

    default <U> CheckedFunction<U, R, E> compose(CheckedFunction<? super U, ? extends T, ? extends E> before) {
        Objects.requireNonNull(before);
        
        return u -> apply(before.apply(u));
    }

    default <U> CheckedFunction<T, U, E> andThen(final CheckedFunction<? super R, ? extends U, ? extends E> after) {
        Objects.requireNonNull(after);
        
        return t -> after.apply(this.apply(t));
    }

    static <T, E extends Exception> CheckedFunction<T, T, E> identity() {
        return t -> t;
    }
}

