package org.jprelude.core.io;


import java.nio.file.Paths;
import org.junit.Test;

public class TextReaderTest {
    @Test
    public void testSomething() {
        TextReader.forFile(Paths.get("./src/test/java/org/jprelude/core/io/TextReaderTest.java"))
                .readLines()
                .toStringList()
                .forEach(System.out::println);
    }
}
