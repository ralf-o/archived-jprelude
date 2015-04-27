package org.jprelude.experimental.dataexport.base;

import java.util.Objects;
import java.util.function.Supplier;
import org.jprelude.core.util.Seq;

public interface DataSource<T, K> extends Supplier<Seq<T>> {
    K getKey();
}
