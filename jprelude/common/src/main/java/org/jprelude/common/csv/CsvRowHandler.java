package org.jprelude.common.csv;

public interface CsvRowHandler {
    void set(String fieldName, Object fieldValue);
    void set(int fieldIdx, Object fieldValue);
    void add(Object... fields);
    void newRow();
    int rowNumber();
}
