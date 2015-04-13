package org.jprelude.common.util;

public interface SeqObserver<T> {
    void onNext(T item);
    void onError(Throwable throwable);
    void onComplete();
}
