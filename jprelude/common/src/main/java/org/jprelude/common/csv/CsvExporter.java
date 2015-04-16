package org.jprelude.common.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.jprelude.common.io.TextWriter;
import org.jprelude.common.util.Observer;
import org.jprelude.common.util.Seq;

public final class CsvExporter<T> {
    private final CsvFormat format;
    private final Function<? super T, ? extends Seq<List<?>>> recordMapper;
    private final TextWriter target;
    private final List<Observer<? super T>> sourceRecordsObservers;
    private final List<Observer<? super String>> targetRowsObservers;
    
    private CsvExporter(final Builder<T> builder) {
        assert builder != null;
        
        if (builder.recordMapper == null) {
            throw new IllegalArgumentException("Builder of CsvExporter does not provide record mapper");
        }
        
        if (builder.target == null) {
            throw new IllegalArgumentException("Builder of CsvExporter does not provide text writer");
        }
        
        this.format = builder.format;
        this.recordMapper =  builder.recordMapper;
        this.target = builder.target;
        this.sourceRecordsObservers = builder.sourceRecordsObservers;
        this.targetRowsObservers = builder.targetRowsObservers;    
    }
    
    public CsvFormat getFormat() {
        return this.format;
    }
    
    public Function<? super T, ? extends Seq<List<?>>> getRecordMapper() {
        return this.recordMapper;
    }
    
    public TextWriter getTarget() {
        return target;
    }
    
    public CsvExportResult export(final Seq<T> records) {
        Objects.requireNonNull(records, "NPE CsvExporter::export(records)");
     
        return CsvMultiExporter.<T>builder()
                .addExporter("myExport", this)
                .build()
                .export(records)
                .get("myExport");
    }
    
    public static <T> Builder builder() {
        return new Builder<>();
    }
    
    public static <T> Builder<T> builder(final CsvExporter<T> prototype) {
        final Builder<T> builder = CsvExporter.builder();
        builder.format = prototype.format;
        builder.recordMapper = prototype.recordMapper;
        builder.target =  prototype.target;
        return builder;
    }
    
    public static class Builder<T> implements Cloneable {
        private CsvFormat format;
        private Function<? super T, ? extends Seq<List<?>>> recordMapper;
        private TextWriter target;
        private List<Observer<? super T>> sourceRecordsObservers;
        private List<Observer<? super String>> targetRowsObservers;
                
        private Builder() {
            this.format = CsvFormat.builder().build();
            this.recordMapper = null;
            this.target =  null;
            this.sourceRecordsObservers = new ArrayList<>();
            this.targetRowsObservers = new ArrayList<>();
        }
                
        public Builder recordMapper(final Function<? super T, ? extends Seq<List<?>>> recordMapper) {
            this.recordMapper = recordMapper;
            return this;
        }
        
        public Builder target(final TextWriter target) {
            this.target = target;
            return this;
        }
        
        public Builder sourceRecordsObserver(final Observer<? super T> observer) {
            return this.sourceRecordsObservers(observer);
        }
        
        public Builder sourceRecordsObservers(final Observer<? super T>... observers) {
            this.sourceRecordsObservers(Seq.of(observers).toList());
            return this;
        }
        
        public Builder sourceRecordsObservers(final Iterable<Observer<? super T>> observers) {
            this.sourceRecordsObservers.clear();
            
            if (observers != null) {
                observers.forEach(observer -> this.addSourceRecordsObserver(observer));
            }
            
            return this;
        }
        
        public Builder addSourceRecordsObserver(final Observer<? super T> observer) {
            if (observer != null) {
                this.sourceRecordsObservers.add(observer);
            }
            
            return this;
        }
        
        public Builder targetRowsObserver(final Observer<? super String> observer) {
            this.targetRowsObservers(observer);
            return this;
        }

        public Builder targetRowsObservers(final Observer<? super String>... observers) {
            this.targetRowsObservers(Seq.of(observers).toList());
            return this;
        }
        
        public Builder targetRowsObservers(final Iterable<Observer<? super String>> observers) {
            this.targetRowsObservers.clear();
            
            if (observers != null) {
                observers.forEach(observer -> this.addTargetRowsObserver(observer));
            }
            
            return this;
        }
        
        public Builder addTargetRowsObserver(final Observer<? super String> observer) {
            if (observer != null) {
                this.targetRowsObservers.add(observer);
            }
            
            return this;
        }
        
        public CsvExporter build() {
            return new CsvExporter(this);
        }
        
        public Builder<T> copy() {
            final Builder<T> ret = new Builder();
            ret.format = this.format;
            ret.recordMapper = this.recordMapper;
            ret.target = this.target;
            ret.sourceRecordsObservers = this.sourceRecordsObservers;
            ret.targetRowsObservers = this.targetRowsObservers;
            return ret;
        }
    }
}
