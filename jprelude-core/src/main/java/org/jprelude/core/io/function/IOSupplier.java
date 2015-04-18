package org.jprelude.core.io.function;

import java.io.IOException;
import org.jprelude.core.util.function.CheckedSupplier;

@FunctionalInterface
public interface IOSupplier<T> extends CheckedSupplier<T> {
    @Override
    T get() throws IOException;
}
