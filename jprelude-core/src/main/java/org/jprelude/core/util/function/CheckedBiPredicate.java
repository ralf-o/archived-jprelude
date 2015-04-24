package org.jprelude.core.util.function;

import java.util.Objects;
import java.util.function.BiPredicate;

public interface CheckedBiPredicate<T1, T2> {
    boolean test(T1 v1, T2 v2) throws Throwable;
   
    default BiPredicate<T1, T2> unchecked() {
        return (v1, v2) -> {
            final boolean ret;
            
            try {
                ret = CheckedBiPredicate.this.test(v1, v2);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
            
            return ret;
        };
    }

    static <T1, T2> BiPredicate<T1, T2> unchecked(
            final CheckedBiPredicate<T1, T2> biPredicate) {
        
        Objects.requireNonNull(biPredicate);
        
        return biPredicate.unchecked();
    }    
}
