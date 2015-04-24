package org.jprelude.core.util.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedTriConsumer<T1, T2, T3> {
    void accept(T1 v1, T2 v2, T3 v3) throws Throwable;
   
    default TriConsumer<T1, T2, T3> unchecked() {
        return (v1, v2, v3) -> {
            try {
                CheckedTriConsumer.this.accept(v1, v2, v3);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    static <T1, T2, T3> TriConsumer<T1, T2, T3> unchecked(
            final CheckedTriConsumer<T1, T2, T3> triConsumer) {
        
        Objects.requireNonNull(triConsumer);
        
        return triConsumer.unchecked();
    }
}
