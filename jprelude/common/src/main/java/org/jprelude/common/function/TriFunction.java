package org.jprelude.common.function;

@FunctionalInterface
public interface TriFunction<A1, A2, A3, R> {
    R apply(A1 a1, A2 a2, A3 a3);
}
