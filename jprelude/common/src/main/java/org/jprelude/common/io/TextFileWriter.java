package org.jprelude.common.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.stream.Stream;
import org.jprelude.common.util.Seq;

public class TextFileWriter {
    final Path path;
    final Charset charset;
    final OpenOption[] options;
    
    public TextFileWriter(final Path path) {
        this.path = path;
        this.charset = Charset.defaultCharset();
        this.options = new OpenOption[] {};
    }
    
    public TextFileWriter(final File file) {
        this(file.toPath());
    }
    
    public TextFileWriter(final String file) {
        this(new File(file).toPath());
    }
    
    public long write(final Seq<?> lines) throws IOException {
        final int[] counter = { 0 };
        
        if (lines != null && this.path != null) {
            final Seq<?> seq = lines.peek((line) -> ++counter[0]);

            CharsetEncoder encoder = charset.newEncoder();
            OutputStream out = Files.newOutputStream(path, options);
            
            try (final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, encoder));
                    final Stream<?> stream = seq.stream()) {
                final Iterator<?> iterator = stream.iterator();
                
                while (iterator.hasNext()) {
                    final Object line = iterator.next();
                    
                    if (line != null) {
                       writer.append(line.toString());
                       writer.newLine();
                    }
                }
            }
        }

        return counter[0];        
    }
}
