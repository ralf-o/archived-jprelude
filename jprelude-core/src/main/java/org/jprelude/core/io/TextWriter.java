package org.jprelude.core.io;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import org.jprelude.core.io.function.IOBiConsumer;
import org.jprelude.core.io.function.IOConsumer;
import org.jprelude.core.io.function.IOSupplier;
import org.jprelude.core.util.LineSeparator;
import org.jprelude.core.util.Seq;

public interface TextWriter {
    Charset getCharset();

    OutputStream newOutputStream() throws IOException;

    default void writeLines(final Seq<?> lines) throws IOException {
        Objects.requireNonNull(lines);
        
        this.writeLines(lines, LineSeparator.LF);
    }
    
    default void writeLines(final Seq<?> lines, LineSeparator lineSeparator)
            throws IOException {
        
        Objects.requireNonNull(lines);
 
        final String lineSeparatorValue =
                (lineSeparator == null || lineSeparator == LineSeparator.NONE)
                ? ""
                : lineSeparator.value();


        this.write(printStream -> Seq.sequential(lines).forEach(line -> {
            printStream.print(Objects.toString(line, ""));
            printStream.print(lineSeparatorValue);

            if (printStream.checkError()) {
                throw new UncheckedIOException(
                        new IOException(
                                "Could not write to PrintStream - checkError() returned true"));
            }
        }));        
    }
    
    default void writeFullText(final Object text) throws IOException {
        this.writeLines(Seq.of(Objects.toString(text, "")), LineSeparator.NONE);
    }
    
    
    default void write(final IOConsumer<PrintStream> delegate)
            throws IOException {
        
        this.write((printStream, charset) -> delegate.accept(printStream));
    }
    
   default void write(final IOBiConsumer<PrintStream, Charset> delegate)
            throws IOException {
        
        Objects.requireNonNull(delegate);
        
        final Charset charset = this.getCharset();
        
        final Charset nonNullCharset = charset != null
                ?  charset
                : StandardCharsets.UTF_8;
        
        try (
                final OutputStream outputStream = this.newOutputStream();
                
                final PrintStream printStream = new PrintStream(
                        new BufferedOutputStream(outputStream),
                        true,
                        nonNullCharset.name())) {

            delegate.accept(printStream, charset);

            if (printStream.checkError()) {
                throw new IOException(
                        "Could not write to PrintStream - "
                        + "checkError() returned true");
            }
        }
    }

    static TextWriter create(
            final IOSupplier<OutputStream> outputStreamSupplier) {
    
        Objects.requireNonNull(outputStreamSupplier);
        
        return TextWriter.create(outputStreamSupplier, StandardCharsets.UTF_8);
    }
   
    static TextWriter create(
            final IOSupplier<OutputStream> outputStreamSupplier,
            final Charset charset) {
       
        Objects.requireNonNull(outputStreamSupplier);
       
        final Charset nonNullCharset = charset != null
                ? charset
                : StandardCharsets.UTF_8;
        
        return new TextWriter() {
            @Override
            public OutputStream newOutputStream() throws IOException {
                return outputStreamSupplier.get();
            }
            
            @Override
            public Charset getCharset() {
                return nonNullCharset;
            }
        };
    }
    
    static TextWriter create(
            final Path path,
            final OpenOption... openOptions) {
     
        Objects.requireNonNull(path);
        
        return TextWriter.create(path, null, openOptions);
    }
    
    static TextWriter create(
            final Path path,
            final Charset charset,
            final OpenOption... openOptions) {
        
        Objects.requireNonNull(path);
       
        final OpenOption[] options = Seq.of(openOptions)
                .filter(option -> option != null)
                .prepend(StandardOpenOption.WRITE)
                .toArray(OpenOption[]::new);
        
        return TextWriter.create(
                () -> Files.newOutputStream(path, options),
                charset != null ? charset : StandardCharsets.UTF_8);
    }
    
    static TextWriter create(final OutputStream outputStream) {
        Objects.requireNonNull(outputStream);
        
        return TextWriter.create(outputStream, null);
    }
    
    static TextWriter create(
            final OutputStream outputStream,
            final Charset charset) {
            
        Objects.requireNonNull(outputStream);

        final IOSupplier<OutputStream> supplier = () -> new OutputStream() {
            private boolean isClosed = false;
            
            @Override
            public void write(int b) throws IOException {
                if (!isClosed) {
                    outputStream.write(b);
                } else {
                    throw new IOException(
                            "Output stream is already closed");
                }
            }

            @Override
            public void flush() throws IOException {
                if (!isClosed) {
                    outputStream.flush();
                }
            }

            @Override
            public void close() throws IOException {
                isClosed = true;
            }
        };
        
        return TextWriter.create(supplier, charset);
    }            
}
