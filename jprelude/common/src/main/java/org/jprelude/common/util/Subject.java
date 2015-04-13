package org.jprelude.common.util;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public interface Subject<T, R> extends Observer<T>, Observable<R> {
    boolean hasObservers();
    
    static <T> Subject<T, T> create() {
        return new Subject<T, T>() {
            final Set<Observer<T>> observers = new ConcurrentSkipListSet<>(); 

            @Override
            public boolean hasObservers() {
                return !this.observers.isEmpty();
            }

            @Override
            public void onNext(final T item) {
                this.observers.forEach(observer -> observer.onNext(item));
            }

            @Override
            public void onComplete() {
                this.observers.forEach(observer -> observer.onComplete());
            }

            @Override
            public void onError(final Throwable throwable) {
                this.observers.forEach(observer -> observer.onError(throwable));
            }

            @Override
            public Disposable subscribe(final Observer<T> observer) {
                Objects.requireNonNull(observer);
                
                this.observers.add(observer);
                return ()-> this.observers.remove(observer);
            }
        };
    }
}
