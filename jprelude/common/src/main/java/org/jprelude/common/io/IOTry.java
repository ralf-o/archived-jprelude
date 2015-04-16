package org.jprelude.common.io;

import java.io.IOException;
import java.util.NoSuchElementException;
import org.jprelude.common.util.Try;

public interface IOTry<T> extends Try<T, IOException> {
    static <T> IOTry of(final T value) {
        return new IOTry<T>() {
            @Override
            public T get() {
                return value;
            }

            @Override
            public IOException getError() {
                throw new NoSuchElementException("Try object represents a successr - error is not available");
            }

            @Override
            public boolean isSuccess() {
                return true;
            }
        };
    }
    
    static <T> IOTry error(final IOException error) {
        return new IOTry<T>() {
            @Override
            public T get() {
                throw new NoSuchElementException("Try object represents an error - value is not available");
            }

            @Override
            public IOException getError() {
                return error;
            }

            @Override
            public boolean isSuccess() {
                return false;
            }
            
        };
    }
}

