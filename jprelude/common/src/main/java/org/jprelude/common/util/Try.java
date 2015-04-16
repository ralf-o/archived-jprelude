package org.jprelude.common.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Try<V, E extends Throwable> {
    V get();
    E getError();

    boolean isSuccess();
    
    default boolean isError() {
        return !this.isSuccess();
    }
    
    default V orElse(final V other) {
        return this.isSuccess() ? this.get() : other;
    }
    
    default V orElseGet(final Supplier<? extends V> other) {
        Objects.requireNonNull(other);
        
        return this.isSuccess() ? this.get() : other.get() ;
    }
    
    default V orElseThrow() throws E {
        if (!this.isSuccess()) {
            throw this.getError();
        }
        
        return this.get();
    }
    
    default Try<V, E> ifErrorThrow() throws E {
        if (this.isError()) {
            throw this.getError();
        }
        
        return this;
    }
    
    default <R> Try<R, E> map(final Function<? super V, ? extends R> f) {
        return this.isSuccess()
                ? Try.of(f.apply(this.get()))
                : Try.error(this.getError());
    }
    
    default <R> Try<R, E> flatMap(final Function<? super V, Try<R, E>> f) {
        return this.isSuccess()
                ? f.apply(this.get())
                : Try.error(this.getError());
    }
   
    default Try<V, E>  ifSuccess(final Consumer<V> consumer) {
        if (this.isSuccess()) {
            consumer.accept(this.get());
        }
        
        return this;
    }
    
    default Try<V, E> ifError(final Consumer<E> consumer) {
        if (this.isError()) {
            consumer.accept(this.getError());
        }
        
        return this;
    }
    
    static <V, E extends Throwable> Try<V, E> of(final V value) {
        return new Try<V, E>() {
            @Override
            public V get() {
                return value;
            }

            @Override
            public E getError() {
                if (this.isSuccess()) {
                    throw new NoSuchElementException("Validation object represents a success - error cannot be retrieved");
                }
                
                return null;
            }

            @Override
            public boolean isSuccess() {
                return true;
            }
        };
    }

    static <V, E extends Throwable> Try<V, E> error(final E error) {
        Objects.requireNonNull(error);

        return new Try<V, E>() {
            @Override
            public V get() {
                if (this.isSuccess()) {
                    throw new NoSuchElementException("Try object represents an error - value cannot be retrieved");
                }
                
                return null;
            }

            @Override
            public E getError() {
                return error;
            }

            @Override
            public boolean isSuccess() {
                return false;
            }
        };
    }
}
