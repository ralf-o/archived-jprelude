package org.jprelude.common.csv;

import java.util.Objects;
import org.apache.commons.csv.CSVRecord;

public final class CsvRecord {
    final CSVRecord apacheCommonsCsvRecord;
    
    CsvRecord(final CSVRecord apacheCommonsCsvRecord) {
        Objects.requireNonNull(apacheCommonsCsvRecord);
        
        this.apacheCommonsCsvRecord = apacheCommonsCsvRecord;
    }
    
    public String get(int columnIdx) {
        return this.apacheCommonsCsvRecord.get(columnIdx);
    }
    
    public String get(String columnName) {
        return this.apacheCommonsCsvRecord.get(columnName);
    }
        
    long index() {
        return this.apacheCommonsCsvRecord.getRecordNumber() - 1;
    }
}
