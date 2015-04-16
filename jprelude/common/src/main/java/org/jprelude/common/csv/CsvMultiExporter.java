package org.jprelude.common.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jprelude.common.util.Observer;
import org.jprelude.common.util.Seq;

public final class CsvMultiExporter<T> {
    final Map<String, CsvExporter<T>> exportersByID;
    private final List<Observer<? super T>> sourceRecordsObservers;
    
    
    private CsvMultiExporter(final Builder builder) {
        assert builder != null;
        this.exportersByID = new HashMap<>(builder.exporters);
        this.sourceRecordsObservers = new ArrayList<>(builder.sourceRecordsObservers);
    }
    
    public Map<String, CsvExportResult> export(final Seq<T> records) {
        Objects.requireNonNull(records, "NPE CsvMultiExporter::export(records)");
        
        final Map<String, CsvExportResult> ret = new HashMap<>();
        final List<Observer<T>> observers = new ArrayList();
        
        this.exportersByID.keySet().forEach(id -> { // TODO
        });
        
        return ret;
    }
    
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
    
    public static <T> Builder<T> builder(final CsvMultiExporter<T> prototype) {
        Objects.requireNonNull(prototype);
        
        final Builder<T> ret = new Builder<>();
        ret.exporters.putAll(prototype.exportersByID);
        ret.sourceRecordsObservers.addAll(prototype.sourceRecordsObservers);    
        return ret;
    }
    
    public static class Builder<T> {
        private final Map<String, CsvExporter<T>> exporters = new HashMap<>();
        private final List<Observer<? super T>> sourceRecordsObservers = new ArrayList<>();
        
        public Builder<T> exports(final Map<String, CsvExporter<T>> exporters) {
            this.exporters.clear();
            
            if (this.exporters != null) {
                exporters.forEach((String id, CsvExporter<T> exporter) -> this.addExporter(id, exporter));
            }
            
            return this;
        }
        
        public Builder<T> addExporter(final String id, final CsvExporter<T> exporter) {
            if (exporter != null) {
                this.exporters.put(id, exporter);
            }

            return this;
        }
        
        public Builder<T> addExporters(final Map<String, CsvExporter<T>> exporters) {
            if (exporters != null) {
                exporters.keySet().forEach(key -> this.addExporter(key, exporters.get(key)));
            }
            
            return this;
        }
        
        public CsvMultiExporter<T> build() {
            return new CsvMultiExporter<>(this);
        }
        
        public Builder<T> copy() {
            final Builder<T> ret = new Builder();
            ret.exporters.putAll(this.exporters);
            ret.sourceRecordsObservers.addAll(this.sourceRecordsObservers);
            return ret;
        }
    }
}
