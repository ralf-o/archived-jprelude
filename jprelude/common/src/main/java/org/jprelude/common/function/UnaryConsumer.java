package org.jprelude.common.function;

import java.util.function.Consumer;

@FunctionalInterface
public interface UnaryConsumer<A> extends Consumer<A> {
    public static  <A> UnaryConsumer<A> from(final Consumer<A> consumer) {
        return v -> consumer.accept(v);
    }
}
