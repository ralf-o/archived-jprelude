package org.jprelude.common.csv;

import java.util.function.Consumer;
import org.jprelude.common.function.Command;

public final class CsvExportResult {
    private final Status status;
    private final Throwable error;
    private final long recordCount;
    
    private CsvExportResult(final Builder builder) {
        assert builder != null;
        
        this.status = builder.status;
        this.error = builder.error;
        this.recordCount = builder.recordCount; 
    }
        
    public Status getStatus() {
        return this.status;
    }
    
    public Throwable getError() {
        return this.error;
    }
    
    public long getRecordCount() {
        return this.recordCount;
    }
    
    public CsvExportResult doOnSuccess(final Command command) {
        if (command != null && this.status == Status.SUCCESS) {
            command.execute();
        }
        
        return this;
    };
    
    public CsvExportResult doOnSuccess(final Consumer<CsvExportResult> consumer) {
        if (consumer != null && this.status == Status.SUCCESS) {
            consumer.accept(this);
        }
        
        return this;
    };

    public CsvExportResult doOnError(final Command command) {
        if (command != null && this.status == Status.ERROR) {
            command.execute();
        }
        
        return this;
    };
    
    public CsvExportResult doOnError(final Consumer<Throwable> consumer) {
        if (consumer != null && this.status == Status.ERROR) {
            consumer.accept(this.error);
        }
        
        return this;
    };

    public CsvExportResult throwOnError() throws Throwable {
        if (this.status == Status.ERROR) {
            throw this.error;
        }
        
        return this;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum Status {
        SUCCESS, ERROR;
    }
    
    public static class Builder {
        private Status status = null;
        private Throwable error = null;
        private long recordCount = 0;
        
        private Builder() {
            status = null;
            error = null;
            recordCount = 0;
        }
        
        public Builder status(final Status status) {
            this.status = status == null ? null : status;
            return this;
        }
        
        public Builder error(final Throwable throwable) {
            this.error = throwable;
            return this;
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
