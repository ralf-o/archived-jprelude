package org.jprelude.csv.base;

import org.jprelude.core.io.TextReader;
import org.jprelude.core.util.Seq;
import org.jprelude.core.util.function.CheckedFunction;

public class CsvImporter<T> {
    private CsvImporter(final Builder builder) {
        
    }
    
    
    public Seq<T> parse(final TextReader textReader) {
        return null;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder<T> {
        public Builder format(final CsvFormat format) {
            return this;
        }
        
        public Builder recordMapper(final CheckedFunction<CsvRecord, Seq<T>> mapper) {
            return this;
        }
        
        public CsvImporter build() {
            return new CsvImporter(this);
        }
    }
}
