package org.jprelude.common.util;

import java.util.*;

public abstract class Generator<T> implements Iterator<T>, AutoCloseable {
  private boolean isInitialized = false;
  private final Queue<T> nextValues = new LinkedList<>();
  private boolean reachedEnd = false;
  private boolean isDisposed = false;
  
  
  protected final void yield(final T value) {
      this.nextValues.add(value);
  }
  
  protected final void yieldMany(T... values) {
      if (values != null) {
          for (final T value : values) {
              this.nextValues.add(value);
          }
      }
  }
  
  protected final void yieldMany(final Collection<T> values) {
      if (values != null) {
          this.nextValues.addAll(values);
      }
  }
  
  protected abstract void generate() throws Throwable;
  
  protected void init() throws Throwable {
  };
  
  @Override
  public final boolean hasNext() {
    boolean ret = false;
    
    if (!this.reachedEnd) {
      if (!this.isInitialized) {
          try {
              this.init();
          } catch (final Throwable throwable) {
              try {
                  this.close();
              } catch (final Throwable ignore) {
                  // ignore
              }
              
              throw new RuntimeException(throwable); 
          }
          
          this.isInitialized = true;
      }
        
      if (!this.nextValues.isEmpty()) {
        ret = true;
      } else {
        try {
          this.generate();
          this.reachedEnd = this.nextValues.isEmpty();
          ret = !this.reachedEnd;
        } catch (final Throwable throwable) {
            try {
                this.close();
            } catch (final Exception ignore) {
                throw new RuntimeException(throwable);
            }
        }
      }
    }
    
    return ret;
  }
  
  @Override
  public final T next() {
    if (this.nextValues.isEmpty() && !this.hasNext()) {
      throw new NoSuchElementException();
    }
    
    return this.nextValues.poll();
  }

  @Override
  public final void remove() {
    throw new UnsupportedOperationException("Method 'remove' is not supperted by this iterator!");
  }
  
  @Override
  public final void close() throws Exception {
    if (!this.isDisposed) {
      this.dispose();
      this.isDisposed = true;
    }
  }
  
  protected void dispose() throws Exception {
  }
  
  @Override
  @SuppressWarnings("FinalizeDeclaration")
  protected final void finalize() throws Throwable {
    try {
      this.close();
    } finally {
      super.finalize();
    }
  }  
}