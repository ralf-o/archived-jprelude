package org.jprelude.core.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Mutable<T>  extends Supplier<T> {
    void set(T value);

    default void update(final Function<T, T> f) {
        this.set(f.apply(this.get()));
    }
    
    default void clear() {
        this.set(null);
    }
    
    default T orElse(final T other) {
        final T value = this.get();
        return (value != null ? value : other);
    }
    
    default T orElseGet(final Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        final T value = this.get();
        return (value != null ? value : supplier.get());
    }

    default <X extends Throwable> T orElseThrow(final Supplier<X> supplier) throws  X {
        Objects.requireNonNull(supplier);
        final T value = this.get();
        
        if (value == null) {
            final X throwable = supplier.get();
            
            if (throwable != null) {
                throw throwable;
            }
        }
        
        return value;
    }
    
    default boolean isPresent() {
        return this.get() != null;
    }
    
    default boolean isEmpty() {
        return this.get() == null;
    }
    
    default void ifPresent(final Consumer<T> consumer) {
        if (consumer != null) {
            final T value = this.get();
            
            if (value != null) {
                consumer.accept(value);
            }
        }
    }

    default void ifEmpty(final Consumer<T> consumer) {
        if (consumer != null) {
            final T value = this.get();
            
            if (value != null) {
                consumer.accept(value);
            }
        }
    }
    
    static <T> Mutable<T> empty() {
        return Mutable.of(null);
    }
    
    static <T> Mutable<T> of(final T value) {
        return new Mutable<T>() {
             private T val = value;

            @Override
            public void set(final T newValue) {
                this.val = newValue;
            }

            @Override
            public T get() {
                return this.val;
            }
        };
    }
}
