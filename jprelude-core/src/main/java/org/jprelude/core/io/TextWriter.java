package org.jprelude.core.io;

import java.io.BufferedOutputStream;
import org.jprelude.core.io.function.IOSupplier;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import org.jprelude.core.io.function.IOCommand;
import org.jprelude.core.io.function.IOConsumer;
import org.jprelude.core.util.LineSeparator;
import org.jprelude.core.util.Seq;

public final class TextWriter {
    final String targetName;
    final Charset charset;
    final boolean autoCloseOutputStream;
    final IOSupplier<OutputStream> outputStreamSupplier;
    final IOCommand onSuccess;
    final IOCommand onError;
    
    private TextWriter(
            final String targetName,
            final IOSupplier<OutputStream> outputStreamSupplier,
            final Charset charset,
            final boolean autoCloseOutputStream,
            final IOCommand onSuccess,
            final IOCommand onError) {
        assert outputStreamSupplier != null;
        assert onSuccess != null;
        assert onError != null;
        
        this.targetName = (targetName == null || targetName.trim().isEmpty() ? null : targetName.trim());
        this.charset = charset != null ? charset : Charset.defaultCharset();
        this.autoCloseOutputStream = autoCloseOutputStream;
        this.outputStreamSupplier = outputStreamSupplier;
        this.onSuccess = onSuccess;
        this.onError = onError;
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
        
        final Charset nonNullCharset = charset != null ? charset : Charset.defaultCharset();
        
        final OpenOption[] openOptions = append
                ? new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND }
                : new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.WRITE };
        
        return new TextWriter(
                path.toString(),
                () -> Files.newOutputStream(path, openOptions),
                charset,
                true,
                () -> {},
                () -> {});
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
        return TextWriter.from(outputStream, null);
    }
    
    public static TextWriter from(final OutputStream outputStream, final Charset charset) {
        Objects.requireNonNull(outputStream);
        final Charset nonNullCharset = charset != null ? charset : Charset.defaultCharset();
        
        return new TextWriter(
                "OutputStream", () -> outputStream,
                charset,
                false,
                () -> {},
                () -> {});
    }
       
    public Charset getCharset() {
        return this.charset;
    }

    public void writeLines(final Seq<?> lines) throws IOException {
        this.writeLines(lines, null);
    }

    public void writeLines(final Seq<?> lines, LineSeparator lineSeparator) throws IOException {
        boolean isError = false;
        
        try (final PrintStream printStream = this.newPrintStream()) {   
            if (lineSeparator == null) {
                Seq.sequential(lines).forEach(line -> {
                    printStream.print(line == null ? "" : line.toString());
                });
            } else {
                final String sep = lineSeparator.getValue();
                
                Seq.sequential(lines).forEach(line -> {
                    printStream.print(line == null ? "" : line.toString());
                    printStream.print(sep);
                });                
            }
            
            if (printStream.checkError()) {
                isError = true;
            }
        } catch (final Throwable throwable) {
            this.handleError(throwable);
        }
        
        if (isError) {
            this.handleError(null);
        } else {
            this.handleSuccess();
        }
    }
    
    public void writeFullText(final CharSequence text) throws IOException {
        this.writeLines(Seq.of(text), LineSeparator.NONE);
    }
    
    public void write(final IOConsumer<PrintStream> delegate) throws IOException {
        Objects.requireNonNull(delegate);
        PrintStream printStream = null;
        
        try (final PrintStream out = this.newPrintStream()) {
            printStream = out;
            delegate.accept(out);
        } catch (final Throwable throwable) {System.out.println(throwable + throwable.getMessage());
            this.handleError(throwable);
        }
        
        assert printStream != null;
        
        if (printStream.checkError()) {
            this.handleError(null);
        } else {
            this.onSuccess.execute();
        }
    }
    
    public OutputStream newOutputStream() throws IOException {
        final OutputStream ret;
        
        if (this.autoCloseOutputStream) {
            ret = this.outputStreamSupplier.get();
        } else {
            ret = new OutputStream() {
                private OutputStream outputStream = TextWriter.this.outputStreamSupplier.get();

                @Override
                public void write(int b) throws IOException {
                    if (this.outputStream != null) {
                        this.outputStream.write(b);
                    } else {
                        throw new IOException("Output stream is already closed");
                    }
                }

                @Override
                public void flush() throws IOException {
                    if (this.outputStream != null) {
                        this.outputStream.flush();
                    }
                }

                @Override
                public void close() throws IOException {
                    if (this.outputStream != null) {
                        final OutputStream outStream = this.outputStream;
                        this.outputStream = null;

                        if (TextWriter.this.autoCloseOutputStream) {
                            outStream.close();
                        }
                    }
                }
            };
        }
        
        return ret;
    }
    
    private PrintStream newPrintStream() throws IOException {
        return new PrintStream(new BufferedOutputStream(this.newOutputStream()), true, this.charset.name());
    }
    
    private void closePrintStream(final PrintStream printStream) throws IOException {
        assert printStream != null;
 
        printStream.close();
 
        if (printStream.checkError()) {
            this.handleError(null);
        }
    }
    
    private void handleSuccess() throws IOException {
        try {
            this.onSuccess.execute();
        } catch (final Throwable throwable) {
            this.handleError(throwable);
        }
    }
    
    private void handleError(final Throwable cause) throws IOException {
        try {
            this.onError.execute();
        } catch (final Throwable t) {
        }
      
        if (cause == null || cause.getMessage() == null || cause.getMessage().isEmpty()) {
            final StringBuilder errorMsgBuilder = new StringBuilder();

            errorMsgBuilder.append("Error while writing");

            if (this.targetName != null) {
                errorMsgBuilder.append(" ");
                errorMsgBuilder.append(this.targetName);
            }

            throw new IOException(errorMsgBuilder.toString(), cause);            
        }
        
        if (cause instanceof IOException) {
            throw (IOException) cause;
        } else {
            throw new IOException(cause);
        }
    }
}
