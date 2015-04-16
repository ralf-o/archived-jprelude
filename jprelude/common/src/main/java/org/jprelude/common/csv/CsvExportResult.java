package org.jprelude.common.csv;

import java.util.function.Consumer;
import org.jprelude.common.function.Command;

public final class CsvExportResult {
    private final Status status;
    private final Throwable error;
    private final long sourceRecordCount;
    private final long targetRowCount;
    
    private CsvExportResult(final Builder builder) {
        assert builder != null;
        
        this.status = builder.status;
        this.error = builder.error;
        this.sourceRecordCount = builder.sourceRecordCount;
        this.targetRowCount = builder.targetRowCount;
    }
        
    public Status getStatus() {
        return this.status;
    }
    
    public Throwable getError() {
        return this.error;
    }
    
    public long getSourceRecordCount() {
        return this.sourceRecordCount;
    }
    
    public long getTargetRowCount() {
        return this.targetRowCount;
    }
    
    public CsvExportResult ifSuccess(final Command command) {
        if (command != null && this.status == Status.SUCCESS) {
            command.execute();
        }
        
        return this;
    };
    
    public CsvExportResult ifSuccess(final Consumer<CsvExportResult> consumer) {
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
    
    public CsvExportResult ifError(final Consumer<Throwable> consumer) {
        if (consumer != null && this.status == Status.ERROR) {
            consumer.accept(this.error);
        }
        
        return this;
    };

    public CsvExportResult ifError(final Command command) {
        if (command != null && this.status == Status.ERROR) {
            command.execute();
        }
        
        return this;
    };

    
    public CsvExportResult ifErrorThrow() throws Throwable {
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
        private long sourceRecordCount = 0;
        private long targetRowCount = 0;
        
        private Builder() {
            status = null;
            error = null;
            sourceRecordCount = 0;
            targetRowCount = 0;
        }
        
        public Builder status(final Status status) {
            this.status = status == null ? null : status;
            return this;
        }
        
        public Builder error(final Throwable throwable) {
            this.error = throwable;
            return this;
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
