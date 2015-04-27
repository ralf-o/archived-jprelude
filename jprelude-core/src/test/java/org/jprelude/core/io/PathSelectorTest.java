package org.jprelude.core.io;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import org.jprelude.core.util.Seq;
import org.junit.Test;

public class PathSelectorTest {
    @Test
    public void testSomething() throws Exception {  
       final Seq<Path> files = PathSelector.builder()
               .recursive(dir -> !dir.matches("**/*report*"))               
               .include(file -> file.isRegularFile() && file.matches("**/*.java") &&  file.getModifiedTime() != null)
               //.maxDepth(3)
               //.exclude(file -> file.byName().matches("*tmp*"))
               .exclude(file -> file.getElapsedTimeCreation(ChronoUnit.HOURS) >= 1)
               .skipInaccessableSubdirectories(true)
               .build()
               .list(Paths.get("./"));
       files.forEach(System.out::println);
       
    }
}
