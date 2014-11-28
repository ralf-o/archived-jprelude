package org.jprelude.common.function;

import java.util.function.Function;
import rx.functions.Func1;

@FunctionalInterface
public interface UnaryFunction<A, R> extends Function<A, R> {
    default Func1<A, R> toUnaryFunction() {
        return x -> this.apply(x);
    }
}
