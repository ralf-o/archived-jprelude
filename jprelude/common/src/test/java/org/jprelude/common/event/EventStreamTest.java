package org.jprelude.common.event;

import org.jprelude.common.util.Seq;
import org.junit.Test;

public class EventStreamTest {
    @Test
    public void testEventStream() {
        Seq.range(0, 10).toEventStream().map(n -> n * n).subscribe(System.out::println);
    }
}
