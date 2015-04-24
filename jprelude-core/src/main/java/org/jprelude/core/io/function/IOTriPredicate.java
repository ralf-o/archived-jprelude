package org.jprelude.core.io.function;

import java.io.IOException;
import org.jprelude.core.util.function.CheckedTriPredicate;

@FunctionalInterface
public interface IOTriPredicate<T1, T2, T3> extends CheckedTriPredicate<T1, T2, T3> {
    @Override
    boolean test(T1 v1, T2 v2, T3 v3) throws IOException;
}
