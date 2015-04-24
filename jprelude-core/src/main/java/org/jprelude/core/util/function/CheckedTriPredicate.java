package org.jprelude.core.util.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedTriPredicate<T1, T2, T3> {
    boolean test(T1 v1, T2 v2, T3 v3) throws Throwable;
   
    default TriPredicate<T1, T2, T3> unchecked() {
        return (v1, v2, v3) -> {
            final boolean ret;
            
            try {
                ret = CheckedTriPredicate.this.test(v1, v2, v3);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
            
            return ret;
        };
    }

    static <T1, T2, T3> TriPredicate<T1, T2, T3> unchecked(
            final CheckedTriPredicate<T1, T2, T3> biPredicate) {
        
        Objects.requireNonNull(biPredicate);
        
        return biPredicate.unchecked();
    }    
}
