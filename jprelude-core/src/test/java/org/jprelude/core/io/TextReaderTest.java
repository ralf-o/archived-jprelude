package org.jprelude.core.io;


import java.io.File;
import static org.jprelude.core.util.Seq.from;
import org.junit.Test;

public class TextReaderTest {
    @Test
    public void testSomething() {
        TextReader.from(new File("./src/test/java/org/jprelude/core/io/TextReaderTest.java"))
                .readLines()
                .toStringList()
                .forEach(System.out::println);
    }
}
