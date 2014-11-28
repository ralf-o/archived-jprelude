package org.jprelude.common.util;

import java.util.*;

public abstract class Generator<T> implements Iterator<T>, AutoCloseable {
  private boolean hasNextValue = false;
  private T nextValue = null;
  private boolean reachedEnd = false;
  private boolean isDisposed = false;
  
  protected final void yield(T value) {
    this.hasNextValue = true;
    this.nextValue = value;
  }
  
  protected abstract void generate() throws Exception;
  
  @Override
  public final boolean hasNext() {
    boolean ret = false;
    
    if (!this.reachedEnd) {
      if (this.hasNextValue) {
        ret = true;
      } else {
        try {
          this.generate();
          this.reachedEnd = !this.hasNextValue;
          ret = this.hasNextValue;
        } catch (final Exception e) {
            try {
                this.close();
            } catch (final Exception ignore) {
                throw new RuntimeException(e);
            }
        }
      }
    }
    
    return ret;
  }
  
  @Override
  public final T next() {
    if (!this.hasNextValue && !this.hasNext()) {
      throw new NoSuchElementException();
    }
    
    this.hasNextValue  = false;
    return this.nextValue;
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