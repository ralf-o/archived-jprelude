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
    
    default boolean isHidden() throws IOException {
        return Files.isHidden(this.getPath());
    }
    
    default boolean isExecutable() {
        return Files.isExecutable(this.getPath());
    }
    
    default boolean isReadable()  {
        return Files.isReadable(this.getPath());
    }

    default boolean isWritable()  {
        return Files.isWritable(this.getPath());
    }

    default boolean exists()  {
        return Files.exists(this.getPath(), this.getDefaultLinkOptions());
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
    
    default FileTime getCreationTime() throws IOException {
        return this.getPath()
                .getFileSystem()
                .provider()
                .readAttributes(this.getPath(),
                        BasicFileAttributes.class,
                        this.getDefaultLinkOptions())
                .creationTime();
    }

    default FileTime getModifiedTime() throws IOException {
        return this.getPath()
                .getFileSystem()
                .provider()
                .readAttributes(this.getPath(),
                        BasicFileAttributes.class,
                        this.getDefaultLinkOptions())
                .lastModifiedTime();
    }

    default FileTime getLastAccessTime() throws IOException {
        return this.getPath()
                .getFileSystem()
                .provider()
                .readAttributes(this.getPath(),
                        BasicFileAttributes.class,
                        this.getDefaultLinkOptions())
                .lastAccessTime();
    }
    
    default long getElapsedTimeCreation(final TemporalUnit unit) throws IOException {
        Objects.requireNonNull(unit);
        
        return unit.between(this.getCreationTime().toInstant(), Instant.now());
    }
    
    default long getElapsedTimeModified(final TemporalUnit unit) throws IOException {
        Objects.requireNonNull(unit);
        
        return unit.between(this.getModifiedTime().toInstant(), Instant.now());
    }
    
    default long getElapsedTimeLastAccess(final TemporalUnit unit) throws IOException {
        Objects.requireNonNull(unit);
        
        return unit.between(this.getLastAccessTime().toInstant(), Instant.now());
    }
    
    default boolean isAbsolute() {
        return this.getPath().isAbsolute();
    }

    default boolean startsWith(final String other) {
        return this.getPath().startsWith(other);
    }

    default String getFileName() {
        return this.getPath().getFileName().toString();
    }

}
