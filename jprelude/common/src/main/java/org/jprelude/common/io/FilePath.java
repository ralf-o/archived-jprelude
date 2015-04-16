package org.jprelude.common.io;

import org.jprelude.common.io.function.IOPredicate;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jprelude.common.util.Seq;

public interface FilePath extends Path {
    boolean isRegularFile() throws IOException;
    boolean isDirectory() throws IOException;
    boolean isSymbolicLink() throws IOException;
    boolean isHidden() throws IOException;
    
    default Seq<FilePath> list() {
        return Seq.from(() -> {
            final Stream<FilePath> ret;
            
            try {
                final DirectoryStream<Path> dirStream = this.getFileSystem().provider().newDirectoryStream(this, file -> true);
                
                ret = StreamSupport.stream(dirStream.spliterator(), false)
                        .map(path -> FilePath.from(path));
            } catch (final IOException e) {
                throw new UncheckedIOException(e.getMessage(), e);
            }
            
            return ret;
        });
    }
    
    default Seq<FilePath> list(IOPredicate<FilePath> filter) {
        Objects.requireNonNull(filter);

        return this.list().filter(filePath -> {
            final boolean ret;
            
            try {
                ret = filter.accept(filePath);
            } catch (final IOException e) {
                throw new UncheckedIOException(e.getMessage(), e);
            }
            
            return ret;
        });
    }
            
    default Seq<FilePath> listRecursive() {
        return this.list().flatMap(filePath -> {
            final Seq<FilePath> ret;
            
            try {
                ret = filePath.isDirectory()
                        ? filePath.list()
                        : Seq.of(filePath);
            } catch (final IOException e) {
                throw new UncheckedIOException(e.getMessage(), e);
            }
            
            return ret;
        });
    }
    
    default Seq<FilePath> listRecursive(final IOPredicate filter) {
        return this.listRecursive().filter(filePath -> {
            final boolean ret;
            
            try {
                ret = filter.accept(filePath);
            } catch (final IOException e) {
                throw new UncheckedIOException(e.getMessage(), e);
            }
            
            return ret;
        });
    }
    
    default Seq<FilePath> listRecursive(final IOPredicate filter, int maxDepth) {
        return this.listRecursive(filter, dir -> true, maxDepth);
    }
   
    default Seq<FilePath> listRecursive(final IOPredicate filter, final IOPredicate dirFilter, int maxDepth) {
        return null; // TODO
    }

    static FilePath from(final Path path) {
        Objects.requireNonNull(path);
        
        return new FilePath() {
            @Override
            public boolean isRegularFile() throws IOException {
                final BasicFileAttributes fileAttrs = Files.readAttributes(path, BasicFileAttributes.class);
                return fileAttrs.isRegularFile();
            }

            @Override
            public boolean isDirectory() throws IOException {
                final BasicFileAttributes fileAttrs = Files.readAttributes(path, BasicFileAttributes.class);
                return fileAttrs.isDirectory();
            }

            @Override
            public boolean isSymbolicLink() throws IOException {
                final BasicFileAttributes fileAttrs = Files.readAttributes(path, BasicFileAttributes.class);
                return fileAttrs.isSymbolicLink();
            }

            @Override
            public boolean isHidden() throws IOException {
                this.list(file -> file.isHidden());
                return this.getFileSystem().provider().isHidden(this);            
            }
               
            @Override
            public FileSystem getFileSystem() {
                return path.getFileSystem();
            }

            @Override
            public boolean isAbsolute() {
                return path.isAbsolute();
            }

            @Override
            public Path getRoot() {
                return path.getRoot();
            }

            @Override
            public Path getFileName() {
                return path.getFileName();
            }

            @Override
            public Path getParent() {
                return path.getParent();
            }

            @Override
            public int getNameCount() {
                return path.getNameCount();
            }

            @Override
            public Path getName(final int index) {
                return path.getName(index);
            }

            @Override
            public Path subpath(final int beginIndex, final int endIndex) {
                return path.subpath(beginIndex, endIndex);
            }

            @Override
            public boolean startsWith(final Path other) {
                return path.startsWith(other);
            }

            @Override
            public boolean startsWith(String other) {
                return path.startsWith(other);
            }

            @Override
            public boolean endsWith(Path other) {
                return path.endsWith(other);
            }

            @Override
            public boolean endsWith(String other) {
                return path.endsWith(other);
            }

            @Override
            public Path normalize() {
                return path.normalize();
            }

            @Override
            public Path resolve(Path other) {
                return path.resolve(other);
            }

            @Override
            public Path resolve(String other) {
                return path.resolve(other);
            }

            @Override
            public Path resolveSibling(Path other) {
                return path.resolveSibling(other);
            }

            @Override
            public Path resolveSibling(String other) {
                return path.resolveSibling(other);
            }

            @Override
            public Path relativize(Path other) {
                return path.relativize(other);
            }

            @Override
            public URI toUri() {
                return path.toUri();
            }

            @Override
            public Path toAbsolutePath() {
                return path.toAbsolutePath();
            }

            @Override
            public Path toRealPath(final LinkOption... options) throws IOException {
                return path.toRealPath(options);
            }

            @Override
            public File toFile() {
                return path.toFile();
            }

            @Override
            public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>[] events, final WatchEvent.Modifier... modifiers) throws IOException {
                return path.register(watcher, events, modifiers);
            }

            @Override
            public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>... events) throws IOException {
                return path.register(watcher, events);
            }

            @Override
            public Iterator<Path> iterator() {
                return path.iterator();
            }

            @Override
            public int compareTo(final Path other) {
                return path.compareTo(other);
            }     
        };
    }
    
    public static FilePath from(final File file) {
        Objects.requireNonNull(file);
        return FilePath.from(file.toPath());
    }
}
