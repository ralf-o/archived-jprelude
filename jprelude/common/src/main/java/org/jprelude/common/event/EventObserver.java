package org.jprelude.common.event;

public interface EventObserver<T> {
    void onNext(T item);
    void onError(Throwable throwable);
    void onComplete();
}
