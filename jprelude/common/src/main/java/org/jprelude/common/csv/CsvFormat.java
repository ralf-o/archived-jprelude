package org.jprelude.common.csv;

public final class CsvFormat {
    private final String delimiter;
    private final String recordSeparator;
    private final boolean autoTrim;
            
    
    private CsvFormat(final Builder builder) {
        this.delimiter = builder.delimiter;
        this.recordSeparator = builder.recordSeparator;
        this.autoTrim = builder.autoTrim;
    }
    
    public String getDelimiter() {
        return this.delimiter;
    }
    
    public String getRecordSeparator() {
        return this.recordSeparator;
    }
    
    public boolean isAutoTrim() {
        return this.autoTrim;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static Builder builder(final CsvFormat prototype) {
        return new Builder(prototype);
    }
            
    public static final class Builder {
        private String delimiter;
        private String recordSeparator;
        private boolean autoTrim;
        
        private Builder() {
            this.delimiter = ",";
            this.recordSeparator = "\r\n";
            this.autoTrim = false;         
        }
        
        private Builder(final CsvFormat prototype) {
            if (prototype != null) {
                this.delimiter = prototype.delimiter;
                this.recordSeparator = prototype.recordSeparator;
                this.autoTrim = prototype.autoTrim;
            }
        }
        
        public Builder delimiter(final String delimiter) {
            if (delimiter == null || delimiter.isEmpty()) {
                throw new IllegalArgumentException("First parameter must not be null or empty string");
            }
            
            this.delimiter = delimiter;
            return this;
        }
        
        public Builder recordSeparator(final String recordSeparator) {
            if (recordSeparator == null || recordSeparator.isEmpty()) {
                throw new IllegalArgumentException("First parameter must not be null or empty string");
            }
            
            this.recordSeparator = recordSeparator;
            return this;
        }
        
        public Builder autoTrim(final boolean autoTrim) {
            this.autoTrim = autoTrim;
            return this;
        }
        
        public CsvFormat build() {
            return new CsvFormat(this);
        }
    }         
}
