
package org.jprelude.core.util.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface CheckedPredicate<T> {
    boolean test(T t) throws Exception;

    default CheckedPredicate<T> and(final CheckedPredicate<? super T> other) {
        Objects.requireNonNull(other);
        return t -> this.test(t) && other.test(t);
    }

    default CheckedPredicate<T> negate() {
        return t -> !this.test(t);
    }

    default CheckedPredicate<T> or(CheckedPredicate<? super T> other) {
        Objects.requireNonNull(other);
        return t -> this.test(t) || other.test(t);
    }
    
    static <T> CheckedPredicate<T> isEqual(final Object targetRef) {
        Objects.nonNull(targetRef);
        
        return (null == targetRef)
                ? Objects::isNull
                : object -> targetRef.equals(object);
    }

    default Predicate<T> unchecked() {
        return value -> {
            boolean ret;
            
            try {
                ret = CheckedPredicate.this.test(value);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
            
            return ret;
        };
    }
    
    static <T> Predicate<T> unchecked(final CheckedPredicate<T> predicate) {
        Objects.requireNonNull(predicate);
        
        return predicate.unchecked();
    }
 }
