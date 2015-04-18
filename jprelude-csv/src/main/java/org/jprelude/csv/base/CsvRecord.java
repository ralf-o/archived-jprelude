package org.jprelude.csv.base;

import java.util.Objects;
import org.apache.commons.csv.CSVRecord;

public final class CsvRecord {
    final CSVRecord apacheCommonsCsvRecord;
    
    CsvRecord(final CSVRecord apacheCommonsCsvRecord) {
        Objects.requireNonNull(apacheCommonsCsvRecord);
        
        this.apacheCommonsCsvRecord = apacheCommonsCsvRecord;
    }
    
    public String get(final int columnIdx) {
        return this.apacheCommonsCsvRecord.get(columnIdx);
    }
    
    public String get(final String columnName) {
        return this.apacheCommonsCsvRecord.get(columnName);
    }
    
    public String get(final Enum<?> column) {
        return this.apacheCommonsCsvRecord.get(column);
    }
        
    public long index() {
        return this.apacheCommonsCsvRecord.getRecordNumber() - 1;
    }
}
