package org.jprelude.core.io;

import java.nio.file.FileSystems;
import java.util.concurrent.TimeUnit;
import org.jprelude.core.io.function.IOConsumer;
import org.junit.Test;

public class PathInfoTest {
    @Test
    public void testSomething() {
        PathInfo.from(FileSystems.getDefault().getPath("/home/kenny/Desktop"))
                .listRecursive(1)
                .forEach(
                    IOConsumer.<PathInfo>unchecked(info -> 
                        System.out.println(info.getFullName()+ "   -> " + info.getAge(TimeUnit.HOURS))
                    ));
    }
}
