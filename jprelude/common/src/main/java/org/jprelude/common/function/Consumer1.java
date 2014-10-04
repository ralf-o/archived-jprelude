package org.jprelude.common.function;

import java.util.function.Consumer;

@FunctionalInterface
public interface Consumer1<A> extends Consumer<A> {
    public static  <A> Consumer1<A> from(final Consumer<A> consumer) {
        return v -> consumer.accept(v);
    }
}
