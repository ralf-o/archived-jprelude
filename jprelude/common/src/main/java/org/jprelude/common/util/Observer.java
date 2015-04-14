package org.jprelude.common.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jprelude.common.function.Command;

public interface Observer<T> {
    void onNext(T item);
    void onError(Throwable throwable);
    void onComplete();
    void onSubscribe(Subscription subscription);
    
    static <T> Observer<T> create(final Consumer<T> onNext) {
        return Observer.create(onNext, null, null, null);
    }

    static <T> Observer<T> create(final Consumer<T> onNext, final Command onComplete) {
        return Observer.create(onNext, onComplete, null, null);
    }

    static <T> Observer<T> create(final Consumer<T> onNext, final Consumer<Throwable> onError) {
        return Observer.create(onNext, null, onError, null);
    }
    
    static <T> Observer<T> create(final Consumer<T> onNext, final Command onComplete, final Consumer<Throwable> onError) {
        return Observer.create(onNext, onComplete, onError, null);
    }
    
    static <T> Observer<T> create(final Consumer<T> onNext, final Command onComplete, final Consumer<Throwable> onError, final Consumer<Subscription> onSubscribe) {
        return Observer.builder()
                .onNext(onNext)
                .onComplete(onComplete)
                .onError(onError)
                .onSubscribe(onSubscribe)
                .build();
    }
    
    static Builder builder() {
        return new Builder();
    }
    
    static class Builder<T> {
        private BiConsumer<T, Long> onNextWithIndex;
        private Consumer<Long> onCompleteWithIndex;
        private BiConsumer<Throwable, Long> onErrorWithIndex;
        private Consumer<Subscription> onSubscribe;
        
        public Builder onNext(final Consumer<T> consumer) {
            this.onNextWithIndex = consumer == null
                    ? null
                    : (item, idx) -> consumer.accept(item);
            
            return this;
        }

        public Builder onNext(final BiConsumer<T, Long> consumer) {
            this.onNextWithIndex = consumer;
            return this;
        }

        public Builder onComplete(final Command command) {
            this.onCompleteWithIndex = command == null
                    ? null
                    : idx -> command.execute();
            
            return this;
        }
        
        public Builder onComplete(final Consumer<Long> consumer) {
            this.onCompleteWithIndex = consumer;
            return this;
        }

        public Builder onError(final Consumer<Throwable> consumer) {
            this.onErrorWithIndex = consumer == null
                    ? null
                    : (throwable, idx) -> consumer.accept(throwable);
            
            return this;
        }

        public Builder onError(final BiConsumer<Throwable, Long> consumer) {
            this.onErrorWithIndex = consumer;
            return this;
        }

        public Builder onSubscribe(final Consumer<Subscription> consumer) {
            this.onSubscribe = consumer;
            return this;
        }

        Observer<T> build() {
            return new Observer<T>() {
                private final BiConsumer<T, Long> onNextWithIndex = Builder.this.onNextWithIndex;
                private final Consumer<Long> onCompleteWithIndex = Builder.this.onCompleteWithIndex;
                private final BiConsumer<Throwable, Long> onErrorWithIndex = Builder.this.onErrorWithIndex;
                private final Consumer<Subscription> onSubscribe  = Builder.this.onSubscribe;
                private long index = -1;
                
                @Override
                public void onNext(final T item) {
                    if (this.onNextWithIndex != null) {
                        this.onNextWithIndex.accept(item, ++this.index);
                    }
                }

                @Override
                public void onError(final Throwable throwable) {
                    if (this.onErrorWithIndex != null) {
                        this.onErrorWithIndex.accept(throwable, this.index);
                    }
                }

                @Override
                public void onComplete() {
                    if (this.onCompleteWithIndex != null) {
                        this.onCompleteWithIndex.accept(this.index);
                    }
                }

                @Override
                public void onSubscribe(final Subscription subscription) {
                    if (this.onSubscribe != null) {
                        this.onSubscribe.accept(subscription);
                    }
                }
            };
        }
    }
}
