package org.jprelude.common.csv;

import java.util.List;
import java.util.function.Function;

public class CsvRow {
    final Long index;
    final List<?> fields;
    final Function<String, Integer> nameToIndex;
    
    public CsvRow(final Long index, final List<?> fields, Function<String, Integer> nameToIndex) {
        this.fields = fields;
        this.nameToIndex = nameToIndex;
        this.index = index;
    }
    
    public String get(int columnIdx) {
        final Object field = this.fields.get(columnIdx);
        return (field == null ? null : field.toString());
    }
    
    public String get(String columnName) {
        final String ret;
        final Integer columnIdx = this.nameToIndex.apply(columnName);
        
        if (columnIdx == null) {
            ret = null;
        } else {
            ret = this.get(columnIdx);
        }
        
        return ret;
    }
        
    long index() {
        return this.index;
    }
}
