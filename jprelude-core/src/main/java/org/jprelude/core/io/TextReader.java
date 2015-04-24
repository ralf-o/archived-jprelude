package org.jprelude.core.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.jprelude.core.util.Seq;
import org.jprelude.core.util.function.CheckedBiConsumer;
import org.jprelude.core.util.function.CheckedConsumer;
import org.jprelude.core.util.function.CheckedSupplier;

public interface TextReader {
    InputStream newInputStream() throws Exception;
    Charset getCharset();
    URI getUri();
    
    default String readFullText() throws Exception {
        final CharBuffer charBuffer = CharBuffer.allocate(8096);
        final StringBuilder strBuilder = new StringBuilder();
                
        try(
                final InputStream inputStream = this.newInputStream();
                final BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(inputStream, this.getCharset().name()))) {
                
            while (bufferedReader.read(charBuffer) > 0) {
               strBuilder.append(charBuffer.toString());
            }
        }
         
        return strBuilder.toString();
    }

    default void read(final CheckedConsumer<InputStream> delegate) throws Exception {
        this.read((inputStream, charset) -> delegate.accept(inputStream));
    };
    
    default void read(final CheckedBiConsumer<InputStream, Charset> delegate) throws Exception {
        Objects.requireNonNull(delegate);
        
        try (final InputStream inputStream = this.newInputStream()) {
            delegate.accept(inputStream, this.getCharset());
        } catch (final Throwable throwable) {
            if (throwable instanceof UncheckedIOException) {
                throw (UncheckedIOException) throwable;
            } else if (throwable instanceof IOException) {
                throw (IOException) throwable;
            } else {
                throw new IOException("Error while reading", throwable);
            }
        }       
    };
    
    default Seq<String> readLines() {
        return Seq.from(() -> {
            final BufferedReader bufferedReader;

            try {
                final InputStream inputStream = this.newInputStream();
                
                bufferedReader = new BufferedReader(new InputStreamReader(
                        inputStream, this.getCharset()));
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            } catch (final RuntimeException e) {
                throw (RuntimeException) e;
            } catch (final Throwable e) {
                throw  new RuntimeException(e);
            }

            final Iterator<String> iter = new Iterator<String>() {
                private String nextLine = null;

                @Override
                public boolean hasNext() {
                    if (this.nextLine != null) {
                        return true;
                    } else {
                        boolean ret = false;

                        try {
                            this.nextLine = bufferedReader.readLine();
                            ret = this.nextLine != null;
                        } catch (final Throwable throwable) {
                            try {
                                bufferedReader.close();
                            } catch (final IOException e) {
                                throwable.addSuppressed(e);
                            }
                            
                            if (throwable instanceof IOException) {
                                throw new UncheckedIOException((IOException) throwable);
                            } else if (throwable instanceof RuntimeException) {
                                throw (RuntimeException) throwable;
                            } else {
                                throw new RuntimeException(throwable);
                            }
                        }

                        return ret;
                    }
                }

                @Override
                public String next() {
                    if (this.nextLine == null || !this.hasNext()) {
                        throw new NoSuchElementException();
                    }

                    final String line = this.nextLine;
                    this.nextLine = null;
                    return line;
                }
            };

            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                    iter, Spliterator.ORDERED | Spliterator.NONNULL), false);
        });
    }
    
    public static TextReader create(
            final CheckedSupplier<InputStream> inputStreamSupplier,
            final Charset charset,
            final URI uri) {
        
        Objects.requireNonNull(inputStreamSupplier);
        
        final Charset nonNullCharset = charset != null
                ? charset
                : StandardCharsets.UTF_8;
        
        return new TextReader() {
            @Override
            public InputStream newInputStream() throws Exception {
                final InputStream ret;
                
                try {
                    ret = inputStreamSupplier.get();
                } catch (final Exception e) {
                    throw e;
                } catch (final Throwable e) {
                    throw new RuntimeException(e);
                }
                
                return ret;
            }

            @Override
            public Charset getCharset() {
                return nonNullCharset;
            }
            
            @Override
            public URI getUri() {
                return uri;
            }
        };
    }
        
    public static TextReader forFile(
            final Path path,
            OpenOption... openOptions) {
        
        Objects.requireNonNull(path);
        
        return TextReader.forFile(path, StandardCharsets.UTF_8, openOptions);
    }
    
    public static TextReader forFile(
            final Path path,
            final Charset charset,
            final OpenOption... openOptions) {
        
        Objects.requireNonNull(path);

        final OpenOption[] options = Seq.of(openOptions)
            .filter(option -> option != null)
            .prepend(StandardOpenOption.READ)
            .toArray(OpenOption[]::new);

        return TextReader.create(
                () -> Files.newInputStream(path, options),
                charset,
                path.toUri());
    }
    
    public static TextReader forInputStream(final InputStream inputStream) {
        return TextReader.forInputStream(inputStream, null);
    }
    
    public static TextReader forInputStream(final InputStream inputStream, final Charset charset) {
        Objects.requireNonNull(inputStream);
        
        final CheckedSupplier<InputStream> supplier = () -> new InputStream() {
            private boolean isClosed = false;

            @Override
            public int read() throws IOException {
                final int ret;
                
                if (!this.isClosed) {
                    ret = inputStream.read();
                } else {
                    throw new IOException("InputStream is already closed");
                }
                
                return ret;
            }
            
            @Override
            public void close() throws IOException {
                this.isClosed = true;
            }
        };
        
        return TextReader.create(
                supplier,
                charset != null ? charset : StandardCharsets.UTF_8,
                null);
    }
    
    static TextReader forString(final String text) {
        Objects.requireNonNull(text);

        return TextReader.create(
                () -> new ByteArrayInputStream(text.getBytes("UTF-8")),
                StandardCharsets.UTF_8,
                null);
    }
    
    
}