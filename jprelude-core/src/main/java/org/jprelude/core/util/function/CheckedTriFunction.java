package org.jprelude.core.util.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedTriFunction <T1, T2, T3, R> {
    R apply(T1 v1, T2 v2, T3 v3) throws Throwable;
   
    default TriFunction<T1, T2, T3, R> unchecked() {
        return (v1, v2, v3) -> {
            final R ret;
            
            try {
                ret = CheckedTriFunction.this.apply(v1, v2, v3);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
            
            return ret;
        };
    }

    static <T1, T2, T3, R> TriFunction<T1, T2, T3, R> unchecked(
            final CheckedTriFunction<T1, T2, T3, R> triFunction) {
        
        Objects.requireNonNull(triFunction);
        
        return triFunction.unchecked();
    }    
}
