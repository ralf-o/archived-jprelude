package org.jprelude.common.util;

public interface Observer<T> {
    void onNext(T item);
    void onError(Throwable throwable);
    void onComplete();
    void onSubscribe(Subscription subscription);
}
