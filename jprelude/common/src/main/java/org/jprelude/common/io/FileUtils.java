/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jprelude.common.io;

import org.jprelude.common.util.Seq;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import static java.nio.file.Files.newOutputStream;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 *
 * @author kenny
 */
public final class FileUtils {

    public static String getSuffix(final File file) {
        String ret = "";

        if (file != null) {
            String fileName = file.getName();

            if (fileName != null) {
                int dotPos = fileName.lastIndexOf(".");

                if (dotPos > 0) {
                    ret = fileName.substring(dotPos + 1);
                }
            }
        }

        return ret;
    }

    public static Seq<String> lines(final File file) {
        final Supplier<Stream<String>> supplier = () -> {
            final Stream<String> ret;

            try {
                ret = new BufferedReader(new FileReader(file)).lines();
            } catch (final FileNotFoundException e) {
                throw new UncheckedIOException(e);
            }

            return ret;
        };

        return Seq.buildBy(supplier);
    }
    
    public static long writeToFile(final Path path, final Seq<?> lines, Charset charset, final OpenOption... options) throws IOException {
        final int[] counter = { 0 };
        
        if (lines != null && path != null) {
            final Seq<?> seq = lines.peek((line) -> ++counter[0]);

            CharsetEncoder encoder = charset.newEncoder();
            OutputStream out = newOutputStream(path, options);
            
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
    
    public static long writeToFile(final Path path, final Seq<?> lines) throws IOException {
        return FileUtils.writeToFile(path, lines, Charset.defaultCharset());
    }
    
    public static long writeToFile(final File file, final Seq<?> lines) throws IOException {
        final long ret;
        
        if (file != null) {
            ret = 0;
        } else {
            final Path path = file.toPath();
            ret = FileUtils.writeToFile(path, lines);
        }
        
        return ret;
    }
    
    public static Seq<File> listDirectory(final File directory, final FileSelector selector) {
        return selector.listDirectory(directory);
    }
    
    public static Seq<File> listDirectoryRecursively(final File directory, final FileSelector selector) {
        return selector.listDirectory(directory, true);
    }
}
