package org.jprelude.core.tuple;

public interface Quadruple<T1, T2, T3, T4> {
  T1 getFirst();
  
  T2 getSecond();
  
  T3 getThird();
  
  T4 getFourth();

  public static <T1, T2, T3, T4> Quadruple<T1, T2, T3, T4> of(final T1 first, final T2 second, final T3 third, final T4 fourth) {
    return new Quadruple<T1, T2, T3, T4>() {
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
        public T4 getFourth() {
            return fourth;
        }

        @Override
        public String toString() {
            return "(" + first + ", " + second + ", " + third + ")";
        }
    };
  }
}