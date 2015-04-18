package org.jprelude.core.io.function;

import java.io.IOException;
import org.jprelude.core.util.function.CheckedConsumer;

@FunctionalInterface
public interface IOConsumer<T> extends CheckedConsumer<T> {
    @Override
    void accept(T t) throws IOException;
}
