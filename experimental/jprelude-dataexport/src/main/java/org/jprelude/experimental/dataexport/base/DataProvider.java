package org.jprelude.experimental.dataexport.base;

public interface DataProvider<T, K> {
    T provide(K key);
}
