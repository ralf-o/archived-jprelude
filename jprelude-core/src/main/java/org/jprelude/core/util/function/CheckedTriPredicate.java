package org.jprelude.core.util.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedTriPredicate<T1, T2, T3> {
    boolean test(T1 t1, T2 t2, T3 t3) throws Exception;
  
    default CheckedTriPredicate<T1, T2, T3> and(
            final CheckedTriPredicate<? super T1, ? super T2, ? super T3> other) {
        
        Objects.requireNonNull(other);
        return (t1, t2, t3) -> test(t1, t2, t3) && other.test(t1, t2, t3);
    }

    default CheckedTriPredicate<T1, T2, T3> negate() {
        return (t1, t2, t3) -> !test(t1, t2, t3);
    }

    default CheckedTriPredicate<T1, T2, T3> or(
            final CheckedTriPredicate<? super T1, ? super T2, ? super T3> other) {
        
        Objects.requireNonNull(other);
        return (t1, t2, t3) ->
                this.test(t1, t2, t3) || other.test(t1, t2, t3);
    }
   
    default TriPredicate<T1, T2, T3> unchecked() {
        return (t1, t2, t3) -> {
            final boolean ret;
            
            try {
                ret = CheckedTriPredicate.this.test(t1, t2, t3);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
            
            return ret;
        };
    }

    static <T1, T2, T3> TriPredicate<T1, T2, T3> unchecked(
            final CheckedTriPredicate<T1, T2, T3> pred) {
        
        Objects.requireNonNull(pred);
        return pred.unchecked();
    }    
}
