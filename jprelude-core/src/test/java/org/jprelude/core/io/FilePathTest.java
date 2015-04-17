package org.jprelude.core.io;

import java.nio.file.FileSystems;
import org.junit.Test;

public class FilePathTest {
    @Test
    public void testSomething() {
        PathInfo.from(FileSystems.getDefault().getPath("/home/kenny/Downloads"))
                .list()
                .forEach(test -> System.out.println(test.toUri()));
    }
    
}
