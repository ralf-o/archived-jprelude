package org.jprelude.core.io.function;

import java.io.IOException;
import org.jprelude.core.util.function.CheckedBiPredicate;

@FunctionalInterface 
public interface IOBiPredicate<T1, T2> extends CheckedBiPredicate<T1, T2> {
    @Override
    boolean test(T1 v1, T2 v2) throws IOException;
}
