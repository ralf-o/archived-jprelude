package org.jprelude.common.event;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jprelude.common.function.Command;
import org.jprelude.common.util.Disposable;

public interface EventStream<T> {
    Disposable subscribe(final EventObserver<T> observer);
    
    <R> EventStream<R> map(final Function<? super T, ? extends R> f);

    default Disposable subscribe(final Consumer<T> onNext, final Command onComplete, final Consumer<Throwable> onError) {
        final EventObserver<T> observer = new EventObserver<T>() {
            @Override
            public void onNext(T item) {
                if (onNext != null) {
                    onNext.accept(item);
                }
            }

            @Override
            public void onComplete() {
                if (onComplete != null) {
                    onComplete.execute();
                }
            }
            
            @Override
            public void onError(final Throwable t) {
                if (onError != null) {
                    onError.accept(t);
                }
            }
        };
        
        return this.subscribe(observer);
    }
    
    default Disposable subscribe(final Consumer<T> onNext) {
        return this.subscribe(onNext, null, null);
    } 
    
    default Disposable subscribe(final Consumer<T> onNext, final Command onComplete) {
        return this.subscribe(onNext, onComplete, null);
    }
    
    default Disposable subscribe(final Consumer<T> onNext, final Consumer<Throwable> onError) {
        return this.subscribe(onNext, null, onError);
    }
    
    static <T> EventStream<T> create(final Function<EventObserver<T>, Disposable> onSubscribe) {
        Objects.requireNonNull(onSubscribe);
        
        return new EventStream<T>() {         
            @Override
            public Disposable subscribe(final EventObserver<T> observer) {
                Objects.requireNonNull(observer);
                final Disposable disposable = onSubscribe.apply(observer);
                return disposable::dispose;
            }

            @Override
            public <R> EventStream<R> map(Function<? super T, ? extends R> f) {
                return EventStream.create(
                    observer -> this.subscribe(
                        v -> observer.onNext(f.apply(v)),
                        () -> observer.onComplete(),
                        t -> observer.onError(t)
                    )
                );
            }        
        };
    }
    
    static <T> EventStream empty() {
        return EventStream.create(observer -> {
            if (observer != null) {
                observer.onComplete();
            }
            
            return () -> {};
        });
    }
}

