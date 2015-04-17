package org.jprelude.core.util.tuple;

public interface Triple<T1, T2, T3> {
  T1 getFirst();
  
  T2 getSecond();
  
  T3 getThird();

  public static <T1, T2, T3> Triple<T1, T2, T3> of(final T1 first, final T2 second, final T3 third) {
    return new Triple<T1, T2, T3>() {
        @Override
        public T1 getFirst() {
            return first;
        }

        @Override
        public T2 getSecond() {
            return second;
        }
        
        @Override
        public T3 getThird() {
            return third;
        }

        @Override
        public String toString() {
            return "(" + first + ", " + second + ", " + third + ")";
        }
    };
  }
}