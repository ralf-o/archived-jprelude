package org.jprelude.core.io;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.concurrent.TimeUnit;
import org.jprelude.core.io.function.IOConsumer;
import org.jprelude.core.util.Seq;
import org.junit.Test;

public class PathRefTest {
    @Test
    public void testSomething() {
        final Seq<PathRef> pathInfos = PathRef.from(FileSystems.getDefault().getPath("/home/kenny/Desktop")).list();
        
        final Seq<PathRef> sortedPathInfos = pathInfos.sorted((info1, info2) -> info1.getFullName().compareTo(info2.getFullName()));
        
  
        // this.maxDepth = builder.maxDepth;
        
        /*
        sortedPathInfos.forEach(
                    IOConsumer.<PathInfo>unchecked(info -> 
                        System.out.println(info.getFullName()+ "   -> " + info.getAge(TimeUnit.HOURS))
                    ));
       */
        
        PathLister.builder()
                .fullRecursive()
                .maxDepth(3)
                .build()
                .list(new File("/home/kenny/Desktop").toPath())
                .forEach(System.out::println);
    }
}
