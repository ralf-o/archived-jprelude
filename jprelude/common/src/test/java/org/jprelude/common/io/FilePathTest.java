package org.jprelude.common.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import org.junit.Test;

public class FilePathTest {
    //@Test
    public void testSomething() {
        FilePath.from(FileSystems.getDefault().getPath("/home/kenny/Downloads"))
                .list()
                .forEach(System.out::println);
    }
    
    @Test
    public void testSomethingElese() throws IOException {
        Files.newDirectoryStream(FilePath.from(new File("/home/kenny/Downloads")), file -> true);
    }
}
