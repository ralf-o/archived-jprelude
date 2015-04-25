package org.jprelude.core.io;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Objects;
import org.jprelude.core.util.Seq;

public interface PathEntry {
    Path getPath();

    LinkOption[] getDefaultLinkOptions();
    
    default boolean isRegularFile() {
        return Files.isRegularFile(this.getPath(), this.getDefaultLinkOptions());
    }
    
    default boolean isDirectory() {
        return Files.isDirectory(this.getPath(), this.getDefaultLinkOptions());
    }
    
    default boolean isSymbolicLink() {
        return Files.isSymbolicLink((this.getPath()));
    }
    
    default PathEntry byName() {
        return new PathEntry() {
            @Override
            public Path getPath() {
                return PathEntry.this.getPath().getFileName();
            }

            @Override
            public LinkOption[] getDefaultLinkOptions() {
                return PathEntry.this.getDefaultLinkOptions();
            }
        };
    }
    
    default boolean matches(final String... patterns) {
        final Path path = this.getPath();
        
        return Seq.of(patterns)
                .rejectNulls()
                .anyMatch(pattern ->
                        path.getFileSystem()
                                .getPathMatcher("glob:" + pattern)
                                .matches(path)); 
    }   
 }
