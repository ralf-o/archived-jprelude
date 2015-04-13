package org.jprelude.common.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public interface SeqSubject<T> extends SeqObserver<T>, SeqObservable<T> {
    boolean hasObservers();
    
    static <T> SeqSubject<T> create() {
        return new SeqSubject<T>() {
            final Set<SeqObserver<T>> observers = new HashSet<>(); 

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
            public Disposable subscribe(final SeqObserver<T> observer) {
                Objects.requireNonNull(observer);
                
                this.observers.add(observer);
                return ()-> this.observers.remove(observer);
            }
        };
    }
}
