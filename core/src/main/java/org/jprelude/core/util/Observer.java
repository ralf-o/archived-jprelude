package org.jprelude.core.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jprelude.core.function.Command;

public interface Observer<T> {
    void onNext(T item);
    void onError(Throwable throwable);
    void onComplete();
    
    static <T> Observer<T> create(final Consumer<T> onNext) {
        return Observer.create(onNext, null, null);
    }

    static <T> Observer<T> create(final Consumer<T> onNext, final Command onComplete) {
        return Observer.create(onNext, onComplete, null);
    }

    static <T> Observer<T> create(final Consumer<T> onNext, final Consumer<Throwable> onError) {
        return Observer.create(onNext, null, onError);
    }
    
    static <T> Observer<T> create(final Consumer<T> onNext, final Command onComplete, final Consumer<Throwable> onError) {
        return Observer.builder()
                .onNext(onNext)
                .onComplete(onComplete)
                .onError(onError)
                .build();
    }
    
    static Builder builder() {
        return new Builder();
    }
    
    static class Builder<T> {
        private BiConsumer<? super T, ? super Long> onNextWithIndex;
        private Consumer<? super Long> onCompleteWithIndex;
        private BiConsumer<? super Throwable, ? super Long> onErrorWithIndex;
        
        public Builder onNext(final Consumer<? super T> consumer) {
            this.onNextWithIndex = consumer == null
                    ? null
                    : (item, idx) -> consumer.accept(item);
            
            return this;
        }
        
        public Builder onNextx(final BiConsumer<? super T, ? super Long> consumer) {
            this.onNextWithIndex = consumer;
            return this;
        }

        public Builder onComplete(final Command command) {
            this.onCompleteWithIndex = command == null
                    ? null
                    : idx -> command.execute();
            
            return this;
        }
        
        public Builder onComplete(final Consumer<? super Long> consumer) {
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

        public Observer<T> build() {
            return new Observer<T>() {
                private final BiConsumer<? super T, ? super Long> onNextWithIndex = Builder.this.onNextWithIndex;
                private final Consumer<? super Long> onCompleteWithIndex = Builder.this.onCompleteWithIndex;
                private final BiConsumer<? super Throwable, ? super Long> onErrorWithIndex = Builder.this.onErrorWithIndex;
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
            };
        }
    }
}
