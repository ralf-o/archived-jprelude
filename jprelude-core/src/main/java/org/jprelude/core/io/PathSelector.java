package org.jprelude.core.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jprelude.core.util.Seq;
import org.jprelude.core.util.function.CheckedPredicate;

public interface PathSelector {

    Seq<Path> list(final Path path);

    public static Builder builder() {
        return new Builder();
    }
   
    public static Builder builder(final Builder prototype) {
        final Builder ret = new Builder();
        
        ret.recursive = prototype.recursive;
        ret.maxDepth = prototype.maxDepth;
        ret.linkOptions = Seq.of(prototype.linkOptions).toArray(LinkOption[]::new);
        ret.includes.addAll(prototype.includes);
        ret.excludes.addAll(prototype.excludes);
        ret.recursionRestrictions.addAll(prototype.recursionRestrictions);
        return ret;
    }

    public final static class Builder {
        private boolean recursive;
        private int maxDepth;
        private LinkOption[] linkOptions;
        private final List<CheckedPredicate<? super PathEntry, IOException>> includes;
        private final List<CheckedPredicate<? super PathEntry, IOException>> excludes;
        private final List<CheckedPredicate<? super PathEntry, IOException>> recursionRestrictions;

        private Builder() {
            this.maxDepth = Integer.MAX_VALUE;
            this.recursive = false;
            this.linkOptions = new LinkOption[]{};
            this.includes = new ArrayList<>();
            this.excludes = new ArrayList<>();
            this.recursionRestrictions = new ArrayList<>();
        }

        public Builder recursive() {
            this.recursive = true;
            return this;
        }

        public Builder recursive(final CheckedPredicate<? super PathEntry, IOException> recursionRestriction) {
            this.recursive = true;

            if (recursionRestriction != null) {
                this.recursionRestrictions.add(recursionRestriction);
            }

            return this;
        }

        public Builder maxDepth(final int maxDepth) {
            if (maxDepth <= 0) {
                throw new IllegalArgumentException(
                        "IAE PathSelector.Builder.maxDepth(maxDepth)");
            }

            this.maxDepth = maxDepth;
            return this;
        }
        
        public Builder unlimitedDepth() {
            this.maxDepth = Integer.MAX_VALUE;
            return this;
        }

        public Builder dontFollowSymbolicLinks() {
            this.linkOptions = new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
            return this;
        }

        public Builder include(final CheckedPredicate<? super PathEntry, IOException>... preds) {
            Objects.requireNonNull(preds);

            Seq.from(preds)
                    .rejectNulls()
                    .forEach(pred -> this.includes.add(pred));

            return this;
        }

        public Builder exclude(final CheckedPredicate<? super PathEntry, IOException>... preds) {
            Objects.requireNonNull(preds);

            Seq.from(preds)
                    .rejectNulls()
                    .forEach(pred -> this.excludes.add(pred));

            return this;
        }

        public Builder restrictRecursion(final CheckedPredicate<? super PathEntry, IOException>... preds) {
            Objects.requireNonNull(preds);

            final List<CheckedPredicate<? super PathEntry, IOException>> filteredPreds = Seq.of(preds)
                    .rejectNulls()
                    .toList();

            this.recursionRestrictions.add(
                    pathEntry -> filteredPreds.stream().anyMatch(
                            CheckedPredicate.unchecked(pred -> pred.test(pathEntry))));

            return this;
        }

        public Builder include(final String... patterns) {
            Seq.of(patterns)
                    .rejectNulls()
                    .forEach(pattern -> this.include(p -> p.matches(pattern)));

            return this;
        }

        public Builder exclude(final String... patterns) {
            Seq.of(patterns)
                    .rejectNulls()
                    .forEach(pattern -> this.exclude(p -> p.matches(pattern)));

            return this;
        }
        
        public Builder includeAll() {
            this.include(p -> true);
            return this;
        }

        public Builder includeAllRegularFiles() {
            this.include(PathEntry::isRegularFile);
            return this;
        }

        public Builder includeAllDirectories() {
            this.include(PathEntry::isDirectory);
            return this;
        }

        public Builder includeAllSymbolicLinks() {
            this.include(PathEntry::isSymbolicLink);
            return this;
        }

        public Builder includeRegularFiles(final String... patterns) {
            Seq.of(patterns)
                    .rejectNulls()
                    .forEach(pattern -> this.include(
                                    p -> p.isRegularFile() && p.matches(pattern)));

            return this;
        }

        public Builder includeDirectories(final String... patterns) {
            Seq.of(patterns)
                    .rejectNulls()
                    .forEach(pattern -> this.include(
                                    p -> p.isDirectory() && p.matches(pattern)));

            return this;
        }

        public Builder includeSymbolicLinks(final String... patterns) {
            Seq.of(patterns)
                    .rejectNulls()
                    .forEach(pattern -> this.include(
                                    p -> p.isSymbolicLink() && p.matches(pattern)));

            return this;
        }

        public Builder excludeAllRegularFiles() {
            this.exclude(PathEntry::isRegularFile);
            return this;
        }

        public Builder excludeAllDirectories() {
            this.exclude(PathEntry::isDirectory);
            return this;
        }

        public Builder excludeAllSymbolicLinks() {
            this.exclude(PathEntry::isSymbolicLink);
            return this;
        }

        public Builder excludeRegularFiles(final String... patterns) {
            Seq.of(patterns)
                    .rejectNulls()
                    .forEach(pattern -> this.exclude(
                                    p -> p.isRegularFile() && p.matches(pattern)));

            return this;
        }

        public Builder excludeDirectories(final String... patterns) {
            Seq.of(patterns)
                    .rejectNulls()
                    .forEach(pattern -> this.exclude(
                                    p -> p.isDirectory() && p.matches(pattern)));

            return this;
        }

        public Builder excludeSymbolicLinks(final String... patterns) {
            Seq.of(patterns)
                    .rejectNulls()
                    .forEach(pattern -> this.exclude(
                                    p -> p.isSymbolicLink() && p.matches(pattern)));

            return this;
        }

        public Builder restrictRecursion(final String... patterns) {
            final List<String> filteredPatterns = Seq.of(patterns)
                    .rejectNulls()
                    .toList();

            this.recursionRestrictions.add(
                    pathEntry -> filteredPatterns.stream().anyMatch(
                            pattern -> pathEntry.matches(pattern)));

            return this;
        }

        public Builder excludeFromRecursion(final String... patterns) {
            Seq.of(patterns)
                    .rejectNulls()
                    .forEach(pattern -> this.restrictRecursion(
                                    path -> !path.matches(pattern)));

            return this;
        }

        public PathSelector build() {
            return new PathSelector() {
                final boolean recursive = Builder.this.recursive;
                final int maxDepth = Builder.this.maxDepth;
                final LinkOption[] linkOptions = Builder.this.linkOptions;
                final List<CheckedPredicate<? super PathEntry, IOException>> includes = new ArrayList<>(Builder.this.includes);
                final List<CheckedPredicate<? super PathEntry, IOException>> excludes = new ArrayList<>(Builder.this.excludes);

                final List<CheckedPredicate<? super PathEntry, IOException>> recursionRestrictions
                        = new ArrayList<>(Builder.this.recursionRestrictions);

                @Override
                public Seq<Path> list(final Path path) {
                    Objects.requireNonNull(path);

                    return this.listDirectoryFilteredAndRecursive(
                            Builder.createPathEntry(path, linkOptions),
                            maxDepth).map(PathEntry::getPath);
                }

                private Seq<PathEntry> listDirectoryFilteredAndRecursive(
                        final PathEntry pathEntry,
                        final int maxDepth) {

                    assert pathEntry != null;

                    return this.listDirectoryUnfilteredAndNonrecursive(pathEntry.getPath()).flatMap(subPath -> {
                        Seq<PathEntry> ret;
                        final PathEntry subPathEntry = Builder.createPathEntry(subPath);

                        try {
                            final boolean involve
                                    = !excludes.stream().anyMatch(
                                            CheckedPredicate.unchecked(
                                                    pred -> pred.test(subPathEntry)))
                                    && includes.stream().anyMatch(
                                            CheckedPredicate.unchecked(
                                                    pred -> pred.test(subPathEntry)));

                            final boolean descent
                                    = this.recursive
                                    && maxDepth > 1
                                    && subPathEntry.isDirectory()
                                    && !recursionRestrictions.stream()
                                    .anyMatch(
                                            CheckedPredicate.unchecked(
                                                    pred -> !pred.test(subPathEntry)));

                            ret = descent
                                    ? this.listDirectoryFilteredAndRecursive(
                                            subPathEntry,
                                            maxDepth - 1)
                                    : (!involve ? Seq.empty() : Seq.of(subPathEntry));

                            if (descent && involve) {
                                ret = ret.prepend(subPathEntry);
                            }
                        } catch (final RuntimeException e) {
                            throw e;
                        } catch (final Throwable e) {
                            throw new RuntimeException(e);
                        }

                        return ret;
                    });
                }

                private Seq<Path> listDirectoryUnfilteredAndNonrecursive(
                        final Path path) {

                    assert path != null;

                    return Seq.from(() -> {
                        final Stream<Path> ret;

                        try {
                            final DirectoryStream<Path> dirStream = Files.newDirectoryStream(path, file -> true);

                            ret = StreamSupport.stream(dirStream.spliterator(), false);
                        } catch (final IOException e) {
                            throw new UncheckedIOException(e);
                        }

                        return ret;
                    });
                }
            };
        }

        
        private static Map<FileSystem, Map<String, PathMatcher>> patternMatcherCache =
                new ConcurrentHashMap<>();

 
        private static PathEntry createPathEntry(final Path path, final LinkOption... linkOptions) {
            Objects.requireNonNull(path);

            final LinkOption[] filteredLinkOptions = Seq.of(linkOptions)
                    .rejectNulls()
                    .toArray(LinkOption[]::new);

            return new PathEntry() {
                private PathEntry byName = null;
                
                @Override
                public Path getPath() {
                    return path;
                }

                @Override
                public LinkOption[] getDefaultLinkOptions() {
                    return filteredLinkOptions;
                }
                
                @Override
                public boolean matches(final String... patterns) {
                    return Seq.of(patterns)
                            .rejectNulls()
                            .anyMatch(pattern -> this.checkMatching(
                                    path, pattern)); 
                }
                
                private boolean checkMatching(final Path path, final String pattern) {
                    assert path != null;
                    
                    final FileSystem fileSystem = path.getFileSystem();
                    
                    final Map<String, PathMatcher> map = Builder
                            .patternMatcherCache
                            .computeIfAbsent(fileSystem, fs ->
                                new LinkedHashMap<String, PathMatcher>(201, 0.75f, true) {
                                    @Override
                                    protected boolean removeEldestEntry(final Map.Entry<String, PathMatcher> eldest) {
                                        return size() > 200;
                                    }                                    
                                });
                
                    final PathMatcher pathMatcher = map.computeIfAbsent(
                            pattern,
                            pat -> {
                                final String syntaxAndPattern =
                                        pattern.matches("^[a-zA-z][a-zA-Z0-9]*\\:$")
                                        ? pattern
                                        : "glob:" + pattern;
                                
                                return fileSystem
                                        .getPathMatcher(syntaxAndPattern);
                            });
                    
                    return pathMatcher.matches(path);
                }
                
                @Override
                public PathEntry byName() {
                    return this.pathEntryByName;
                }

                private final PathEntry pathEntryByName = new PathEntry() {
                    @Override
                    public Path getPath() {
                        return path.getFileName();
                    }

                    @Override
                    public LinkOption[] getDefaultLinkOptions() {
                        return linkOptions;
                    }

                };
            };
        }
    }
}
