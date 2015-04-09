
package org.jprelude.common.io.function;

import java.io.IOException;

@FunctionalInterface
public interface IOPredicate<T> {
    boolean accept(T t) throws IOException;
}
