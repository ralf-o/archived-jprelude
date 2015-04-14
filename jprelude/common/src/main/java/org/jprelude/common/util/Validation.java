package org.jprelude.common.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Validation<V, E extends Throwable> {
    V getValue();
    E getError();

    boolean isSuccess();
    
    default boolean isError() {
        return !this.isSuccess();
    }
    
    default V orElse(final V other) {
        return this.isSuccess() ? this.getValue() : other;
    }
    
    default V orElseGet(final Supplier<? extends V> other) {
        Objects.requireNonNull(other);
        
        return this.isSuccess() ? this.getValue() : other.get() ;
    }
    
    default V orElseThrow() throws E {
        if (!this.isSuccess()) {
            throw this.getError();
        }
        
        return this.getValue();
    }
    
    default Validation<V, E> ifErrorThrow() throws E {
        if (this.isError()) {
            throw this.getError();
        }
        
        return this;
    }
    
    default <R> Validation<R, E> map(final Function<? super V, ? extends R> f) {
        return this.isSuccess()
                ? Validation.value(f.apply(this.getValue()))
                : Validation.error(this.getError());
    }
    
    default <R> Validation<R, E> flatMap(final Function<? super V, Validation<R, E>> f) {
        return this.isSuccess()
                ? f.apply(this.getValue())
                : Validation.error(this.getError());
    }
   
    default Validation<V, E>  ifSuccess(final Consumer<V> consumer) {
        if (this.isSuccess()) {
            consumer.accept(this.getValue());
        }
        
        return this;
    }
    
    default Validation<V, E> ifError(final Consumer<E> consumer) {
        if (this.isError()) {
            consumer.accept(this.getError());
        }
        
        return this;
    }
    
    static <V, E extends Throwable> Validation<V, E> value(final V value) {
        return new Validation<V, E>() {
            @Override
            public V getValue() {
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

    static <V, E extends Throwable> Validation<V, E> error(final E error) {
        Objects.requireNonNull(error);

        return new Validation<V, E>() {
            @Override
            public V getValue() {
                if (this.isSuccess()) {
                    throw new NoSuchElementException("Validation object represents an error - value cannot be retrieved");
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
