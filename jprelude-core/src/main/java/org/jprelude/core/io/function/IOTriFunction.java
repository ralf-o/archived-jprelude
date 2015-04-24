package org.jprelude.core.io.function;

import java.io.IOException;
import java.util.Objects;
import org.jprelude.core.util.function.CheckedFunction;
import org.jprelude.core.util.function.CheckedTriFunction;

@FunctionalInterface
public interface IOTriFunction<T1, T2, T3, R> extends CheckedTriFunction<T1, T2, T3, R> {
    @Override
    R apply(T1 v1, T2 v2, T3 v3) throws IOException;

    default <U> IOTriFunction<T1, T2, T3, U> andThen(
            final IOFunction<? super R, ? extends U> after) {
        
        Objects.requireNonNull(after);
        
        return (t1, t2, t3) -> after.apply(apply(t1, t2, t3));
    }
}
