package org.jprelude.common.util;

import org.jprelude.common.util.SeqObservable;
import org.jprelude.common.util.Seq;
import org.junit.Test;

public class SeqObservableTest {
    @Test
    public void testEventStream() {
        SeqObservable<Integer> str1 = Seq.range(1, 5).toObservable();
        SeqObservable<Integer> str2 = Seq.range(5, 10).toObservable();
        SeqObservable<Integer> str3 = Seq.range(10, 15).toObservable();
        
        SeqObservable<SeqObservable<Integer>> combined = SeqObservable.of(str1, str2, str3);
        SeqObservable.flatten(combined).subscribe(System.out::println);
        //Seq.range(1, 11).toEventStream().map(n -> n * n).subscribe(System.out::println);
    }
}
