package org.jprelude.common.function;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@FunctionalInterface
public interface BinaryConsumer<A1, A2> extends BiConsumer<A1, A2> {
    public static  <A1, A2> BinaryConsumer<A1, A2> from(final BiConsumer<A1, A2> consumer) {
        Objects.nonNull(consumer);
        return (v1, v2) -> consumer.accept(v1, v2);
    }
}
