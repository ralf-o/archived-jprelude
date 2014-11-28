package org.jprelude.common.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;
import org.jprelude.common.util.Seq;

public class TextWriter {
    final PrintStream printStream;
    final Path path;
    final Charset charset;
    final OpenOption[] options;
    
    public TextWriter(final Path path) {
        this.printStream = null;
        this.path = path;
        this.charset = Charset.defaultCharset();
        this.options = new OpenOption[] {};
    }
    
    public TextWriter(final File file) {
        this(file.toPath());
    }
    
    public TextWriter(final String file) {
        this(new File(file).toPath());
    }
    
    public TextWriter(final PrintStream printStream) {
        this.printStream = printStream;
        this.path = null;
        this.charset = null;
        this.options = null;
    }
    
    public long writeLines(final Seq<?> lines) throws IOException {
        int counter = 0;
       
        if (this.printStream != null) {
            final Iterator<?> iterator = lines.stream().iterator();
            
            while (iterator.hasNext()) {
                final Object token = iterator.next();
                
                if (token != null) {
                    this.printStream.println(token.toString());
                    ++counter;
                }
            }
        } else if (lines != null && this.path != null) {
            final CharsetEncoder encoder = charset.newEncoder();
            final OutputStream out = Files.newOutputStream(path, options);
            
            try (final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, encoder));
                    final Stream<?> stream = lines.stream()) {
                final Iterator<?> iterator = stream.iterator();
                
                while (iterator.hasNext()) {
                    final Object line = iterator.next();
                    
                    if (line != null) {
                       writer.append(line.toString());
                    }
                       
                   writer.newLine();
                   ++counter;
                }
            }
        }

        return counter;        
    }
}
