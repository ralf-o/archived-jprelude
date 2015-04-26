package org.jprelude.core.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Objects;
import org.jprelude.core.util.Seq;

public interface PathEntry {
    Path path();

    LinkOption[] getDefaultLinkOptions();
    
    default boolean isRegularFile() {
        return Files.isRegularFile(this.path(), this.getDefaultLinkOptions());
    }
    
    default boolean isDirectory() {
        return Files.isDirectory(this.path(), this.getDefaultLinkOptions());
    }
    
    default boolean isSymbolicLink() {
        return Files.isSymbolicLink((this.path()));
    }
    
    default boolean isHidden() throws IOException {
        return Files.isHidden(this.path());
    }
    
    default boolean isExecutable() {
        return Files.isExecutable(this.path());
    }
    
    default boolean isReadable()  {
        return Files.isReadable(this.path());
    }

    default boolean isWritable()  {
        return Files.isWritable(this.path());
    }

    default boolean exists()  {
        return Files.exists(this.path(), this.getDefaultLinkOptions());
    }
      
    default PathEntry byName() {
        return new PathEntry() {
            @Override
            public Path path() {
                return PathEntry.this.path().getFileName();
            }

            @Override
            public LinkOption[] getDefaultLinkOptions() {
                return PathEntry.this.getDefaultLinkOptions();
            }
        };
    }
    
    default boolean matches(final String... patterns) {
        final Path path = this.path();
        
        return Seq.of(patterns)
                .rejectNulls()
                .anyMatch(pattern ->
                        path.getFileSystem()
                                .getPathMatcher("glob:" + pattern)
                                .matches(path)); 
    }
    
    default FileTime creationTime() throws IOException {
        return this.path()
                .getFileSystem()
                .provider()
                .readAttributes(this.path(),
                        BasicFileAttributes.class,
                        this.getDefaultLinkOptions())
                .creationTime();
    }

    default FileTime modifiedTime() throws IOException {
        return this.path()
                .getFileSystem()
                .provider()
                .readAttributes(this.path(),
                        BasicFileAttributes.class,
                        this.getDefaultLinkOptions())
                .lastModifiedTime();
    }

    default FileTime lastAccessTime() throws IOException {
        return this.path()
                .getFileSystem()
                .provider()
                .readAttributes(this.path(),
                        BasicFileAttributes.class,
                        this.getDefaultLinkOptions())
                .lastAccessTime();
    }
    
    default long elapsedTimeCreation(final TemporalUnit unit) throws IOException {
        Objects.requireNonNull(unit);
        
        return unit.between(this.creationTime().toInstant(), Instant.now());
    }
    
    default long elapsedTimeModified(final TemporalUnit unit) throws IOException {
        Objects.requireNonNull(unit);
        
        return unit.between(this.modifiedTime().toInstant(), Instant.now());
    }
    
    default long elapsedTimeLastAccess(final TemporalUnit unit) throws IOException {
        Objects.requireNonNull(unit);
        
        return unit.between(this.lastAccessTime().toInstant(), Instant.now());
    }
    
    default boolean isAbsolute() {
        return this.path().isAbsolute();
    }

    default boolean startsWith(final String other) {
        return this.path().startsWith(other);
    }

    default String fileName(final String other) {
        return this.path().getFileName().toString();
    }

}
