package org.jprelude.core.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jprelude.core.util.Seq;
import org.junit.Test;

public class PathListerTest {
    @Test
    public void testSomething() {
        final Seq<Path> paths = PathLister
                .createRecursive(Files::isRegularFile)
                .list(Paths.get("/home/kenny/Desktop"));
     
        
        
       final Seq<Path> sortedPaths = paths.sorted((path1, path2) -> path1.toUri().compareTo(path2.toUri()));
       sortedPaths.forEach(System.out::println);
  
        // this.maxDepth = builder.maxDepth;
        
        /*
        sortedPathInfos.forEach(
                    IOConsumer.<PathInfo>unchecked(info -> 
                        System.out.println(info.getFullName()+ "   -> " + info.getAge(TimeUnit.HOURS))
                    ));
       */
        System.out.println("-----------");
       
        PathLister.builder()
                .recursive()
                .maxDepth(3)
                .build()
                .list(Paths.get("/home/kenny/Desktop"));
              //  .forEach(System.out::println);
    }
}
