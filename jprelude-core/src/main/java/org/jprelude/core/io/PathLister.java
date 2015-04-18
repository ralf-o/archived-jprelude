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

public final class PathLister {
    final Function<Path, Seq<Path>> f;
    
    private PathLister(final Function<Path, Seq<Path>> f) {
        assert f != null;
        
        this.f = f;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public Seq<Path> list(final Path path) {
        Objects.requireNonNull(path);
        
        return this.f.apply(path);
    }
   
    public static class Builder {
        Function<Path, IOPredicate<Path>> pathFilterFunction;
        Function<Path, IOPredicate<Path>> recursionFilterFunction; 
        int maxDepth = Integer.MAX_VALUE;
        
        public Builder() {
            this.pathFilterFunction = p1 -> (p2 -> true);
            this.recursionFilterFunction = p1 -> (p2 -> false);
            this.maxDepth = Integer.MAX_VALUE;
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
            this.maxDepth = Integer.MAX_VALUE;
            return this;
        }
      
        public PathLister build() {
            return new PathLister(
                    path -> PathLister.createRecursive(
                        this.pathFilterFunction.apply(path),
                        recursionFilterFunction.apply(path),
                        this.maxDepth).list(path));
        }
    }

    public static PathLister create() {
        return new PathLister(path -> Seq.from(() -> {
            final Stream<Path> ret;

            try {
                final DirectoryStream<Path> dirStream = Files.newDirectoryStream(path, file -> true);

                ret = StreamSupport.stream(dirStream.spliterator(), false);
            } catch (final IOException e) {
                throw new UncheckedIOException(e.getMessage(), e);
            }

            return ret;                
        }));
    }
    
    static PathLister create(final IOPredicate<? super Path> pathFilter) {
        Objects.requireNonNull(pathFilter);

        return PathLister.createRecursive(pathFilter, (IOPredicate<Path>) path -> false, 1);
    }

    static PathLister createRecursive() {
        return PathLister.createRecursive((IOPredicate<Path>) path -> true);
    }
    
    static PathLister createRecursive(final IOPredicate<? super Path> pathFilter) {
        Objects.requireNonNull(pathFilter);
        
        return PathLister.createRecursive(pathFilter, (IOPredicate<Path>) path -> true); 
    }
    
    static PathLister createRecursive(
            final IOPredicate<? super Path> pathFilter,
            final IOPredicate<? super Path> recursionFilter) {
        
        Objects.requireNonNull(pathFilter);
        Objects.requireNonNull(recursionFilter);

        return PathLister.createRecursive(pathFilter, recursionFilter, Integer.MAX_VALUE);
    }
    
    static PathLister createRecursive(
            final IOPredicate<? super Path> pathFilter,
            final IOPredicate<? super Path> recursionFilter,
            final int maxDepth) {
        
        Objects.requireNonNull(pathFilter);
        Objects.requireNonNull(recursionFilter);
       
        if (maxDepth <= 0) {
            throw new IllegalArgumentException();
        }
        
        
        return new PathLister(path -> PathLister.create().list(path).flatMap(p -> {
            final Seq<Path> ret;

            try {
                final boolean involve = pathFilter.test(p);
                final boolean descent = maxDepth > 1 && Files.isDirectory(p) && recursionFilter.test(p);

                ret = descent
                        ? PathLister.createRecursive(pathFilter, recursionFilter, maxDepth - 1).list(p)
                        : (!involve ? Seq.empty() : Seq.of(p));
            } catch (final IOException e) {
                throw new UncheckedIOException(e.getMessage(), e);
            }

            return ret;
        }));
    }
}
