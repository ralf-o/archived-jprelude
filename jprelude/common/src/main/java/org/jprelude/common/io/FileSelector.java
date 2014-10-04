/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jprelude.common.io;

import org.jprelude.common.util.Generator;
import org.jprelude.common.util.Seq;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class FileSelector implements Predicate<File> {

    public static final FileSelector SELECT_ALL = new FileSelector(file -> true);

    public static final FileSelector SELECT_NONE = new FileSelector(file -> false);

    public static final FileSelector SELECT_FILES = new FileSelector(file -> (file != null && file.isFile()));

    public static final FileSelector SELECT_DIRECTORIES = new FileSelector(file -> file != null && file.isDirectory());
    
    private final Predicate<File> innerFilter;

    private FileSelector() {
        this.innerFilter = FileSelector.SELECT_ALL;
    }

    public FileSelector(Predicate<File> f) {
        assert (f != null);

        if (f instanceof FileSelector) {
            this.innerFilter = ((FileSelector) f).innerFilter;
        } else {
            this.innerFilter = f;
        }
    }

    public final boolean test(final File file) {
        return this.innerFilter.test(file);
    }

    public FileSelector filter(final Predicate<File> f) {
        assert (f != null);
        return new FileSelector(this.innerFilter.and(f));
    }

    public FileSelector whereFile() {
        return new FileSelector(this.innerFilter.and(new Predicate<File>() {
            public boolean test(final File file) {
                return (file != null && file.isFile());
            }
        }));
    }

    public FileSelector whereDirectory() {
        return new FileSelector(this.innerFilter.and(new Predicate<File>() {
            public boolean test(final File file) {
                return (file != null && file.isDirectory());
            }
        }));
    }

    public FileSelector whereVisible() {
        return new FileSelector(this.innerFilter.and(new Predicate<File>() {
            public boolean test(final File file) {
                return (file != null && !file.isHidden());
            }
        }));
    }

    public FileSelector whereHidden() {
        return new FileSelector(this.innerFilter.and(new Predicate<File>() {
            public boolean test(final File file) {
                return (file != null && file.isHidden());
            }
        }));
    }

    public FileSelector whereFileName(final String... values) {
        final List<String> valueList = Arrays.asList(values);

        return new FileSelector(this.innerFilter.and(new Predicate<File>() {
            public boolean test(final File file) {
                return (valueList.contains(file.getName()));
            }
        }));
    }

    public FileSelector whereFileName(final Predicate<? super String>... fs) {
        FileSelector ret = this;

        if (fs != null && fs.length > 0) {
            ret = new FileSelector(this.innerFilter.and(new Predicate<File>() {
                public boolean test(File file) {
                    boolean ret = false;

                    for (int i = 0; i < fs.length; ++i) {
                        if (fs[i] != null) {
                            ret |= fs[i].test(file.getName());
                        }
                    }

                    return ret;
                }
            }));
        }

        return ret;
    }

    public FileSelector wherePath(final String value) {
        return new FileSelector(this.innerFilter.and(new Predicate<File>() {
            public boolean test(final File file) {
                return (value != null && value.equals(file.getPath()));
            }
        }));
    }

    public FileSelector wherePath(final Predicate<? super String>... fs) {
        FileSelector ret = this;

        if (fs != null && fs.length > 0) {
            ret = new FileSelector(this.innerFilter.and(new Predicate<File>() {
                public boolean test(File file) {
                    boolean ret = false;

                    for (int i = 0; i < fs.length; ++i) {
                        if (fs[i] != null) {
                            ret |= fs[i].test(file.getPath());
                        }
                    }

                    return ret;
                }
            }));
        }

        return ret;
    }

    public FileSelector whereAbsolutePath(final Predicate<? super String>... fs) {
        FileSelector ret = this;

        if (fs != null && fs.length > 0) {
            ret = new FileSelector(this.innerFilter.and(new Predicate<File>() {
                public boolean test(File file) {
                    boolean ret = false;

                    for (int i = 0; i < fs.length; ++i) {
                        if (fs[i] != null) {
                            ret |= fs[i].test(file.getAbsolutePath());
                        }
                    }

                    return ret;
                }
            }));
        }

        return ret;
    }

    public FileSelector whereSize(final Predicate<? super Long>... fs) {
        FileSelector ret = this;

        if (fs != null && fs.length > 0) {
            ret = new FileSelector(this.innerFilter.and(new Predicate<File>() {
                public boolean test(File file) {
                    boolean ret = false;

                    for (int i = 0; i < fs.length; ++i) {
                        if (fs[i] != null) {
                            ret |= fs[i].test(file.length());
                        }
                    }

                    return ret;
                }
            }));
        }

        return ret;
    }

    public FileSelector whereModificationDate(final Predicate<? super Date>... fs) {
        FileSelector ret = this;

        if (fs != null && fs.length > 0) {
            ret = new FileSelector(this.innerFilter.and(new Predicate<File>() {
                public boolean test(File file) {
                    boolean ret = false;
                    GregorianCalendar cal = new java.util.GregorianCalendar();
                    cal.setTimeInMillis(file.lastModified());
                    Date modificationDate = cal.getTime();

                    for (int i = 0; i < fs.length; ++i) {
                        if (fs[i] != null) {
                            ret |= fs[i].test(modificationDate);
                        }
                    }

                    return ret;
                }
            }));
        }

        return ret;
    }

    public FileSelector whereModificationAge(final Predicate<? super Long>... fs) {
        FileSelector ret = this;

        if (fs != null && fs.length > 0) {
            ret = new FileSelector(this.innerFilter.and(new Predicate<File>() {
                public boolean test(File file) {
                    boolean ret = false;

                    for (int i = 0; i < fs.length; ++i) {
                        if (fs[i] != null) {
                            ret |= fs[i].test(file.lastModified());
                        }
                    }

                    return ret;
                }
            }));
        }

        return ret;
    }

    public FileSelector whereSuffix(final String... values) {
        final List<String> valueList = Arrays.asList(values);

        return new FileSelector(this.innerFilter.and(new Predicate<File>() {
            public boolean test(final File file) {
                return (valueList.contains(FileUtils.getSuffix(file)));
            }
        }));
    }

    public FileSelector whereSuffix(final Predicate<? super String>... fs) {
        FileSelector ret = this;

        if (fs != null && fs.length > 0) {
            ret = new FileSelector(this.innerFilter.and(new Predicate<File>() {
                public boolean test(File file) {
                    boolean ret = false;

                    for (int i = 0; i < fs.length; ++i) {
                        if (fs[i] != null) {
                            ret |= fs[i].test(FileUtils.getSuffix(file));
                        }
                    }

                    return ret;
                }
            }));
        }

        return ret;
    }

    public FileSelector and(final FileSelector... selectors) {
        FileSelector ret = this;

        if (selectors != null && selectors.length > 0) {
            Predicate<File> filter = this.innerFilter;

            for (int i = 0; i < selectors.length; ++i) {
                filter = filter.and(selectors[i].innerFilter);
            }

            ret = new FileSelector(filter);
        }

        return ret;
    }

    public FileSelector or(final FileSelector... selectors) {
        FileSelector ret = this;

        if (selectors != null && selectors.length > 0) {
            Predicate<File> filter = this.innerFilter;

            for (int i = 0; i < selectors.length; ++i) {
                filter = filter.or(selectors[i].innerFilter);
            }

            ret = new FileSelector(filter);
        }

        return ret;
    }

    /*
    public FileSelector xor(final FileSelector... selectors) {
        FileSelector ret = this;

        if (selectors != null && selectors.length > 0) {
            Predicate<File> filter = this.innerFilter;

            for (int i = 0; i < selectors.length; ++i) {
                filter = filter.xor(selectors[i].innerFilter);
            }

            ret = new FileSelector(filter);
        }

        return ret;
    }
    */

    public FileSelector negate() {
        return new FileSelector(file -> !this.innerFilter.test(file));
    }

    public FileFilter toFileFilter() {
        return new FileFilter() {
            public boolean accept(File file) {
                return FileSelector.this.innerFilter.test(file);
            }
        };
    }

    public Seq<File> listDirectory(final File directory) {
        return this.listDirectory(directory, false, null);
    }

    public Seq<File> listDirectory(final File directory, final boolean recursive) {
        return this.listDirectory(directory, recursive, null);
    }

    public Seq<File> listDirectory(final File directory, final boolean recursive, Predicate<File> directoryPredicate) {
        assert (directory != null);

        final Predicate<File> dirPredicate = (directoryPredicate != null ? directoryPredicate : SELECT_DIRECTORIES);

        Seq<File> ret = Seq.empty();

        if (directory.isDirectory()) {
            if (!recursive) {
                ret = Seq.from(directory.listFiles(this.toFileFilter()));
            } else {
                final Predicate<File> filter = new Predicate<File>() {
                    public boolean test(File file) {
                        return (file != null && (file.isDirectory() || dirPredicate.test(file)));
                    }
                };

                final File[] files = directory.listFiles(new FileSelector(filter).toFileFilter());
                final int fileCount = (files == null ? 0 : files.length);

                if (fileCount > 0) {
                    ret = Seq.from(new Iterable<File>() {
                        public Iterator<File> iterator() {
                            return new Generator<File>() {
                                private int idx = 0;
                                private Iterator<File> itr = null;

                                public void generate() {
                                    if (this.idx < fileCount || this.itr != null) {
                                        if (this.itr != null) {
                                            if (!this.itr.hasNext()) {
                                                this.itr = null;
                                            } else {
                                                this.yield(this.itr.next());
                                            }
                                        }

                                        if (this.itr == null && this.idx < fileCount) {
                                            File file = files[this.idx];
                                            boolean included = FileSelector.this.test(file);
                                            ++this.idx;

                                            if (included) {
                                                this.yield(file);
                                            }

                                            if (file.isDirectory()) {
                                                this.itr = FileSelector.this.listDirectory(file, true, FileSelector.this).stream().iterator();

                                                if (!included) {
                                                    this.generate();
                                                }
                                            }
                                        }
                                    }
                                }
                            };
                        }
                    });
                }
            }
        }

        return ret;
    }
    
    public static FileSelector of(final FileFilter fileFilter) {
        return new FileSelector(file -> fileFilter.accept(file));
    }
}
