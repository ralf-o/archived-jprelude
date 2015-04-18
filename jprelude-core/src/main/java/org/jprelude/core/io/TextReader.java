package org.jprelude.core.io;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jprelude.core.io.function.IOSupplier;
import org.jprelude.core.util.Mutable;
import org.jprelude.core.util.Seq;
import org.jprelude.core.util.function.CheckedConsumer;
import org.jprelude.core.util.function.CheckedSupplier;

public final class TextReader {
    final String sourceName;
    final Charset charset;
    final boolean autoCloseInputStream;
    final IOSupplier<InputStream> inputStreamSupplier;
    
    private TextReader(
            final String sourceName,
            final IOSupplier<InputStream> inputStreamSupplier,
            final Charset charset,
            final boolean autoCloseInputStream) {
        assert inputStreamSupplier != null;
        
        this.sourceName = (sourceName == null || sourceName.trim().isEmpty() ? null : sourceName.trim());
        this.charset = charset != null ? charset : Charset.defaultCharset();
        this.autoCloseInputStream = autoCloseInputStream;
        this.inputStreamSupplier = inputStreamSupplier;
    }
    
    public static TextReader from(final InputStream inputStream) {
        Objects.requireNonNull(inputStream);
        
        return TextReader.from(inputStream, null);
    }
    
    public static TextReader from(final InputStream inputStream, final Charset charset) {
        Objects.requireNonNull(inputStream);

        final Mutable<BufferedReader> bufferedReader = Mutable.empty();
        final Charset nonNullCharset = charset != null ? charset : Charset.defaultCharset();
        
        final IOSupplier<InputStream> inputStreamSupplier = () -> inputStream;
        
        return new TextReader(
                "InputStream",
                inputStreamSupplier,
                nonNullCharset,
                false);
    }    
    
    public static TextReader from(final Path path, final Charset charset) {
        Objects.requireNonNull(path);

        final Mutable<BufferedReader> bufferedReader = Mutable.empty();
        final Charset nonNullCharset = charset != null ? charset : Charset.defaultCharset();
        
        return new TextReader(
                path.toString(),
                () -> Files.newInputStream(path),
                nonNullCharset,
                true);
    }

    public static TextReader from(final Path path) {
        Objects.requireNonNull(path);
        return TextReader.from(path, Charset.defaultCharset());
    }

    public static TextReader from(final File file, final Charset charset) {
       Objects.requireNonNull(file);
       return TextReader.from(file.toPath(), charset);
    }

    public static TextReader from(final File file) {
       Objects.requireNonNull(file);
       return TextReader.from(file.toPath());
    }
    
    public Charset getCharset() {
        return this.charset;
    }
    
    public static TextReader from(final String text) {
        Objects.requireNonNull(text);

        return new TextReader(
                "String",
                () -> new ByteArrayInputStream(text.getBytes()),
                StandardCharsets.UTF_8,
                true);
    }

    public String readAsString() throws IOException {
        final CharBuffer charBuffer = CharBuffer.allocate(8096);
        final StringBuilder strBuilder = new StringBuilder();
        final BufferedReader bufferedReader = this.newBufferedReader();
        
        try {
            while (bufferedReader.read(charBuffer) > 0) {
               strBuilder.append(charBuffer.toString());
            }
        } catch (final Throwable throwable) {
            this.handleError(throwable);
        }
         
        return strBuilder.toString();
    }
    
    public List<String> readAsList() throws IOException {
        List<String> ret = null;
        
        try {
            ret = this.readAsSeq().collect(Collectors.toList());
        } catch (final UncheckedIOException exception) {
            throw exception.getCause();
        } catch (final Throwable throwable) {
            throw new IOException(throwable);
        }

        return ret;
    }
    
    public String[] readAsArray() throws IOException {
        return (String[]) this.readAsList().toArray();
    }
    
    public Seq<String> readAsSeq() {
        return Seq.from(() -> {
            final BufferedReader bufferedReader;
            
            try {
                bufferedReader = this.newBufferedReader();
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
                                TextReader.this.handleError(throwable);
                            } catch (final IOException e) {
                                throw new UncheckedIOException(e);
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
    
    public void read(final CheckedConsumer<InputStream> consumer) throws IOException {
        Objects.requireNonNull(consumer);
        
        try (final InputStream inputStream = this.newInputStream()) {
            consumer.accept(inputStream);
        } catch (final Throwable throwable) {
            this.handleError(throwable);
        }
     }
    
    public InputStream newInputStream() throws IOException {
        final InputStream ret;
        
        if (this.autoCloseInputStream) {
            ret = this.inputStreamSupplier.get();
        } else {
            return new InputStream() {
                private InputStream inputStream = TextReader.this.inputStreamSupplier.get();

                @Override
                public int read() throws IOException {
                    if (this.inputStream == null) {
                        throw new IOException("Input stream is already closed");
                    }

                    return this.inputStream.read();
                }

                @Override
                public void close() throws IOException {
                    final InputStream in = this.inputStream;

                    if (in != null) {
                        this.inputStream = null;

                        if (TextReader.this.autoCloseInputStream) {
                            in.close();
                        }
                    }
                }
            };
        }
        
        return ret;
    }
    
    private BufferedReader newBufferedReader() throws IOException {
        BufferedReader ret = null;
        
        try {
            ret = new BufferedReader(new InputStreamReader(this.newInputStream()));
        } catch (final Throwable throwable) {
            this.handleError(throwable);
        }
        
        return ret;
    }
    
    private void handleError(final Throwable cause) throws IOException {     
        if (cause != null) {
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new IOException(cause);
            }
        } else {
            final StringBuilder errorMsgBuilder = new StringBuilder();

            errorMsgBuilder.append("Error while reading");

            if (this.sourceName != null) {
                errorMsgBuilder.append(" ");
                errorMsgBuilder.append(this.sourceName);
            }

            throw new IOException(errorMsgBuilder.toString());
        }
    }
}
