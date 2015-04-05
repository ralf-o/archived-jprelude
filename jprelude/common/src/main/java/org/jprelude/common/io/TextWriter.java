package org.jprelude.common.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import org.jprelude.common.util.Seq;

public class TextWriter {
    final IOSupplier<PrintStream> printStreamSupplier;
    final boolean autoClosePrintStream;
    
    private TextWriter(final IOSupplier<PrintStream> printStreamSupplier, final boolean autoClosePrintStream) {
        assert printStreamSupplier != null;
        this.printStreamSupplier = printStreamSupplier;
        this.autoClosePrintStream = autoClosePrintStream;
    }
    
    public static TextWriter from(final Path path) {
        Objects.requireNonNull(path);
        return TextWriter.from(path, false, Charset.defaultCharset());        
    }
    
    public static TextWriter from(final Path path, final boolean append) {
        Objects.requireNonNull(path);
        return TextWriter.from(path, append, Charset.defaultCharset());
    }
    
    public static TextWriter from(final Path path, final Charset charset) {
        Objects.requireNonNull(path);
        return TextWriter.from(path, false, charset);        
    }
    
    public static TextWriter from(final Path path, final boolean append, final Charset charset) {
        Objects.requireNonNull(path);
        
        final OpenOption[] openOptions = append
                ? new OpenOption[] {StandardOpenOption.WRITE, StandardOpenOption.APPEND}
                : new OpenOption[] {StandardOpenOption.WRITE    };
        
        return new TextWriter(() -> new PrintStream(Files.newOutputStream(path, openOptions)), true);
    }
    
    public static TextWriter from(final File file) {
        Objects.requireNonNull(file);
        return TextWriter.from(file.toPath(), false, Charset.defaultCharset());        
    }

    public static TextWriter from(final File file, final boolean append) {
        Objects.requireNonNull(file);
        return TextWriter.from(file.toPath(), append, Charset.defaultCharset());
    }
    
    public static TextWriter from(final File file, final Charset charset) {
        Objects.requireNonNull(file);
        return TextWriter.from(file.toPath(), false, charset);        
    }
    
    public static TextWriter from(final File file, final boolean append, final Charset charset) {
        Objects.requireNonNull(file);
        return TextWriter.from(file.toPath(), append, charset);
    }
    
    public static TextWriter from(final OutputStream outputStream) {
        Objects.requireNonNull(outputStream);

        return new TextWriter(() ->
            outputStream instanceof PrintStream
                    ? (PrintStream) outputStream
                    : new PrintStream(outputStream),
            false
        );
    }

    public void writeLines(final Seq<?> lines) throws IOException {
        final PrintStream printStream = this.printStreamSupplier.get();

        try {
            Seq.sequential(lines).forEach(line -> {
                printStream.println(line == null ? "" : line.toString());
            });
        } catch (final UncheckedIOException e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            if (this.autoClosePrintStream) {
                printStream.close();
            }
        }
    }
    
    public void write(final IOConsumer<PrintStream> printStreamConsumer) throws IOException {
        if (printStreamConsumer != null) {
            final PrintStream printStream = this.printStreamSupplier.get();
            
            try {
                printStreamConsumer.accept(printStream);                
            } catch (final UncheckedIOException e) {
                throw new IOException(e.getMessage(), e);
            } finally {
                if (this.autoClosePrintStream) {
                    printStream.close();
                }
            }
        }
    }
}
