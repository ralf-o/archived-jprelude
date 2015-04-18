
package org.jprelude.core.io.function;

import java.io.IOException;
import org.jprelude.core.util.function.CheckedPredicate;

@FunctionalInterface
public interface IOPredicate<T> extends CheckedPredicate<T> {
    @Override
    boolean test(T t) throws IOException;
}
