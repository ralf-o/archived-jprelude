package org.jprelude.common.io.function;

import java.io.IOException;

@FunctionalInterface
public interface IOConsumer<T> {
    void accept(T t) throws IOException;
}
