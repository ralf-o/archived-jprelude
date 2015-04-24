package org.jprelude.core.util.function;

import java.util.Objects;
import java.util.function.BiFunction;

@FunctionalInterface
public interface CheckedBiFunction<T1, T2, R> {
    R apply(T1 v1, T2 v2) throws Throwable;
   
    default BiFunction<T1, T2, R> unchecked() {
        return (v1, v2) -> {
            final R ret;
            
            try {
                ret = CheckedBiFunction.this.apply(v1, v2);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
            
            return ret;
        };
    }

    static <T1, T2, R> BiFunction<T1, T2, R> unchecked(
            final CheckedBiFunction<T1, T2, R> biFunction) {
        
        Objects.requireNonNull(biFunction);
        
        return biFunction.unchecked();
    }
}
