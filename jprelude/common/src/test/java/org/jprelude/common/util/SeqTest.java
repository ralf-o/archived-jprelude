package org.jprelude.common.util;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class SeqTest {
    @Test
    public void testFactories() {
       Seq.of(1, 2, 3).toList().equals(Arrays.asList(1, 2, 3));
    }
    
    @Test
    public void testMethodMap() {
        final Seq<Integer> seq = Seq.of(1, 2, 3)
            .map((x, i) -> x * 2 + i);

        Assert.assertArrayEquals(seq.toArray(), new Integer[] {2, 5, 8});
    }
    
    @Test
    public void testMethodFlatMap() {
        final Seq<Integer> seq = Seq.of(1, 2, 3)
            .flatMap(n -> Seq.of(n * 10, n * 10 + 1, n * 10 + 2));
        
        Assert.assertArrayEquals(seq.toArray(), new Integer[] {10, 11, 12, 20, 21, 22, 30, 31, 32});
    }
    
    @Test
    public void testMethodFilter() {
        final Seq<Integer> seq = Seq.of(1, 2, 3, 4, 5, 6)
            .filter((n, idx) -> idx % 3 == 0);
        
        Assert.assertArrayEquals(seq.toArray(), new Integer[] {1, 4});
    }
}
