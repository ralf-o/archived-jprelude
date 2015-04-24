package org.jprelude.core.util.function;

import java.util.Objects;
import java.util.function.BiFunction;

@FunctionalInterface
public interface CheckedBiFunction<T1, T2, R, E extends Exception> {
    R apply(T1 t1, T2 t2) throws E;
   
    default BiFunction<T1, T2, R> unchecked() {
        return (t1, t2) -> {
            final R ret;
            
            try {
                ret = CheckedBiFunction.this.apply(t1, t2);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            
            return ret;
        };
    }

    default <U> CheckedBiFunction<T1, T2, U, E> andThen(CheckedFunction<? super R, ? extends U, ? extends E> after) {
        Objects.requireNonNull(after);
        
        return (t1, t2) -> after.apply(apply(t1, t2));
    }

    
    static <T1, T2, R> BiFunction<T1, T2, R> unchecked(
            final CheckedBiFunction<T1, T2, R, ?> f) {
        
        Objects.requireNonNull(f);
        
        return f.unchecked();
    }
}
