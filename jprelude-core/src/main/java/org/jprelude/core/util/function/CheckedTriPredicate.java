package org.jprelude.core.util.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedTriPredicate<T1, T2, T3, E extends Exception> {
    boolean test(T1 t1, T2 t2, T3 t3) throws E;
  
    default CheckedTriPredicate<T1, T2, T3, E> and(
            final CheckedTriPredicate<? super T1, ? super T2, ? super T3, ? extends E> other) {
        
        Objects.requireNonNull(other);
        return (t1, t2, t3) -> test(t1, t2, t3) && other.test(t1, t2, t3);
    }

    default CheckedTriPredicate<T1, T2, T3, E> negate() {
        return (t1, t2, t3) -> !test(t1, t2, t3);
    }

    default CheckedTriPredicate<T1, T2, T3, E> or(
            final CheckedTriPredicate<? super T1, ? super T2, ? super T3, E> other) {
        
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
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            
            return ret;
        };
    }

    static <T1, T2, T3> TriPredicate<T1, T2, T3> unchecked(
            final CheckedTriPredicate<T1, T2, T3, ?> pred) {
        
        Objects.requireNonNull(pred);
        return pred.unchecked();
    }    
}
