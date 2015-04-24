
package org.jprelude.core.util.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface CheckedPredicate<T, E extends Exception> {
    boolean test(T t) throws E;

    default CheckedPredicate<T, E> and(final CheckedPredicate<? super T, ? extends E> other) {
        Objects.requireNonNull(other);
        return t -> this.test(t) && other.test(t);
    }

    default CheckedPredicate<T, E> negate() {
        return t -> !this.test(t);
    }

    default CheckedPredicate<T, E> or(CheckedPredicate<? super T, ? extends E> other) {
        Objects.requireNonNull(other);
        return t -> this.test(t) || other.test(t);
    }
    
    /*
    static <T> CheckedPredicate<T, E> isEqual(final Object targetRef) {
        Objects.nonNull(targetRef);
        
        return (null == targetRef)
                ? Objects::isNull
                : object -> targetRef.equals(object);
    }
*/
    default Predicate<T> unchecked() {
        return value -> {
            boolean ret;
            
            try {
                ret = CheckedPredicate.this.test(value);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            
            return ret;
        };
    }
    
    static <T> Predicate<T> unchecked(final CheckedPredicate<T, ?> predicate) {
        Objects.requireNonNull(predicate);
        
        return predicate.unchecked();
    }
 }
