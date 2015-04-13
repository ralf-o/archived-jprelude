package org.jprelude.common.util;

import org.junit.Test;

public class ObservableTest {
    @Test
    public void testEventStream() {
        Observable<Integer> str1 = Seq.range(1, 5).toObservable();
        Observable<Integer> str2 = Seq.range(5, 10).toObservable();
        Observable<Integer> str3 = Seq.range(10, 15).toObservable();
        
        Observable<Observable<Integer>> combined = Observable.of(str1, str2, str3);
        Observable.flatten(combined).subscribe(System.out::println);
        //Seq.range(1, 11).toEventStream().map(n -> n * n).subscribe(System.out::println);
    
        Observable.range(1, 100).takeWhile(n -> {if (n == 30) throw new RuntimeException("Juhuu"); return n < 50;}).subscribe(System.out::println);
    }
}
