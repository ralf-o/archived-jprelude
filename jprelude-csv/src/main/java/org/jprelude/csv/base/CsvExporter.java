package org.jprelude.csv.base;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.jprelude.core.io.TextWriter;
import org.jprelude.core.util.Seq;

public class CsvExporter<T> {
    private final CsvFormat format;
    private final Function<T, Seq<List<?>>> multiMapper;
    
    private CsvExporter(final Builder<T> builder) {
        
        assert builder.format != null;
        assert builder.multiMapper != null;
        
        this.format = builder.format;
        this.multiMapper = builder.multiMapper;
    }
    
    public CsvFormat getFormat() {
        return this.format;
    }
    
    public CsvExportResult export(final Seq<T> entities, final TextWriter target) {
        Objects.requireNonNull(entities);
        Objects.requireNonNull(target);
        
        final Map<CsvExporter<T>, TextWriter> exporterMap = new HashMap<>();
        exporterMap.put(this, target);
        
        return CsvExporter.export(entities, exporterMap).get(this);
    }
        
    public static <T>  Map<CsvExporter<T>, CsvExportResult> export(
            final Seq<T> entities,
            final Map<CsvExporter<T>, TextWriter> exporterMap) {
        
        final Map<CsvExporter<T>, CsvExportResult> ret = new HashMap<>();
        
        Throwable error = null;
        
        final List<CsvExporter<T>> nonNullExporters = Seq
                .from(exporterMap.keySet())
                .rejectNulls()
                .toList();
                
        final Map<CsvExporter, PrintStream> printStreamMap = new HashMap<>(); 
        final Map<CsvExporter, CsvExportResult.Builder> resultBuilderMap = new HashMap<>();
        
        try {
            for (final CsvExporter exporter : nonNullExporters) {
                final TextWriter target = exporterMap.get(exporter);
                
                if (target == null) {
                    continue;
                }
                
                printStreamMap.put(exporter,
                        new PrintStream(
                                new BufferedOutputStream(target.newOutputStream()),
                                true,
                                target.getCharset().name()));
            }
        } catch (final IOException e) {
            error = e;
        }
        
        if (error == null) {
            try {
                entities.sequential().forEach((entity, n) -> {
                    nonNullExporters.forEach(exporter -> {
                        final PrintStream printStream = printStreamMap.get(exporter);
                        final Function<List<?>, String> mapper = exporter.format.asMapper();
                        final String recordSeparator = exporter.format.getRecordSeparator().value();
                        
                        if (n == 0L) {
                            final List<?> row = Seq.from(exporter.format.getColumns())
                                    .map(col -> col.getName()).toList();
                            
                            printStream.print(mapper.apply(row));
                            printStream.print(recordSeparator);
                        }
                        
                        Seq<List<?>> rows = exporter.multiMapper.apply(entity);

                        rows.forEach(row -> {
                            printStream.print(mapper.apply(row));
                            printStream.print(recordSeparator);
                        });

                        
                        if (printStream.checkError()) {
                            throw new UncheckedIOException(
                                    new IOException("Could not write to PrintStream - checkError() return true"));
                        }
                    });
                });
            } catch (final Throwable throwable) {
                error = throwable;
            }
        }

        for (final PrintStream printStream : printStreamMap.values()) {
            try {
                printStream.close();
            } catch (final Throwable throwable) {
                  if (error == null) {
                    error = throwable;
                } else {
                    error.addSuppressed(throwable);
                }
            }          
        }
        
        if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        } else if (error instanceof IOException) {
            throw new UncheckedIOException((IOException) error);
        } else if (error != null) {
            throw new RuntimeException(error);
        }
        
        resultBuilderMap.forEach(
                (exporter, builder) -> ret.put(exporter, builder.build()));
        
        return ret;
    }
    
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
    
    public static class Builder<T> {
        private CsvFormat format;
        private Function<T, Seq<List<?>>> multiMapper;

        private Builder() {
            this.format = null;
            this.multiMapper = null;
        }
        
        public Builder<T> format(final CsvFormat format) {
            Objects.requireNonNull(format);
            
            this.format = format;
            return this;
        }
        
        public Builder<T> mapper(final Function<T, List<?>> mapper) {
            Objects.requireNonNull(mapper);
            
            this.multiMapper = entity -> Seq.of(mapper.apply(entity));
            return this;
        }
        
        public Builder<T> multiMapper(final Function<T, Seq<List<?>>> multiMapper) {
            Objects.requireNonNull(multiMapper);
            
            this.multiMapper = multiMapper;
            return this;
        }
        
        public CsvExporter<T> build() {
            return new CsvExporter<>(this);
        }
    }
}
