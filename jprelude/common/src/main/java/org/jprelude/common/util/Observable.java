package org.jprelude.common.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jprelude.common.function.Command;

public interface Observable<T> {
    Subscription subscribe(final Observer<T> observer);
    

    default Subscription subscribe(final Consumer<T> onNext, final Command onComplete, final Consumer<Throwable> onError, final Consumer<Subscription> onSubscribe) {
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
            }
            
            @Override
            public void onError(final Throwable throwable) {
                if (onError != null) {
                    onError.accept(throwable);
                } else {
                    if (throwable instanceof RuntimeException) {
                        throw (RuntimeException) throwable;
                    } else {
                        throw new RuntimeException(throwable);
                    }
                }
            }

            @Override
            public void onSubscribe(Subscription subscription) {
                if (onSubscribe != null) {
                    onSubscribe.accept(subscription);
                }
            }
        };
        
        return this.subscribe(observer);
    }
    
    default Subscription subscribe(final Consumer<T> onNext) {
        return this.subscribe(onNext, null, null, null);
    } 
    
    default Subscription subscribe(final Consumer<T> onNext, final Command onComplete) {
        return this.subscribe(onNext, onComplete, null, null);
    }
    
    default Subscription subscribe(final Consumer<T> onNext, final Consumer<Throwable> onError) {
        return this.subscribe(onNext, null, onError, null);
    }
    
     default Subscription subscribe(final Consumer<T> onNext, final Command onComplete, final Consumer<Throwable> onError) {
        return this.subscribe(onNext, onComplete, onError, null);
    }
    
    default <R> Observable<R> map(final Function<T, R> f) {
        Objects.requireNonNull(f);
        return this.map((item, idx) -> f.apply(item));
    }

    default <R> Observable<R> map(final BiFunction<T, Long, R> f) {
        Objects.requireNonNull(f);
        
        return Observable.create(observer -> new Subscription() {
            private Subscription subscription = null;
            private boolean isCancelled = false;
            private long counter = 0;
           
            @Override
            public void cancel() {
                this.isCancelled = true;
               
                if (this.subscription != null) {
                    final Subscription theSubscription = this.subscription;
                    this.subscription = null;
                    theSubscription.cancel();
                }
            }

            @Override
            public void request(long n) {
                if (n > 0 && !this.isCancelled) {
                    if (this.subscription == null) {
                        this.subscription = Observable.this.subscribe(new Observer<T>() {
                            @Override
                            public void onNext(final T item) {
                                try {
                                    observer.onNext(f.apply(item, counter++));
                                } catch (final Throwable throwable) {
                                    final Subscription theSubscription = subscription;
                                    subscription = null;
                                    theSubscription.cancel();
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

                            @Override
                            public void onSubscribe(Subscription subscription) {
                            }
                       });
                    }
                    
                    this.subscription.request(n);
               }
           }
       });  
    };

    default Observable<T> flatMap(final Function<T, Observable<T>> f) {
        Objects.requireNonNull(f);
        
        return Observable.flatten(this.map(f));
    }

    default Observable<T> filter(final Predicate<T> pred) {
        Objects.requireNonNull(pred);
        return this.filter((item, idx) -> pred.test(item));
    }
 
    default Observable<T> filter(final BiPredicate<T, Long> pred) {
        Objects.requireNonNull(pred);
        
        return Observable.create(observer -> new Subscription() {
            private Subscription subscription = null;
            private boolean isCancelled = false;
            private long counter = 0;
           
            @Override
            public void cancel() {
                this.isCancelled = true;
               
                if (this.subscription != null) {
                    final Subscription theSubscription = this.subscription;
                    this.subscription = null;
                    theSubscription.cancel();
                }
            }

            @Override
            public void request(long n) {
                if (n > 0 && !this.isCancelled) {
                    if (this.subscription == null) {
                        this.subscription = Observable.this.subscribe(new Observer<T>() {
                            @Override
                            public void onNext(final T item) {
                                try {
                                    if (pred.test(item, counter++)) {
                                        observer.onNext(item);
                                    } else {
                                        subscription.request(1);
                                    }
                                } catch (final Throwable throwable) {
                                    final Subscription theSubscription = subscription;
                                    subscription = null;
                                    theSubscription.cancel();
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

                            @Override
                            public void onSubscribe(Subscription subscription) {
                            }
                       });
                    }
                    
                    this.subscription.request(n);
               }
           }
       });  
    };
    
    default Observable<T> reject(final Predicate<T> pred) {
        Objects.requireNonNull(pred);
        return this.filter(item -> !pred.test(item));
    }    

    default Observable<T> reject(final BiPredicate<T, Long> pred) {
        Objects.requireNonNull(pred);
        return this.filter((item, idx) -> !pred.test(item, idx));
    }    

    
    default Observable<T> take(final long n) {
        return this.takeWhile((item, idx) -> idx < n);
    }
    
    default Observable<T> takeWhile(final Predicate<T> pred) {
        return this.takeWhile((item, idx) -> pred.test(item));
    }
    
    default Observable<T> takeWhile(final BiPredicate<T, Long> pred) {
        return Observable.create(observer -> new Subscription() {
            private Subscription subscription = null;
            private boolean isCancelled = false;
            private long counter = 0;
           
            @Override
            public void cancel() {
                this.isCancelled = true;
               
                if (this.subscription != null) {
                    final Subscription theSubscription = this.subscription;
                    this.subscription = null;
                    theSubscription.cancel();
                }
            }

            @Override
            public void request(long n) {
                if (n > 0 && !this.isCancelled) {
                    if (this.subscription == null) {
                        this.subscription = Observable.this.subscribe(new Observer<T>() {
                            @Override
                            public void onNext(final T item) {
                                try {
                                    if (pred.test(item, counter)) {
                                        ++counter;
                                        observer.onNext(item);
                                    } else {
                                        final Subscription theSubscription = subscription;
                                        subscription = null;
                                        theSubscription.cancel();
                                    }
                                } catch (final Throwable throwable) {
                                    final Subscription theSubscription = subscription;
                                    subscription = null;
                                    theSubscription.cancel();
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

                            @Override
                            public void onSubscribe(Subscription subscription) {
                            }
                       });
                    }
                    
                    this.subscription.request(n);
               }
           }
       });  
    };
    
    default Observable<T> skip(final long n) {
        return this.skipWhile((item, idx) -> idx < n);
    }
    
    default Observable<T> skipWhile(final Predicate<T> pred) {
        Objects.requireNonNull(pred);

        return this.skipWhile((item, idx) -> pred.test(item));
    }

    default Observable<T> skipWhile(final BiPredicate<T, Long> pred) {
        return Observable.create(observer -> new Subscription() {
            private Subscription subscription = null;
            private boolean isCancelled = false;
            private long counter = 0;
            private boolean hasStarted = false;
           
            @Override
            public void cancel() {
                this.isCancelled = true;
               
                if (this.subscription != null) {
                    final Subscription theSubscription = this.subscription;
                    this.subscription = null;
                    theSubscription.cancel();
                }
            }

            @Override
            public void request(long n) {
                if (n > 0 && !this.isCancelled) {
                    if (this.subscription == null) {
                        this.subscription = Observable.this.subscribe(new Observer<T>() {
                            @Override
                            public void onNext(final T item) {
                                try {
                                    if (hasStarted || !pred.test(item, counter)) {
                                        hasStarted = true;
                                        observer.onNext(item);
                                    } else {
                                        subscription.request(1);
                                    }
                                } catch (final Throwable throwable) {
                                    subscription.cancel();
                                    observer.onError(throwable);
                                }
                                
                                ++counter;
                            }
 
                            @Override
                            public void onError(Throwable throwable) {
                                observer.onError(throwable);
                            }

                            @Override
                            public void onComplete() {
                                observer.onComplete();
                            }

                            @Override
                            public void onSubscribe(Subscription subscription) {
                            }
                       });
                    }
                    
                    this.subscription.request(n);
               }
           }
       });  
    };

    
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

    default void forEach(final Observer<T> observer) {
        if (observer != null) {
            this.subscribe(observer).requestAll();
        }
    }

   default void forEach(final Consumer<T> onNext) {
        if (onNext != null) {
            this.subscribe(onNext).requestAll();
        }
    }
    
    default void forEach(final Consumer<T> onNext, final Command onComplete) {
        if (onNext != null) {
            this.subscribe(onNext, onComplete).requestAll();
        }
    }

    default void forEach(final Consumer<T> onNext, final Consumer<Throwable> onError) {
        if (onNext != null) {
            this.subscribe(onNext, onError).requestAll();
        }
    }

    default void forEach(final Consumer<T> onNext, final Command onComplete, final Consumer<Throwable> onError) {
        if (onNext != null) {
            this.subscribe(onNext, onComplete, onError).requestAll();
        }
    }

    static <T> Observable<T> concat(final Observable<T>... observables) {
        final Observable<T> ret;
        
        if (observables == null) {
            ret = Observable.empty();
        } else {
            ret = Observable.flatten(Observable.of(observables));
        }
        
        return ret;
    }

    static <T> Observable<T> flatten(final Observable<Observable<T>> observables) {
        return Observable.create(observer -> new Subscription() {
            private Subscription masterSubscription = null;
            private Subscription subSubscription = null;
            private boolean noMoreObservables = false;
            private boolean isCancelled = false;
            private long openItemCount = 0;
            
            
            @Override
            public void cancel() {
                this.isCancelled = true;
                this.noMoreObservables = true;
               
                if (this.masterSubscription != null) {
                    final Subscription theMasterSubscription = this.masterSubscription;
                    this.masterSubscription = null;
                    theMasterSubscription.cancel();
                }

                if (this.subSubscription != null) {
                    final Subscription theSubSubscription = this.subSubscription;
                    this.subSubscription = null;
                    theSubSubscription.cancel();
                }                
            }

            @Override
            public void request(long n) {System.out.println("=>> " + n);
                if (n > 0 && !this.isCancelled && this.subSubscription != null) {
                    this.openItemCount += n;
                } else if (n > 0 && !this.isCancelled) {
                   this.openItemCount += n;
                   
                   if (this.masterSubscription == null) {
                        this.masterSubscription = observables.subscribe(new Observer<Observable<T>>() {
                            @Override
                            public void onNext(final Observable<T> observable) {
                                try {
                                    if (subSubscription != null) {
                                         assert false : "This shoud never happen";
                                    } else {
                                        subSubscription = observable.subscribe(new Observer<T>() {
                                            @Override
                                            public void onNext(T item) {
                                                --openItemCount;
                                                observer.onNext(item);
                                            }

                                            @Override
                                            public void onError(Throwable throwable) {
                                                observer.onError(throwable);
                                                noMoreObservables = true;
                                                cancel();
                                            }

                                            @Override
                                            public void onComplete() {
                                                subSubscription = null;
                                                
                                                if (noMoreObservables) {
                                                    observer.onComplete();
                                                    cancel();
                                                } else {
                                                    if (openItemCount > 0) {
                                                        masterSubscription.request(1);
                                                    }
                                                }
                                            }
                                            
                                            @Override
                                            public void onSubscribe(Subscription subscription) {
                                               observer.onSubscribe(subscription);
                                            }
                                        });
                                    
                                        if (openItemCount > 0) {
                                            subSubscription.request(openItemCount);
                                        }
                                    }     
                                } catch (final Throwable throwable) {
                                    cancel();
                                    observer.onError(throwable);
                                }
                            }
 
                            @Override
                            public void onError(Throwable throwable) {
                                cancel();
                                observer.onError(throwable);
                            }

                            @Override
                            public void onComplete() {
                                cancel();
                                observer.onComplete();
                            }

                            @Override
                            public void onSubscribe(Subscription subscription) {
                                observer.onSubscribe(subscription);
                            }
                       });
                    }
                   
                    this.masterSubscription.request(1);
               }
           }
       });  
    };

    static <T> Observable<T> create(final Function<Observer<T>, Subscription> onSubscribe) {
        Objects.requireNonNull(onSubscribe);
        
        return observer -> {
            Objects.requireNonNull(observer);
            
            final Subscription subscription = onSubscribe.apply(observer);
            observer.onSubscribe(subscription);
            return subscription;
        };
    }
    
    static <T> Observable<T> empty() {
        return Observable.create(observer -> {
            final Subscription subscription = new Subscription() {
                @Override
                public void cancel() {
               }

               @Override
               public void request(long n) {
               }
            };
            
            observer.onSubscribe(subscription);
            observer.onComplete();
            return subscription;
        });
    }
    
    static <T> Observable<T> of(final T... items) {
        return items == null || items.length == 0
            ? Observable.empty()
            : Seq.of(items).toObservable();
    }
    
    static <T> Observable<T> from(final Iterable<T> items) {
        return items == null
            ? Observable.empty()
            : Seq.from(items).toObservable();
    }
    
    public static Observable<Integer> range(final int start, final int end) {
        return Seq.range(start, end).toObservable();
    }
    
    public static Observable<Long> range(final Long start, final Long end) {
        return Seq.range(start, end).toObservable();
    }    
    
    static <T> Observable<T> from(final Seq<T> seq) {
        return seq == null
            ? Observable.empty()
            :Observable.create(observer -> new Subscription() {
                private boolean isCompleted = false;
                private Stream<T> stream = null;
                private Iterator<T> iterator = null;

                @Override
                public void cancel() {
                    if (this.stream != null) {
                       final Stream<T> theStream;
                       theStream = this.stream;
                       this.isCompleted = true;
                       this.iterator = null;	
                       this.stream = null;
                       theStream.close();
                    }
                }

                 @Override
                 public void request(long n) {
                    if (n > 0 && !this.isCompleted) {
                        try {
                            if (stream == null) {
                                this.stream = seq.stream();
                                this.iterator = stream.iterator();
                            }

                            long counter = 0L;

                            for (long i = 0; iterator!= null && iterator.hasNext() && i < n; ++i) {
                                observer.onNext(iterator.next());
                            }

                            if (iterator != null && !iterator.hasNext()) {
                                this.stream = null;
                                this.iterator = null;
                                observer.onComplete();
                            }
                        } catch (final Throwable throwable) {
                            if (this.stream != null) {
                               this.stream.close();
                            }

                            this.stream = null;
                            this.iterator = null;
                            observer.onError(throwable);
                        }
                    }
                }
            });
    }
}

