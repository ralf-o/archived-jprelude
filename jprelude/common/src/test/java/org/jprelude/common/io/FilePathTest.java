package org.jprelude.common.io;

import java.nio.file.FileSystems;
import org.junit.Test;

public class FilePathTest {
    // @Test
    public void testSomething() {
        FilePath.from(FileSystems.getDefault().getPath("~/Downloads"))
                .list()
                .forEach(System.out::println);
    }
}
