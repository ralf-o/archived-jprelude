package org.jprelude.common.util;

public interface Subscription {
    void cancel();
    void request(long n);
    
    default void requestAll() {
        this.request(Long.MAX_VALUE);
    }
}
