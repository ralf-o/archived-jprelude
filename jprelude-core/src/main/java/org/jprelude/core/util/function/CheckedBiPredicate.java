package org.jprelude.core.util.function;

import java.util.Objects;
import java.util.function.BiPredicate;

public interface CheckedBiPredicate<T1, T2> {
    boolean test(T1 t1, T2 t2) throws Exception;

    default CheckedBiPredicate<T1, T2> and(
            final CheckedBiPredicate<? super T1, ? super T2> other) {
        
        Objects.requireNonNull(other);
        return (T1 t1, T2 t2) -> test(t1, t2) && other.test(t1, t2);
    }

    default CheckedBiPredicate<T1, T2> negate() {
        return (t1, t2) -> !test(t1, t2);
    }

    default CheckedBiPredicate<T1, T2> or(
            final CheckedBiPredicate<? super T1, ? super T2> other) {
        
        Objects.requireNonNull(other);
        return (T1 t1, T2 t2) -> test(t1, t2) || other.test(t1, t2);
    }
   
    default BiPredicate<T1, T2> unchecked() {
        return (t1, t2) -> {
            final boolean ret;
            
            try {
                ret = CheckedBiPredicate.this.test(t1, t2);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
            
            return ret;
        };
    }

    static <T1, T2> BiPredicate<T1, T2> unchecked(
            final CheckedBiPredicate<T1, T2> pred) {
        
        Objects.requireNonNull(pred);
        
        return pred.unchecked();
    }    
}
