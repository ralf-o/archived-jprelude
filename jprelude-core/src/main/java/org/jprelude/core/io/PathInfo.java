package org.jprelude.core.io;

import org.jprelude.core.io.function.IOPredicate;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jprelude.core.util.Seq;

public interface PathInfo {
    Path getPath();
    
    default boolean isRegularFile() throws IOException {
        final BasicFileAttributes fileAttrs = Files.readAttributes(this.getPath(), BasicFileAttributes.class);
        return fileAttrs.isRegularFile();
    }

    default boolean isDirectory() throws IOException {
        final BasicFileAttributes fileAttrs = Files.readAttributes(this.getPath(), BasicFileAttributes.class);
        return fileAttrs.isDirectory();
    }

    default boolean isSymbolicLink() throws IOException {
        final BasicFileAttributes fileAttrs = Files.readAttributes(this.getPath(), BasicFileAttributes.class);
        return fileAttrs.isSymbolicLink();
    }

    default boolean isHidden() throws IOException {
        return this.getFileSystem().provider().isHidden(this.getPath());            
    }          
    
    default String getFullName() {
        return this.getPath().toFile().getAbsolutePath();
    }
    
    default FileTime getCreationTime() throws IOException {
        final BasicFileAttributes attrs = Files.readAttributes(this.getPath(), BasicFileAttributes.class);
        return attrs.creationTime();
    }
    
    default FileTime getLastModifiedTime() throws IOException {
        final BasicFileAttributes attrs = Files.readAttributes(this.getPath(), BasicFileAttributes.class);
        return attrs.lastModifiedTime();
     }
    
     default FileTime getLastAccessTime() throws IOException {
        final BasicFileAttributes attrs = Files.readAttributes(this.getPath(), BasicFileAttributes.class);
        return attrs.lastAccessTime();
     }

    // TODO - Is that method really useful? Naming: Should we call it "CreationTimeAge"?
    default long getAge() throws IOException {
        return this.getAge(TimeUnit.MILLISECONDS);
    }
     
    // TODO - Naming: Should we call it "getCreationTimeAge"?
    default long getAge(final TimeUnit unit) throws IOException {
        Objects.requireNonNull(unit);
        final BasicFileAttributes attrs = Files.readAttributes(this.getPath(), BasicFileAttributes.class);
        final long creationTimestampInNanos = attrs.creationTime().to(TimeUnit.NANOSECONDS);
        final long currentTimestampInNanos = System.currentTimeMillis() * 1000000;
        return unit.convert(currentTimestampInNanos - creationTimestampInNanos, TimeUnit.NANOSECONDS);
    }
    
    default boolean matches(final PathMatcher pathMatcher) {
        Objects.requireNonNull(pathMatcher);
        return pathMatcher.matches(this.getPath());
    }
        
    default Seq<PathInfo> list() {
        return Seq.from(() -> {
            final Stream<PathInfo> ret;
            
            try {
                final DirectoryStream<Path> dirStream = Files.newDirectoryStream(this.getPath(), file -> true);
                
                ret = StreamSupport.stream(dirStream.spliterator(), false)
                        .map(path -> PathInfo.from(path));
            } catch (final IOException e) {
                throw new UncheckedIOException(e.getMessage(), e);
            }

            return ret;
        });
    }
    
    default Seq<PathInfo> list(final IOPredicate<? super PathInfo> pathFilter) {
        Objects.requireNonNull(pathFilter);
        return this.list().filter(IOPredicate.unchecked(pathFilter));
    }

    default Seq<PathInfo> listRecursive() {
        return this.list().filter(pathFilter -> true);
    }
 
    default Seq<PathInfo> listRecursive(final IOPredicate<? super PathInfo> pathFilter) {
        Objects.requireNonNull(pathFilter);

        return this.listRecursive(pathFilter, p -> true);
    }
 
    default Seq<PathInfo> listRecursive(final IOPredicate<? super PathInfo> pathFilter, final IOPredicate<? super PathInfo> recursionFilter) {
        Objects.requireNonNull(pathFilter);
        Objects.requireNonNull(recursionFilter);
        
        return this.listRecursive(pathFilter, recursionFilter, Integer.MAX_VALUE);
    }
    
    default Seq<PathInfo> listRecursive(final IOPredicate<? super PathInfo> pathFilter, final IOPredicate<? super PathInfo> dirFilter, final int maxDepth) {
        Objects.requireNonNull(pathFilter);
        Objects.requireNonNull(dirFilter);

        if (maxDepth <= 0) {
            throw new IllegalArgumentException();
        }
        
        return this.list().flatMap(filePath -> {
            final Seq<PathInfo> ret;
            
            try {
                final boolean involve = pathFilter.test(filePath);
                final boolean descent = maxDepth > 1 && filePath.isDirectory() && dirFilter.test(filePath);
        
                ret = descent
                        ? filePath.listRecursive(pathFilter, dirFilter, maxDepth - 1)
                        : (!involve ? Seq.empty() : Seq.of(filePath));
            } catch (final IOException e) {
                throw new UncheckedIOException(e.getMessage(), e);
            }
            
            return ret;
        });
    }
    
    default FileSystem getFileSystem() {
        return this.getPath().getFileSystem();
    }

    default boolean isAbsolute() {
        return this.getPath().isAbsolute();
    }

    default Path getRoot() {
        return this.getPath().getRoot();
    }

    default Path getFileName() {
        return this.getPath().getFileName();
    }

    default Path getParent() {
        return this.getPath().getParent();
    }

    default int getNameCount() {
        return this.getPath().getNameCount();
    }

    default Path getName(final int index) {
        return this.getPath().getName(index);
    }

    default Path subpath(final int beginIndex, final int endIndex) {
        return this.getPath().subpath(beginIndex, endIndex);
    }

    default boolean startsWith(final Path other) {
        return this.getPath().startsWith(other);
    }

    default boolean startsWith(String other) {
        return this.getPath().startsWith(other);
    }

    default boolean endsWith(Path other) {
        return this.getPath().endsWith(other);
    }

    default boolean endsWith(String other) {
        return this.getPath().endsWith(other);
    }

    default Path normalize() {
        return this.getPath().normalize();
    }

    default Path resolve(Path other) {
        return this.getPath().resolve(other);
    }

    default Path resolve(String other) {
        return this.getPath().resolve(other);
    }

    default Path resolveSibling(Path other) {
        return this.getPath().resolveSibling(other);
    }

    default Path resolveSibling(String other) {
        return this.getPath().resolveSibling(other);
    }

    default Path relativize(Path other) {
        return this.getPath().relativize(other);
    }

    default URI toUri() {
        return this.getPath().toUri();
    }

    default Path toAbsolutePath() {
        return this.getPath().toAbsolutePath();
    }

    default Path toRealPath(final LinkOption... options) throws IOException {
        return this.getPath().toRealPath(options);
    }

    default File toFile() {
        return this.getPath().toFile();
    }

    default WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>[] events, final WatchEvent.Modifier... modifiers) throws IOException {
        return this.getPath().register(watcher, events, modifiers);
    }

    default WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>... events) throws IOException {
        return this.getPath().register(watcher, events);
    }

    default Iterator<Path> iterator() {
        return this.getPath().iterator();
    }

    default int compareTo(final Path other) {
        return this.getPath().compareTo(other);
    }     


    static PathInfo from(final Path path) {
        Objects.requireNonNull(path);
        return () -> path;
    }
    
    public static PathInfo from(final File file) {
        Objects.requireNonNull(file);
        return PathInfo.from(file.toPath());
    }
}
