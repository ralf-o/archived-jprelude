package org.jprelude.common.util;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jprelude.common.function.Command;

public interface Observable<T> {
    Disposable subscribe(final Observer<T> observer);
    
    default Disposable subscribe(final Consumer<T> onNext, final Command onComplete, final Consumer<Throwable> onError) {
        final Disposable[] disposableWrapper  = { null };
        
        final Observer<T> observer = new Observer<T>() {
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
                
                if (disposableWrapper[0] != null) {
                    disposableWrapper[0].dispose();
                    System.out.println("Juhuuuuuuuuuuuuuuuuuuuuuuu");
                    disposableWrapper[0] = null;
                }
            }
            
            @Override
            public void onError(final Throwable t) {
                if (onError != null) {
                    onError.accept(t);
                }
            }
        };
        
        disposableWrapper[0] = this.subscribe(observer);
        return disposableWrapper[0];
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
    
    static <T> Observable<T> create(final Function<Observer<T>, Disposable> onSubscribe) {
        Objects.requireNonNull(onSubscribe);
        
        return observer -> {
            Objects.requireNonNull(observer);
            final Disposable disposable = onSubscribe.apply(observer);
            return disposable::dispose;
        };
    }
    
    default Observable<T> flatMap(final Function<T, Observable<T>> f) {
        Objects.requireNonNull(f);
        
        return Observable.flatten(this.map(f));
    }

    default <R> Observable <R> map(final Function<? super T, ? extends R> f) {
        return this.map((item, idx) -> f.apply(item));
    }
    
    default <R> Observable<R> map(final BiFunction<? super T, Long, ? extends R> f) {
        return Observable.create(
            observer -> {
                final Mutable<Disposable> disposable = Mutable.empty();
                
                disposable.set(this.subscribe(new Observer<T>() {
                    private long counter = 0;

                    @Override
                    public void onNext(final T item) {
                        try {
                            observer.onNext(f.apply(item, counter++));
                        } catch (final Throwable throwable) {
                            observer.onError(throwable);
                            
                            disposable.ifPresent(Disposable::dispose);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        observer.onError(throwable);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                }));

                return disposable.get();
            }
        );
    }
    
    
    default Observable<T> filter(final Predicate<T> pred) {
        Objects.requireNonNull(pred);
        return this.filter((item,idx) -> pred.test(item));
    }
        
        
    default Observable<T> filter(final BiPredicate<T, Long> pred) {
        Objects.requireNonNull(pred);
        
        return Observable.create(
            observer -> this.subscribe(new Observer<T>() {
                private long counter = 0;
                
                @Override
                public void onNext(final T item) {
                    try {
                        if (pred.test(item, counter++)) {
                            observer.onNext(item);
                        }
                    } catch (final Throwable throwable) {
                        observer.onError(throwable);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    observer.onError(throwable);
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            })
        );
    }
    
    default Observable<T> reject(final Predicate<T> pred) {
        Objects.requireNonNull(pred);
        return this.filter(item -> !pred.test(item));
    }    

    default Observable<T> reject(final BiPredicate<T, Long> pred) {
        Objects.requireNonNull(pred);
        return this.filter((item, idx) -> !pred.test(item, idx));
    }    

    default Observable<T> skip(final long n) {
        return this.skipWhile((item, idx) -> idx < n);
    }
    
    default Observable<T> skipWhile(final Predicate<T> pred) {
        Objects.requireNonNull(pred);
        
        return this.skipWhile((item, idx) -> pred.test(item));
    }
    
    default Observable<T> skipWhile(final BiPredicate<T, Long> pred) {
        Objects.requireNonNull(pred);
        
        return Observable.create(
            observer -> this.subscribe(new Observer<T> () {
                private boolean hasStarted = false;
                private Long counter = 0L;
                
                @Override
                public void onNext(final T item) {
                    if (this.hasStarted) {
                        ++counter;
                        observer.onNext(item);
                    } else {
                        try {
                            if (pred.test(item, counter)) {
                                this.hasStarted = true;
                                ++counter;
                                observer.onNext(item);
                            }
                        } catch (final Throwable throwable) {
                            observer.onError(throwable);
                        }
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    observer.onError(throwable);
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            })
        );        
    }

    default Observable<T> take(final long n) {
        return this.takeWhile((item, idx) -> idx < n);
    }
    
    default Observable<T> takeWhile(final Predicate<T> pred) {
        return this.takeWhile((item, idx) -> pred.test(item));
    }
    
    default Observable<T> takeWhile(final BiPredicate<T, Long> pred) {
        Objects.requireNonNull(pred);
        
        return Observable.create(observer -> this.subscribe(new Observer<T> () {
            private Long counter = 0L;

            @Override
            public void onNext(final T item) {System.out.println("--------------------------" + this.counter);
                try {
                    if (!pred.test(item, counter++)) {
                        observer.onComplete();
                    } else {
                        observer.onNext(item);
                        ++counter;
                    }
                } catch (final Throwable throwable) {
                    observer.onError(throwable);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                observer.onError(throwable);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        }));
    }

    default Observable<T> prepend(final T value) {
        return Observable.concat(Observable.of(value), this);
    }

    default Observable<T> prependMany(final T... values) {
        return Observable.concat(Observable.of(values), this);
    }

    default Observable<T> append(final T value) {
        return Observable.concat(this, Observable.of(value));
    }

    default Observable<T> appendMany(final T... values) {
        return Observable.concat(this, Observable.of(values));
    }
    
    static <T> Observable<T> from(final Seq<T> seq) {
        Objects.requireNonNull(seq);

        return Observable.create(observer -> {
            final Mutable<Boolean> isCompleted = Mutable.of(false);
            
            if (observer != null) {
                final Seq<T> seq2 = Seq.sequential(seq).takeWhile(item -> !isCompleted.get());
                
                final Observer<T> observer2 = new Observer<T>() {
                    @Override
                    public void onNext(final T item) {
                        observer.onNext(item);
                    }

                    @Override
                    public void onError(final Throwable throwable) {
                        observer.onError(throwable);
                        isCompleted.set(true);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                        isCompleted.set(true);
                    }                
                };

                try (final Stream<T> stream = seq2.stream()) {
                    stream.forEach(val -> observer.onNext(val));
                    observer.onComplete();
                } catch (final Throwable throwable) {
                    observer.onError(throwable);
                }
            }
            
            return () -> isCompleted.set(true);
        });
    }
    
    static <T> Observable<T> empty() {
        return Observable.create(observer -> {
            if (observer != null) {
                observer.onComplete();
            }
            
            return () -> {};
        });
    }
    
    static <T> Observable<T> of(final T... items) {
        return Seq.of(items).toObservable();
    }
    
    static <T> Observable<T> from(final Iterable<T> items) {
        return Seq.from(items).toObservable();
    }
    
    public static Observable<Integer> range(final int start, final int end) {
        return Seq.range(start, end).toObservable();
    }
    
    public static Observable<Long> range(final Long start, final Long end) {
        return Seq.range(start, end).toObservable();
    }
                
    static <T> Observable<T> flatten(final Observable<Observable<T>> eventStreams) {
        final Observable<T> ret;
        
        // TODO: Ugly implementation - find a better solution
        
        if (eventStreams == null) {
            ret = Observable.empty();
        } else {
            ret = Observable.create(new Function<Observer<T>, Disposable>() {
                final Queue<Observable<T>> eventStreamQueue = new LinkedList<>();
                final Mutable<Boolean> noMoreEventStreams = Mutable.of(false);
                final Mutable<Disposable> subDisposable = Mutable.empty();
                
                @Override
                public Disposable apply(final Observer<T> observer) {
                    final Mutable<Disposable> masterDisposable = Mutable.empty();
                    
                    masterDisposable.set(eventStreams.subscribe(new Observer<Observable<T>>() {
                        @Override
                        public void onNext(final Observable<T> eventStream) {
                            if (subDisposable.isPresent()) {
                                eventStreamQueue.add(eventStream);
                            } else {
                                subDisposable.set(eventStream.subscribe(new Observer<T>() {
                                    @Override
                                    public void onNext(T item) {
                                        observer.onNext(item);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        observer.onError(throwable);
                                        noMoreEventStreams.set(true);
                                        masterDisposable.ifPresent(Disposable::dispose);
                                        masterDisposable.clear();
                                    }

                                    @Override
                                    public void onComplete() {
                                        System.out.println("Unsetting subDisposable");
                                        subDisposable.clear();
                                        
                                        System.out.println("sub-complete");
                                        if (!eventStreamQueue.isEmpty()) {
                                            System.out.println("Setting subDisposable2");
                                             subDisposable.set(eventStreamQueue.poll().subscribe(this));                                            
                                        } else if (noMoreEventStreams.get()) {
                                            observer.onComplete();
                                        }
                                    }                                    
                                }));
                            }
                        }

                        @Override
                        public void onError(final Throwable throwable) {
                            observer.onError(throwable);
                            eventStreamQueue.clear();
                            noMoreEventStreams.set(true);
                            subDisposable.ifPresent(Disposable::dispose);
                            subDisposable.clear();
                        }

                        @Override
                        public void onComplete() {
                            noMoreEventStreams.set(true);
                        }
                    }));
                    
                    return masterDisposable.get();
                }
            });
            
        }
        
        return ret;
    }
    
    static <T> Observable<T> concat(final Observable<T>... eventStreams) {
        final Observable<T> ret;
        
        if (eventStreams == null) {
            ret = Observable.empty();
        } else {
            ret = Observable.flatten(Observable.of(eventStreams));
        }
        
        return ret;
    }
}

