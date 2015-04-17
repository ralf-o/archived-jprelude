package org.jprelude.csv;

public final class CsvExportResult {
    private final long sourceRecordCount;
    private final long targetRowCount;
    
    private CsvExportResult(final Builder builder) {
        assert builder != null;
        
        this.sourceRecordCount = builder.sourceRecordCount;
        this.targetRowCount = builder.targetRowCount;
    }
        
    public long getSourceRecordCount() {
        return this.sourceRecordCount;
    }
    
    public long getTargetRowCount() {
        return this.targetRowCount;
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long sourceRecordCount = 0;
        private long targetRowCount = 0;
        
        private Builder() {
            sourceRecordCount = 0;
            targetRowCount = 0;
        }
        
        public Builder sourceRecordCount(final long sourceRecordCount) {
            this.sourceRecordCount = sourceRecordCount;
            return this;
        }
        
        public Builder targetRowCount(final long targetRowCount) {
            this.targetRowCount = targetRowCount;
            return this;
        }
        
        public CsvExportResult build() {
            return new CsvExportResult(this);
        }
    }
}
