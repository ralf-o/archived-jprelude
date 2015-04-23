package org.jprelude.csv.base;

public final class CsvExportResult {
    private final long recordCount;
    
    private CsvExportResult(final Builder builder) {
        assert builder != null;
        
        this.recordCount = builder.recordCount;
    }
        
    public long getRecordCount() {
        return this.recordCount;
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long recordCount = 0;
        
        private Builder() {
            recordCount = 0;
        }
        
        public Builder recordCount(final long recordCount) {
            this.recordCount = recordCount;
            return this;
        }
        
        public CsvExportResult build() {
            return new CsvExportResult(this);
        }
    }
}
