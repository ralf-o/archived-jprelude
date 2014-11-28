package org.jprelude.common.csv2;

import org.jprelude.common.function.UnaryFunction;

public class CsvParser<T> extends CsvConsumerBuilder<CsvParser<?>> {
    private UnaryFunction<CsvRecord, T> recordMapper;
    
    public CsvParser setRecordMapper(final UnaryFunction<CsvRecord, T> recordMapper) {
        this.recordMapper = recordMapper;
        return this;
    }
    
    public UnaryFunction<CsvRecord, T> getRecordMapper() {
        return this.recordMapper;
    }
}
