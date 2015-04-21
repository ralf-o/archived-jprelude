package org.jprelude.csv.base;

import java.util.Objects;
import org.apache.commons.csv.CSVRecord;

public final class CsvRecord {
    final CSVRecord apacheCommonsCsvRecord;
    final String source;
    
    CsvRecord(final CSVRecord apacheCommonsCsvRecord, final String source) {
        Objects.requireNonNull(apacheCommonsCsvRecord);
        
        this.apacheCommonsCsvRecord = apacheCommonsCsvRecord;
        this.source = source;
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
    
    public String getSource() {
        return this.source;
    }
        
    public long getIndex() {
        return this.apacheCommonsCsvRecord.getRecordNumber() - 1;
    }
    
    public long getCharacterPosition() {
        return this.apacheCommonsCsvRecord.getCharacterPosition();
    }
}
