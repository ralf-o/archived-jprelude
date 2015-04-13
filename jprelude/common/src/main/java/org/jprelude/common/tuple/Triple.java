package org.jprelude.common.tuple;

public interface Triple<T1, T2, T3> {
  T1 getFirst();
  
  T2 getSecond();
  
  T3 getThrid();

  public static <T1, T2, T3> Triple<T1, T2, T3> of(final T1 first, final T2 second, final T3 thrid) {
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
        public T3 getThrid() {
            return thrid;
        }
    };
  }
}