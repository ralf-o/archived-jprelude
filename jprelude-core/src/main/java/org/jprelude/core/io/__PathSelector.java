package org.jprelude.core.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jprelude.core.util.Seq;
import org.jprelude.core.util.function.CheckedPredicate;

public final class __PathSelector {
    final Function<Path, Seq<Path>> f;
    
    private __PathSelector(final Function<Path, Seq<Path>> f) {
        assert f != null;
        this.f = f;
    }
    
    public Seq<Path> list(final Path path) {
        Objects.requireNonNull(path);
        
        return this.f.apply(path);
    }
    
    public Seq<Path> list(final Path... paths) {
        return Seq.of(paths)
                .flatMap(path -> this.list(path));
    }
    
    public static __PathSelector create() {
        return new __PathSelector(path -> Seq.from(() -> {
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
    
    public static __PathSelector create(final CheckedPredicate< Path, IOException> pathFilter) {
        Objects.requireNonNull(pathFilter);

        return __PathSelector.createRecursive(pathFilter, (CheckedPredicate<Path, IOException>) path -> false, 1);
    }

    public static __PathSelector createRecursive() {
        return __PathSelector.createRecursive((CheckedPredicate<Path, IOException>) path -> true);
    }
    
    public static __PathSelector createRecursive(
            final CheckedPredicate<Path, IOException> pathFilter) {
        Objects.requireNonNull(pathFilter);
        
        return __PathSelector.createRecursive(pathFilter, (CheckedPredicate<Path, IOException>) path -> true); 
    }
    
    public static __PathSelector createRecursive(
            final CheckedPredicate<Path, IOException> pathFilter,
            final CheckedPredicate<Path, IOException> recursionFilter) {
        
        Objects.requireNonNull(pathFilter);
        Objects.requireNonNull(recursionFilter);

        return __PathSelector.createRecursive(pathFilter, recursionFilter, Integer.MAX_VALUE);
    }
    
    public static __PathSelector createRecursive(
            final CheckedPredicate<Path, IOException> pathFilter,
            final CheckedPredicate<Path, IOException> recursionFilter,
            final int maxDepth) {
        
        Objects.requireNonNull(pathFilter);
        Objects.requireNonNull(recursionFilter);
       
        if (maxDepth <= 0) {
            throw new IllegalArgumentException();
        }
  
        return new __PathSelector(path -> __PathSelector.create().list(path).flatMap(p -> {
            Seq<Path> ret;

            try {
                final boolean involve = pathFilter.test(p);
                final boolean descent = maxDepth > 1 && Files.isDirectory(p) && recursionFilter.test(p);

                ret = descent
                        ? __PathSelector.createRecursive(pathFilter, recursionFilter, maxDepth - 1).list(p)
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
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final List<BiFunction<? super Path, ? super LinkOption[], ? extends Predicate<Path>>> pathIncludes;
        private final List<BiFunction<? super Path, ? super LinkOption[], ? extends Predicate<Path>>> pathExcludes;
        private final List<BiFunction<? super Path, ? super LinkOption[], ? extends Predicate<Path>>> recursionRestrictions;
        private boolean recursive;
        private boolean dontFollowSymbolicLinks;
        private int maxDepth;
        
        private Builder() { 
            this.pathIncludes = new ArrayList<>();
            this.pathExcludes = new ArrayList<>();
            this.recursionRestrictions = new ArrayList<>();
            this.recursive = false;
            this.dontFollowSymbolicLinks = false;
            this.maxDepth = Integer.MAX_VALUE; 
        }
        
        
        private Builder addPathPatternFilter(
                final String[] patterns,
                final boolean exclude,
                final boolean applyPatternJustOnName,
                BiPredicate<Path, LinkOption[]> pred) {
        
            assert pred != null;
            
            final Consumer<BiFunction<? super Path, ? super LinkOption[], ? extends Predicate<Path>>> adder =
                    exclude
                    ? this.pathExcludes::add
                    : this.pathIncludes::add;
            
            Seq.of(patterns)
                    .reject(pattern -> pattern == null || pattern.isEmpty())
                    .forEach(pattern ->  adder.accept(
                            (path, linkOptions) -> {
                                final PathMatcher pathMatcher =  path.getFileSystem()
                                        .getPathMatcher(pattern);

                                return path2 -> Files.isDirectory(path, linkOptions)
                                        && pathMatcher.matches(path2);
                            }
                    ));
        
            return this;            
        }
        /*
        private Builder addPatternPathInclude(
                final String[] patterns,
                BiPredicate<Path, LinkOption[]> pred) {
        
            assert pred != null;

            return this.addPathPatternFilter(patterns, false, pred);
        }

        private Builder addPatternPathExclude(
                final String[] patterns,
                BiPredicate<Path, LinkOption[]> pred) {
        
            assert pred != null;

            return this.addPathPatternFilter(patterns, true, pred);
        }

        private Builder addPatternRecursionRestriction(
                final String[] patterns,
                BiPredicate<Path, LinkOption[]>... preds) {
        
            assert preds != null;

            
            // TODO
            
            return this;
        }
        */
        private Builder addPathInclude(
                final BiPredicate<? super Path, ? super LinkOption[]> pred) {
            assert pred != null;
            this.pathIncludes.add((rootPath, linkOptions) -> path -> pred.test(path, linkOptions));
            return this;
        }
        
        private Builder addPathExclude(
                final BiPredicate<? super Path, ? super LinkOption[]> pred) {
            assert pred != null;
            this.pathExcludes.add((rootPath, linkOptions) -> path -> pred.test(path, linkOptions));
            return this;
        }

        private Builder addRecursionRestriction(final BiPredicate<? super Path, ? super LinkOption[]> pred) {
            assert pred != null;
            this.recursionRestrictions.add((rootPath, linkOptions) -> path -> pred.test(path, linkOptions));
            return this;
        }
        
        public Builder recursive() {
            this.recursive = true;
            return this;
        }

        public Builder includeAllRegularFiles() {
            this.addPathInclude((path, linkOptions) -> Files.isRegularFile(path, linkOptions));
            return this;
        }

        public Builder includeAllDirectories() {
            this.addPathInclude((path, linkOptions) -> Files.isDirectory(path, linkOptions));
            return this;
        }
        
        public Builder includeAllSymlinks() {
            this.addPathInclude((path, linkOptions) -> Files.isSymbolicLink(path));
            return this;
        }
        
        public Builder includeAll() {
            this.addPathInclude((path, linkOptions) -> true);
            return this;
        }
        
        public Builder include(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    false,
                    false,
                    (path, linkOptions) -> true);
        }

        public Builder includeRegularFiles(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    false,
                    false,
                    (path, linkOptions) -> Files.isRegularFile(path, linkOptions));
        }
        
        public Builder includeDirectories(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    false,
                    false,
                    (path, linkOptions) -> Files.isDirectory(path, linkOptions));
        }
        
        public Builder includeSymbolicLinks(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    false,
                    false,
                    (path, linkOptions) -> Files.isSymbolicLink(path));
        }

        public Builder includeByName(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    false,
                    true,
                    (path, linkOptions) -> true);
        }

        public Builder includeRegularFilesByName(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    false,
                    true,
                    (path, linkOptions) -> Files.isRegularFile(path, linkOptions));
        }
        
        public Builder includeDirectoriesByName(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    false,
                    true,
                    (path, linkOptions) -> Files.isDirectory(path, linkOptions));
        }
        
        public Builder includeSymbolicLinksByName(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    false,
                    true,
                    (path, linkOptions) -> Files.isSymbolicLink(path));
        }

        public Builder excludeAllRegularFiles() {
            return this.addPathExclude(
                    (path, linkOptions) -> Files.isRegularFile(path, linkOptions));
        }
        
        public Builder excludeAllDirectories() {
            return this.addPathExclude(
                    (path, linkOptions) -> Files.isDirectory(path, linkOptions));
        }
        
        public Builder excludeAllSymbolicLinks() {
            return this.addPathExclude(
                    (path, linkOptions) -> Files.isSymbolicLink(path));
        }
        
        public Builder exclude(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    true,
                    true,
                    (path, linkOptions) -> true);
         }
   
        
        public Builder excludeRegularFiles(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    true,
                    false,
                    (path, linkOptions) -> Files.isRegularFile(path, linkOptions));
         }
        
        public Builder excludeDirectories(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    true,
                    false,
                    (path, linkOptions) -> Files.isDirectory(path, linkOptions));
        }
        
        public Builder excludeSymbolicLinks(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    true,
                    false,
                    (path, linkOptions) -> Files.isSymbolicLink(path));
        }
        
        public Builder excludeByName(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    true,
                    true,
                    (path, linkOptions) -> true);
        }

        public Builder excludeRegularFilesByName(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    true,
                    true,
                    (path, linkOptions) -> Files.isRegularFile(path, linkOptions));
        }
        
        public Builder excludeDirectoriesByName(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    true,
                    true,
                    (path, linkOptions) -> Files.isDirectory(path, linkOptions));
        }
        
        public Builder excludeSymbolicLinksByName(final String... patterns) {
            return this.addPathPatternFilter(
                    patterns,
                    true,
                    true,
                    (path, linkOptions) -> Files.isSymbolicLink(path));
        }

       /* 
        public Builder restrictRecursion(final String... patterns) {
           return this.addPatternRecursionRestriction(patterns,
                    (path, linkOptions) -> true);
        }
        
        public Builder excludeFromRecursion(final String... patterns) {
           return this.addPatternRecursionRestriction(patterns,
                    (path, linkOptions) -> false);
        }

        public Builder restrictRecursionByName(final String... patterns) {
           return this.addPatternRecursionRestriction(patterns,
                    (path, linkOptions) -> true);
        }
        
        public Builder excludeFromRecursionByName(final String... patterns) {
           return this.addPatternRecursionRestriction(patterns,
                    (path, linkOptions) -> false);
        }
        */
        public Builder maxDepth(final int maxDepth) {
            if (maxDepth <= 0) {
                throw new IllegalArgumentException(
                        "IAE PathSelector::maxDepth(int maxDepth): maxDepth must be a positive integer number");
            }
            
            this.maxDepth = maxDepth;
            return this;
            
            
            
            
            
        }
        
        public Builder unlimitedDepth() {
            this.maxDepth(Integer.MAX_VALUE);
            return this;
        }
        
        public Builder dontFollowSymbolicLinks() {
            this.dontFollowSymbolicLinks = true;
            return this;
        }

        public __PathSelector build() {
            final LinkOption[] linkOptions = this.dontFollowSymbolicLinks
                    ? new LinkOption[] { LinkOption.NOFOLLOW_LINKS }
                    : new LinkOption[] {};
            
            final Function<Path, CheckedPredicate<Path, IOException>> f1 = rootPath -> path -> true; // TODO
            final Function<Path, CheckedPredicate<Path, IOException>> f2 = rootPath -> path -> true; // TODO
            
            return new __PathSelector(
                    path -> PathLister.createRecursive(
                        f1.apply(path),
                        f2.apply(path),
                        this.maxDepth).list(path));
        }
    }
}
