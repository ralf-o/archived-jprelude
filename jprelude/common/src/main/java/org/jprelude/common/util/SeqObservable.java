package org.jprelude.common.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jprelude.common.function.Command;

public interface SeqObservable<T> {
    Disposable subscribe(final SeqObserver<T> observer);
    
    default Disposable subscribe(final Consumer<T> onNext, final Command onComplete, final Consumer<Throwable> onError) {
        final SeqObserver<T> observer = new SeqObserver<T>() {
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
    
    static <T> SeqObservable<T> create(final Function<SeqObserver<T>, Disposable> onSubscribe) {
        Objects.requireNonNull(onSubscribe);
        
        return new SeqObservable<T>() {         
            @Override
            public Disposable subscribe(final SeqObserver<T> observer) {
                Objects.requireNonNull(observer);
                final Disposable disposable = onSubscribe.apply(observer);
                return disposable::dispose;
            }
        };
    }
    
    default <R> SeqObservable<R> map(Function<? super T, ? extends R> f) {
        return SeqObservable.create(
            observer -> this.subscribe(
                v -> observer.onNext(f.apply(v)),
                () -> observer.onComplete(),
                t -> observer.onError(t)
            )
        );
    }        

    default SeqObservable<T> prepend(final T value) {
        return SeqObservable.concat(SeqObservable.of(value), this);
    }

    static <T> SeqObservable<T> from(final Seq<T> seq) {
        Objects.requireNonNull(seq);
        
        return SeqObservable.create(observer -> {
            if (observer != null) {
                try (final Stream<T> stream = StreamUtils.sequential(seq.stream())) {
                    stream.forEach(val -> observer.onNext(val));
                    observer.onComplete();
                    System.out.println("Seq-complete");
                } catch (final Throwable t) {
                    observer.onError(t);
                }
            }
            
            return () -> {};
        });
    }
    
    static <T> SeqObservable<T> empty() {
        return SeqObservable.create(observer -> {
            if (observer != null) {
                observer.onComplete();
            }
            
            return () -> {};
        });
    }
    
    static <T> SeqObservable<T> of(final T... items) {
        return Seq.of(items).toObservable();
    }
    
    static <T> SeqObservable<T> from(final Iterable<T> items) {
        return Seq.from(items).toObservable();
    }
    
    static <T> SeqObservable<T> flatten(final SeqObservable<SeqObservable<T>> eventStreams) {
        final SeqObservable<T> ret;
        
        // TODO: Ugly implementation - find a better solution
        
        if (eventStreams == null) {
            ret = SeqObservable.empty();
        } else {
            ret = SeqObservable.create(new Function<SeqObserver<T>, Disposable>() {
                final Queue<SeqObservable<T>> eventStreamQueue = new LinkedList<>();
                final Boolean[] noMoreEventStreamsWrapper = {false};
                final Disposable[] subDisposableWrapper = {null};
                
                @Override
                public Disposable apply(final SeqObserver<T> observer) {
                    final Disposable[] masterDisposableWrapper = {null};
                    
                    masterDisposableWrapper[0] = eventStreams.subscribe(new SeqObserver<SeqObservable<T>>() {
                        @Override
                        public void onNext(final SeqObservable<T> eventStream) {
                            System.out.println("=>>>>>> " + subDisposableWrapper[0]);
                            if (subDisposableWrapper[0] != null) {
                                System.out.println(eventStream);
                                eventStreamQueue.add(eventStream);
                            } else {
                                System.out.println("Setting subDisposable");
                                subDisposableWrapper[0] = eventStream.subscribe(new SeqObserver<T>() {
                                    @Override
                                    public void onNext(T item) {
                                        observer.onNext(item);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        observer.onError(throwable);
                                        noMoreEventStreamsWrapper[0] = true;
                                        
                                        if (masterDisposableWrapper[0] !=null) {
                                            masterDisposableWrapper[0].dispose();
                                            masterDisposableWrapper[0] = null;
                                        }
                                    }

                                    @Override
                                    public void onComplete() {
                                        System.out.println("Unsetting subDisposable");
                                        subDisposableWrapper[0] = null;
                                        
                                        System.out.println("sub-complete");
                                        if (!eventStreamQueue.isEmpty()) {
                                            System.out.println("Setting subDisposable2");
                                             subDisposableWrapper[0] = eventStreamQueue.poll().subscribe(this);                                            
                                        } else if (noMoreEventStreamsWrapper[0]) {
                                            observer.onComplete();
                                        }
                                    }                                    
                                });
                            }
                        }

                        @Override
                        public void onError(final Throwable throwable) {
                            observer.onError(throwable);
                            eventStreamQueue.clear();
                            noMoreEventStreamsWrapper[0] = true;
                            
                            if (subDisposableWrapper[0] != null) {
                                subDisposableWrapper[0].dispose();
                                noMoreEventStreamsWrapper[0] = null;
                            }
                        }

                        @Override
                        public void onComplete() {
                            noMoreEventStreamsWrapper[0] = true;
                            System.out.println("master-complete");
                        }
                    });
                    
                    return masterDisposableWrapper[0];
                }
            });
            
        }
        
        return ret;
    }
    
    static <T, R> SeqObservable<R> flatMap(final SeqObservable<T> eventStream, Function<T, SeqObservable<R>> f) {
        final SeqObservable<R> ret;
        
        if (eventStream == null) {
            ret = SeqObservable.empty();
        } else {
            ret = SeqObservable.flatten(eventStream.map(f));
        }
        
        return ret;        
    }
    
    static <T> SeqObservable<T> concat(final SeqObservable<T>... eventStreams) {
        final SeqObservable<T> ret;
        
        if (eventStreams == null) {
            ret = SeqObservable.empty();
        } else {
            ret = SeqObservable.flatMap(SeqObservable.from(Arrays.asList(eventStreams)), Function.identity());
        }
        
        return ret;
    }
}

