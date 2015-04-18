package org.jprelude.core.io;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Objects;
import java.util.function.Function;
import org.jprelude.core.io.function.IOPredicate;
import org.jprelude.core.util.Seq;

public final class PathLister {
    final Function<Path, IOPredicate<PathInfo>> pathFilterFunction;
    final Function<Path, IOPredicate<PathInfo>> recursionFilterFunction; 
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
                .listInfos(path)
                .map(info -> info.getPath());
    }
    
    public Seq<Path> listFiles(final Path path) {
        Objects.requireNonNull(path);

        return this
                .listFileInfos(path)
                .map(info -> info.getPath());                
    }
    
    public Seq<Path> listDirectories(final Path path) {
        Objects.requireNonNull(path);

        return this
                .listDirectoryInfos(path)
                .map(info -> info.getPath());                
    }
    
    public Seq<PathInfo> listInfos(final Path path) {
        Objects.requireNonNull(path);

        return PathInfo.from(path)
                .listRecursive(
                        this.pathFilterFunction.apply(path),
                        this.recursionFilterFunction.apply(path),
                        (this.maxDepth == null ? Integer.MAX_VALUE : this.maxDepth));
    }
    
    public Seq<PathInfo> listFileInfos(final Path path) {
        Objects.requireNonNull(path);

        return this
                .listInfos(path)
                .filter(IOPredicate.unchecked(info -> !info.isDirectory()));
    }

    public Seq<PathInfo> listDirectoryInfos(final Path path) {
        Objects.requireNonNull(path);

        return this
                .listInfos(path)
                .filter(IOPredicate.unchecked(info -> info.isDirectory()));        
    }
    
    public static class Builder {
        Function<Path, IOPredicate<PathInfo>> pathFilterFunction;
        Function<Path, IOPredicate<PathInfo>> recursionFilterFunction; 
        Integer maxDepth = Integer.MAX_VALUE;
        
        public Builder() {
            this.pathFilterFunction = p1 -> (p2 -> true);
            this.recursionFilterFunction = p1 -> (p2 -> false);
            this.maxDepth = null;
        }
        
        public Builder pathFilter(final IOPredicate<PathInfo> pathFilter) {
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
        
        public Builder recursionFilter(final IOPredicate<PathInfo> recursionFilter) {
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
