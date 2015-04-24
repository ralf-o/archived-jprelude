package org.jprelude.core.util.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedTriFunction <T1, T2, T3, R, E extends Exception> {
    R apply(T1 v1, T2 v2, T3 v3) throws E;
   
    default TriFunction<T1, T2, T3, R> unchecked() {
        return (v1, v2, v3) -> {
            final R ret;
            
            try {
                ret = CheckedTriFunction.this.apply(v1, v2, v3);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            
            return ret;
        };
    }

    default <U> CheckedTriFunction<T1, T2, T3, U, E> andThen(
            final CheckedFunction<? super R, ? extends U, ? extends E> after) {
        
        Objects.requireNonNull(after);
        
        return (t1, t2, t3) -> after.apply(apply(t1, t2, t3));
    }

    static <T1, T2, T3, R> TriFunction<T1, T2, T3, R> unchecked(
            final CheckedTriFunction<T1, T2, T3, R, ?> f) {
        
        Objects.requireNonNull(f);
        return f.unchecked();
    }    
}
