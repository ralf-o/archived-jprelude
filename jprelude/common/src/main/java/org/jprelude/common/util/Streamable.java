package org.jprelude.common.util;

import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@FunctionalInterface
public interface Streamable<T> {
    Stream<T> stream();

    static <T> Streamable<T> from(Streamable<T> streamable) {
        return streamable == null
                ? () -> Stream.empty()
                : streamable;
    }

    static <T> Streamable<T> from(Iterable<T> iterable) {
        return new Streamable() {
            @Override
            public Stream<T> stream() {
                final Stream stream;

                if (iterable == null) {
                    stream = Stream.empty();
                } else if (iterable instanceof Streamable) {
                    stream = ((Streamable) iterable).stream();
                } else if (iterable instanceof Collection) {
                    stream = ((Collection) iterable).stream();
                } else {
                    stream = StreamSupport.stream(iterable.spliterator(), false);
                }
                
                return stream;
            }
        };
    }
}
