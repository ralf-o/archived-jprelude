package org.jprelude.core.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
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
import org.jprelude.core.io.function.IOBiConsumer;
import org.jprelude.core.io.function.IOConsumer;
import org.jprelude.core.io.function.IOSupplier;
import org.jprelude.core.util.Seq;

public interface TextReader {
    InputStream newInputStream() throws IOException;
    Charset getCharset();
    
    default String readFullText() throws IOException {
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

    default void read(final IOConsumer<InputStream> delegate) throws IOException {
        this.read((inputStream, charset) -> delegate.accept(inputStream));
    };
    
    default void read(final IOBiConsumer<InputStream, Charset> delegate) throws IOException {
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
            } catch (final IOException e)  {
                throw new UncheckedIOException(e);
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
            final IOSupplier<InputStream> inputStreamSupplier) {
        
        Objects.requireNonNull(inputStreamSupplier);
        
        return TextReader.create(inputStreamSupplier, null);
    }
    
    public static TextReader create(
            final IOSupplier<InputStream> inputStreamSupplier,
            final Charset charset) {
        
        Objects.requireNonNull(inputStreamSupplier);
        
        final Charset nonNullCharset = charset != null
                ? charset
                : StandardCharsets.UTF_8;
        
        return new TextReader() {
            @Override
            public InputStream newInputStream() throws IOException {
                return inputStreamSupplier.get();
            }

            @Override
            public Charset getCharset() {
                return nonNullCharset;
            }
        };
    }
        
    public static TextReader create(
            final Path path,
            OpenOption... openOptions) {
        
        Objects.requireNonNull(path);
        
        return TextReader.create(path, StandardCharsets.UTF_8, openOptions);
    }
    
    public static TextReader create(
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
                charset);
    }
    
    public static TextReader create(final InputStream inputStream) {
        return TextReader.create(inputStream, null);
    }
    
    public static TextReader create(final InputStream inputStream, final Charset charset) {
        Objects.requireNonNull(inputStream);
        
        final IOSupplier<InputStream> supplier = () -> new InputStream() {
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
                charset != null ? charset : StandardCharsets.UTF_8);
    }
}