package org.jprelude.core.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jprelude.core.io.function.IOPredicate;
import org.jprelude.core.util.Seq;
import org.jprelude.core.util.function.CheckedPredicate;

public final class PathLister {
    final Function<Path, IOPredicate<Path>> pathFilterFunction;
    final Function<Path, IOPredicate<Path>> recursionFilterFunction; 
    final Integer maxDepth;
    
    private PathLister(final Builder builder) {
        assert builder != null;
        
        this.pathFilterFunction = builder.pathFilterFunction;
        this.recursionFilterFunction = builder.recursionFilterFunction;
        this.maxDepth = builder.maxDepth;
    }
    
    public static Builder builder() {
        return new Builder();
    }
   
    
    public Seq<Path> list(final Path path) {
        Objects.requireNonNull(path);
        
        return PathLister.listPath(path);
    }
    
    public Seq<Path> listFiles(final Path path) {
        Objects.requireNonNull(path);

        return this
                .list(path)
                .filter(CheckedPredicate.unchecked(p -> !Files.isDirectory(p)));
    }
    
    public Seq<Path> listDirectories(final Path path) {
        Objects.requireNonNull(path);

        return this
                .list(path)
                .filter(CheckedPredicate.unchecked(p -> Files.isDirectory(p)));
    }
    
    public static class Builder {
        Function<Path, IOPredicate<Path>> pathFilterFunction;
        Function<Path, IOPredicate<Path>> recursionFilterFunction; 
        Integer maxDepth = Integer.MAX_VALUE;
        
        public Builder() {
            this.pathFilterFunction = p1 -> (p2 -> true);
            this.recursionFilterFunction = p1 -> (p2 -> false);
            this.maxDepth = null;
        }
        
        public Builder pathFilter(final IOPredicate<Path> pathFilter) {
            Objects.requireNonNull(pathFilter);
            
            this.pathFilterFunction = p -> pathFilter;
            return this;
        }
        
        public Builder pathFilter(final String syntaxAndPattern) {
            Objects.requireNonNull(syntaxAndPattern);
            
            this.pathFilterFunction = p1 -> {
                final PathMatcher pathMatcher =  p1.getFileSystem().getPathMatcher(syntaxAndPattern);
                
                return p2 -> pathMatcher.matches(p2);
            };
            
            return this;
        }
        
        public Builder recursionFilter(final IOPredicate<Path> recursionFilter) {
            Objects.requireNonNull(recursionFilter);
            
            this.recursionFilterFunction =
                    p1 -> p2 -> Files.isDirectory(p2) && recursionFilter.test(p2);
            
            return this;
        }
        
        public Builder recursionFilter(final String syntaxAndPattern) {
            Objects.requireNonNull(syntaxAndPattern);
            
            this.recursionFilterFunction = p1 -> {
                final PathMatcher pathMatcher =  p1.getFileSystem().getPathMatcher(syntaxAndPattern);
                
                return p2 -> pathMatcher.matches(p2) && Files.isDirectory(p2);
            };
                                 
            return this;
        }
        
        public Builder fullRecursive() {
            this.recursionFilterFunction = p1 -> p2 -> true;
            return this;
        }

        public Builder maxDepth(final int maxDepth) {
            if (maxDepth <= 0) {
                throw new IllegalArgumentException();
            }
            
            this.maxDepth = maxDepth;
            return this;
        }
        
        public Builder unlimitedDepth() {
            this.maxDepth = null;
            return this;
        }
      
        public PathLister build() {
            return new PathLister(this);
        }
    }

    static Seq<Path> listPath(final Path path) {
        Objects.requireNonNull(path);

        return Seq.from(() -> {
            final Stream<Path> ret;
            
            try {
                final DirectoryStream<Path> dirStream = Files.newDirectoryStream(path, file -> true);
                
                ret = StreamSupport.stream(dirStream.spliterator(), false);
            } catch (final IOException e) {
                throw new UncheckedIOException(e.getMessage(), e);
            }

            return ret;
        });
    }
    
    static Seq<Path> listPath(final Path path, final IOPredicate<? super Path> pathFilter) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(pathFilter);

        return PathLister.listPath(path).filter(pathFilter.unchecked());
    }

    static Seq<Path> listPathRecursive(final Path path) {
        Objects.requireNonNull(path);

        return PathLister.listPathRecursive(path, (IOPredicate) p -> true);
    }
 
    static Seq<Path> listPathRecursive(final Path path, final IOPredicate<? super Path> pathFilter) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(pathFilter);

        return PathLister.listPathRecursive(path, pathFilter, (IOPredicate) p -> true);
    }
 
    static Seq<Path> listPathRecursive(final Path path, final IOPredicate<? super Path> pathFilter, final IOPredicate<? super Path> recursionFilter) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(pathFilter);
        Objects.requireNonNull(recursionFilter);
        
        return PathLister.listPathRecursive(path, pathFilter, recursionFilter, Integer.MAX_VALUE);
    }
    
    static Seq<Path> listPathRecursive(final Path path, final IOPredicate<? super Path> pathFilter, final IOPredicate<? super Path> recursionFilter, final int maxDepth) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(pathFilter);
        Objects.requireNonNull(recursionFilter);

        if (maxDepth <= 0) {
            throw new IllegalArgumentException();
        }
        
        System.out.println("==> " + PathLister.listPath(path).toList());
        
        return PathLister.listPath(path).flatMap(p -> {
            final Seq<Path> ret;
        System.out.println("yyyy");
            try {
                final boolean involve = pathFilter.test(p);
                final boolean descent = maxDepth > 1 && Files.isDirectory(p) && recursionFilter.test(p);
        System.out.println("==>" + descent);
                ret = descent
                        ? PathLister.listPathRecursive(p, pathFilter, recursionFilter, maxDepth - 1)
                        : (!involve ? Seq.empty() : Seq.of(p));
            } catch (final IOException e) {
                throw new UncheckedIOException(e.getMessage(), e);
            }
            
            return ret;
        });
    }
}
