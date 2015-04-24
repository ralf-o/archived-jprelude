package org.jprelude.core.io.function;

import java.io.IOException;
import java.util.Objects;
import org.jprelude.core.util.function.CheckedConsumer;


@FunctionalInterface
public interface IOConsumer<T> extends CheckedConsumer<T> {
    @Override
    void accept(T t) throws IOException;

    default IOConsumer<T> andThen(final IOConsumer<? super T> after) {
        Objects.requireNonNull(after);
        
        return v -> {
            accept(v);
            after.accept(v);
        };
    }

}
