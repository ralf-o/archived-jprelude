package org.jprelude.core.util;

import java.io.IOException;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class SeqTest {
    @Test
    public void testFactories() throws IOException {
       Seq.of(1, 2, 3).toList().equals(Arrays.asList(1, 2, 3));
    }
    
    @Test
    public void testMethodMap() {
        final Seq<Long> seq = Seq.of(1, 2, 3)
            .map((x, i) -> x * 2 + i);

        Seq.from((start, blockSize) -> Seq.range(start.intValue(), blockSize.intValue() + start.intValue()).toList(), 100L).take(150)
            .parallel()
            .forEach(v ->  {synchronized (this) { System.out.println("[" + v + "]");}});
        
//        Assert.assertArrayEquals(seq.toArray(), new Integer[] {2, 5, 8});
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
    
    @Test
    public void testMethodZip() {
        final Seq<Integer> seq1 = Seq.of(1, 2, 3, 4, 5, 6);
        final Seq<Character> seq2 = Seq.of('a', 'b', 'c');
        final Seq<String> result = seq1.zip(seq2, (n, c, i) -> i + ": " + c + n);
        final String[] expected = {"0: a1", "1: b2", "2: c3"};
 
        Assert.assertArrayEquals(result.toArray(), expected);
    }
}
