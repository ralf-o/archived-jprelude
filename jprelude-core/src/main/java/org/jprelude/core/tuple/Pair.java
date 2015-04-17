package org.jprelude.core.tuple;

public interface Pair<T1, T2> {
  T1 getFirst();
  
  T2 getSecond();

  public static <T1, T2> Pair<T1, T2> of(final T1 first, final T2 second) {
    return new Pair<T1, T2>() {
        @Override
        public T1 getFirst() {
            return first;
        }

        @Override
        public T2 getSecond() {
            return second;
        }
        
        @Override
        public String toString() {
            return "(" + first + ", " + second + ")";
        }
    };
  }
}