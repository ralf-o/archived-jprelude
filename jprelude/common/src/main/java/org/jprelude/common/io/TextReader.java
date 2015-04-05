package org.jprelude.common.io;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jprelude.common.util.Seq;

public final class TextReader {
    private final IOSupplier<BufferedReader> bufferedReaderSupplier;
    private final boolean autoCloseBufferedReader;
    
    
    private TextReader(final IOSupplier<BufferedReader> bufferedReaderSupplier, final boolean autoCloseBufferedReader) {
        assert bufferedReaderSupplier != null;
 
        this.bufferedReaderSupplier = bufferedReaderSupplier;
        this.autoCloseBufferedReader = autoCloseBufferedReader;
    }
    
    public static TextReader from(final Reader reader) {
        Objects.requireNonNull(reader);
        
        return new TextReader(() -> 
            reader instanceof BufferedReader
                    ? (BufferedReader) reader
                    : new BufferedReader(reader),
            false);
    }
    
    public static TextReader from(final InputStream inputStream) {
        Objects.requireNonNull(inputStream);

        return new TextReader(() -> new BufferedReader(new InputStreamReader(inputStream)), false);        
    }
    
    
    public static TextReader from(final Path path, final Charset charset) {
        Objects.requireNonNull(path);
        
        return new TextReader(
                () ->  Files.newBufferedReader(path, charset),
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

    public String readAsString() throws IOException {
        final CharBuffer charBuffer = CharBuffer.allocate(8096);
        final StringBuilder strBuilder = new StringBuilder();
        final BufferedReader bufferedReader = this.bufferedReaderSupplier.get();
        
        try {
            while (bufferedReader.read(charBuffer) > 0) {
               strBuilder.append(charBuffer.toString());
            }
        } finally {
            if (this.autoCloseBufferedReader) {
                bufferedReader.close();
            }
        }
           
        return strBuilder.toString();
    }
    
    public List<String> readAsList() throws IOException {
        final List<String> ret = new ArrayList<>();
        final BufferedReader bufferedReader = this.bufferedReaderSupplier.get();
    
        try {
            bufferedReader.lines().forEach(line -> {
                ret.add(line);
            });
        } finally {
            if (this.autoCloseBufferedReader) {
                bufferedReader.close();
            }
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
                bufferedReader = this.bufferedReaderSupplier.get();
            } catch (final IOException e)  {
                throw new UncheckedIOException(e.getMessage(), e);
            }
            
            return bufferedReader.lines();
        });
    }
}
