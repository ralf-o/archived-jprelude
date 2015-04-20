package org.jprelude.core.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jprelude.core.util.function.CheckedCommand;
import org.jprelude.core.util.function.CheckedSupplier;

public interface Try<T> {
    T get();
    Throwable getError();

    boolean isSuccess();
    
    default boolean isError() {
        return !this.isSuccess();
    }
    
    default T orElse(final T other) {
        return this.isSuccess() ? this.get() : other;
    }
    
    default T orElseGet(final Supplier<? extends T> other) {
        Objects.requireNonNull(other);
        
        return this.isSuccess() ? this.get() : other.get() ;
    }
    
    default T orElseThrow() {
        if (!this.isSuccess()) {
            final Throwable throwable = this.getError();
            
            if (throwable instanceof IOException) {
                throw new UncheckedIOException((IOException) throwable);
            } else if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else {
                throw new RuntimeException(throwable);
            }
        }
        
        return this.get();
    }
      
    default Try<T> ifErrorThrow() throws Throwable {
        if (this.isError()) {
            throw this.getError();
        }
        
        return this;
    }
    
    default Try<T> ifErrorThrowUnchecked() {
        if (this.isError()) {
            final Throwable error = this.getError();
            
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            } else {
                throw new RuntimeException(error);
            }
        }
        
        return this;
    }
    
    default <E extends Throwable> Try<T> ifCertainError(final Class<E> errorType, final Consumer<Throwable> consumer) {
        Objects.requireNonNull(errorType);
        
        return this.ifError(error -> {
                if (errorType.isAssignableFrom(error.getClass())) {
                    consumer.accept(error);
                }
            }
        );
    }
    
    default <E extends Throwable> Try<T> ifCertainErrorThrow(final Class<E> errorType) throws E {
          Objects.requireNonNull(errorType);
        
        if (!this.isSuccess()) {
            final Throwable throwable = this.getError();

            if (errorType.isAssignableFrom(throwable.getClass())) {
                throw (E) throwable;
            }
        }
        
        return this;
    }

    default <R> Try<R> map(final Function<? super T, ? extends R> f) {
        Try<R> ret;
        
        try {
            ret = this.isSuccess()
                ? Try.of(f.apply(this.get()))
                : Try.error(this.getError());
        } catch (final Throwable throwable) {
            ret = Try.error(throwable);
        }
        
        return ret;
    }
    
    default <R> Try<R> flatMap(final Function<? super T, Try<R>> f) {
        Try<R> ret;
        
        try {
            ret = this.isSuccess()
                    ? f.apply(this.get())
                    : Try.error(this.getError());
        } catch (final Throwable t) {
            ret = Try.error(this.getError());
        }
        
        return ret;
    }
   
    default Try<T>  ifSuccess(final Consumer<T> consumer) {
        if (this.isSuccess()) {
            consumer.accept(this.get());
        }
        
        return this;
    }
    
    default Try<T> ifError(final Consumer<Throwable> consumer) {
        if (this.isError()) {
            consumer.accept(this.getError());
        }
        
        return this;
    }
    
    static <T> Try<T> of(final T value) {
        return new Try<T>() {
            @Override
            public T get() {
                return value;
            }

            @Override
            public Throwable getError() {
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

    static <T> Try<T> error(final Throwable error) {
        Objects.requireNonNull(error);

        return new Try<T>() {
            @Override
            public T get() {
                if (this.isSuccess()) {
                    throw new NoSuchElementException("Try object represents an error - value cannot be retrieved");
                }
                
                return null;
            }

            @Override
            public Throwable getError() {
                return error;
            }

            @Override
            public boolean isSuccess() {
                return false;
            }
        };
    }
    
    static Try<Void> tryToRun(final CheckedCommand command) {
        Objects.requireNonNull(command);
        
        return tryToGet(() -> {
            command.execute();
            return null;
        });
    }
    
    static <T> Try<T> tryToGet(final CheckedSupplier<? extends T> supplier) {
        Try<T> ret;
        
        try {
            ret = Try.of(supplier.get());
        } catch (final Throwable trowable) {
            ret = Try.error(trowable);
        }
        
        return ret;
    };
}
