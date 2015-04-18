package org.jprelude.core.io;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Objects;
import java.util.function.Function;
import org.jprelude.core.io.function.IOPredicate;
import org.jprelude.core.util.Seq;
import org.jprelude.core.util.function.CheckedPredicate;

public final class PathLister {
    final Function<Path, IOPredicate<PathRef>> pathFilterFunction;
    final Function<Path, IOPredicate<PathRef>> recursionFilterFunction; 
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

        return this
                .listRefs(path)
                .map(ref -> ref.getPath());
    }
    
    public Seq<Path> listFiles(final Path path) {
        Objects.requireNonNull(path);

        return this
                .listFileRefs(path)
                .map(ref -> ref.getPath());                
    }
    
    public Seq<Path> listDirectories(final Path path) {
        Objects.requireNonNull(path);

        return this
                .listDirectoryRefs(path)
                .map(ref -> ref.getPath());                
    }
    
    public Seq<PathRef> listRefs(final Path path) {
        Objects.requireNonNull(path);

        return PathRef.from(path)
                .listRecursive(
                        this.pathFilterFunction.apply(path),
                        this.recursionFilterFunction.apply(path),
                        (this.maxDepth == null ? Integer.MAX_VALUE : this.maxDepth));
    }
    
    public Seq<PathRef> listFileRefs(final Path path) {
        Objects.requireNonNull(path);

        return this
                .listRefs(path)
                .filter(CheckedPredicate.unchecked(ref -> !ref.isDirectory()));
    }

    public Seq<PathRef> listDirectoryRefs(final Path path) {
        Objects.requireNonNull(path);

        return this
                .listRefs(path)
                .filter(CheckedPredicate.unchecked(ref -> ref.isDirectory()));        
    }
    
    public static class Builder {
        Function<Path, IOPredicate<PathRef>> pathFilterFunction;
        Function<Path, IOPredicate<PathRef>> recursionFilterFunction; 
        Integer maxDepth = Integer.MAX_VALUE;
        
        public Builder() {
            this.pathFilterFunction = p1 -> (p2 -> true);
            this.recursionFilterFunction = p1 -> (p2 -> false);
            this.maxDepth = null;
        }
        
        public Builder pathFilter(final IOPredicate<PathRef> pathFilter) {
            Objects.requireNonNull(pathFilter);
            
            this.pathFilterFunction = p -> pathFilter;
            return this;
        }
        
        public Builder pathFilter(final String syntaxAndPattern) {
            Objects.requireNonNull(syntaxAndPattern);
            
            this.pathFilterFunction = p1 -> {
                final PathMatcher pathMatcher =  p1.getFileSystem().getPathMatcher(syntaxAndPattern);
                
                return p2 -> pathMatcher.matches(p2.getPath());
            };
            
            return this;
        }
        
        public Builder recursionFilter(final IOPredicate<PathRef> recursionFilter) {
            Objects.requireNonNull(recursionFilter);
            
            this.recursionFilterFunction =
                    p1 -> p2 -> p2.isDirectory() && recursionFilter.test(p2);
            
            return this;
        }
        
        public Builder recursionFilter(final String syntaxAndPattern) {
            Objects.requireNonNull(syntaxAndPattern);
            
            this.recursionFilterFunction = p1 -> {
                final PathMatcher pathMatcher =  p1.getFileSystem().getPathMatcher(syntaxAndPattern);
                
                return p2 -> pathMatcher.matches(p2.getPath()) && p2.isDirectory();
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
}
