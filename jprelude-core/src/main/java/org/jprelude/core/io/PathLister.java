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
import org.jprelude.core.util.Seq;
import org.jprelude.core.util.function.CheckedPredicate;

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
        Function<Path, CheckedPredicate<Path, IOException>> pathFilterFunction;
        Function<Path, CheckedPredicate<Path, IOException>> recursionFilterFunction; 
        int maxDepth = Integer.MAX_VALUE;
        
        public Builder() {
            this.pathFilterFunction = p1 -> (p2 -> true);
            this.recursionFilterFunction = p1 -> (p2 -> false);
            this.maxDepth = Integer.MAX_VALUE;
        }
        
        public Builder filter(final CheckedPredicate<Path, IOException> pathFilter) {
            Objects.requireNonNull(pathFilter);
            
            this.pathFilterFunction = p -> pathFilter;
            return this;
        }
        
        public Builder filter(final String syntaxAndPattern) {
            Objects.requireNonNull(syntaxAndPattern);
            
            this.pathFilterFunction = p1 -> {
                final PathMatcher pathMatcher =  p1.getFileSystem().getPathMatcher(syntaxAndPattern);
                
                return p2 -> pathMatcher.matches(p2);
            };
            
            return this;
        }
        
        public Builder addFilter(final CheckedPredicate<Path, IOException> pathFilter) {
            Objects.requireNonNull(pathFilter);
            
            if (this.pathFilterFunction == null) {
                this.filter(pathFilter);
            } else {
                final Function<Path, CheckedPredicate<Path, IOException>> function = this.pathFilterFunction;
                
                this.pathFilterFunction = p -> p2 -> function.apply(p).test(p2) && pathFilter.test(p2);
            }
            
            return this;
        }
        
        public Builder addFilter(final String syntaxAndPattern) {
            Objects.requireNonNull(syntaxAndPattern);
            
                
            if (this.pathFilterFunction == null) {
                this.filter(syntaxAndPattern);
            } else {
                final Function<Path, CheckedPredicate<Path, IOException>> function = this.pathFilterFunction;
                
                this.pathFilterFunction = p1 -> {
                    final PathMatcher pathMatcher =  p1.getFileSystem().getPathMatcher(syntaxAndPattern);

                    return p2 ->  function.apply(p1).test(p2) && pathMatcher.matches(p2);
                };
            }
            
            return this;
        }
        
        public Builder recursive(final CheckedPredicate<Path, IOException> recursionFilter) {
            Objects.requireNonNull(recursionFilter);
            
            this.recursionFilterFunction =
                    p1 -> p2 -> Files.isDirectory(p2) && recursionFilter.test(p2);
            
            return this;
        }
        
        public Builder recursive(final String syntaxAndPattern) {
            Objects.requireNonNull(syntaxAndPattern);
            
            this.recursionFilterFunction = p1 -> {
                final PathMatcher pathMatcher =  p1.getFileSystem().getPathMatcher(syntaxAndPattern);
                
                return p2 -> pathMatcher.matches(p2) && Files.isDirectory(p2);
            };
                                 
            return this;
        }
        
        public Builder addRecursionFilter(final CheckedPredicate<Path, IOException> recursionFilter) {
            Objects.requireNonNull(recursionFilter);
            
            if (this.recursionFilterFunction == null) {
                this.recursive(recursionFilter);
            } else {
                final Function<Path, CheckedPredicate<Path, IOException>> function = this.recursionFilterFunction;
                
                this.recursionFilterFunction =
                    p1 -> p2 -> function.apply(p1).test(p2) && recursionFilter.test(p2);     
            }
            
            return this;
        }
        
        public Builder addRecursionFilter(final String syntaxAndPattern) {
            Objects.requireNonNull(syntaxAndPattern);
            
            if (this.recursionFilterFunction == null) {
                this.recursive(syntaxAndPattern);
            } else {
                final Function<Path, CheckedPredicate<Path, IOException>> function = this.recursionFilterFunction;

                this.recursionFilterFunction = p1 -> {
                     final PathMatcher pathMatcher =  p1.getFileSystem().getPathMatcher(syntaxAndPattern);

                    return p2 -> function.apply(p1).test(p2) && pathMatcher.matches(p2);
                };
            }
            
            return this;
        }
        
        public Builder recursive() {
            this.recursionFilterFunction = p1 -> p2 -> true;
            return this;
        }
        
        public Builder nonRecursive() {
            this.recursionFilterFunction = p1 -> p2 -> false;
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
    
    public static PathLister create(final CheckedPredicate< Path, IOException> pathFilter) {
        Objects.requireNonNull(pathFilter);

        return PathLister.createRecursive(pathFilter, (CheckedPredicate<Path, IOException>) path -> false, 1);
    }

    public static PathLister createRecursive() {
        return PathLister.createRecursive((CheckedPredicate<Path, IOException>) path -> true);
    }
    
    public static PathLister createRecursive(final CheckedPredicate<Path, IOException> pathFilter) {
        Objects.requireNonNull(pathFilter);
        
        return PathLister.createRecursive(pathFilter, (CheckedPredicate<Path, IOException>) path -> true); 
    }
    
    public static PathLister createRecursive(
            final CheckedPredicate<Path, IOException> pathFilter,
            final CheckedPredicate<Path, IOException> recursionFilter) {
        
        Objects.requireNonNull(pathFilter);
        Objects.requireNonNull(recursionFilter);

        return PathLister.createRecursive(pathFilter, recursionFilter, Integer.MAX_VALUE);
    }
    
    public static PathLister createRecursive(
            final CheckedPredicate<Path, IOException> pathFilter,
            final CheckedPredicate<Path, IOException> recursionFilter,
            final int maxDepth) {
        
        Objects.requireNonNull(pathFilter);
        Objects.requireNonNull(recursionFilter);
       
        if (maxDepth <= 0) {
            throw new IllegalArgumentException();
        }
  
        return new PathLister(path -> PathLister.create().list(path).flatMap(p -> {
            Seq<Path> ret;

            try {
                final boolean involve = pathFilter.test(p);
                final boolean descent = maxDepth > 1 && Files.isDirectory(p) && recursionFilter.test(p);

                ret = descent
                        ? PathLister.createRecursive(pathFilter, recursionFilter, maxDepth - 1).list(p)
                        : (!involve ? Seq.empty() : Seq.of(p));
                                
                if (descent && involve) {
                    ret = ret.prepend(p);
                }
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            }

            return ret;
        }));
    }
}
