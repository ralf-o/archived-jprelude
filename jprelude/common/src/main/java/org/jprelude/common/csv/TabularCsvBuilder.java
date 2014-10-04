package org.jprelude.common.csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jprelude.common.util.Seq;

public final class TabularCsvBuilder<T> extends BaseCsvBuilder<T, TabularCsvBuilder<T>> {
    private final List<CsvColumn<T>> columns;
    private final List<CsvColumn<T>> unmodifiableColumns;
    private boolean headerIncluded;
    
    private TabularCsvBuilder() {
        this.columns = new ArrayList();
        this.unmodifiableColumns = Collections.unmodifiableList(this.columns);
        this.headerIncluded = true;
    }

    public static <T> TabularCsvBuilder<T> ofSourceType(Class<T> type) {
        return new TabularCsvBuilder();
    }

    @Override
    public Seq<String> buildSeq(final Seq<T> source) {
        final Seq<String> ret;
        
        final Seq<String> bodySeq = source.map((item, idx) -> {
            final List<Object> cells = new ArrayList();
           
            for (final CsvColumn<T> column : this.columns) {
                cells.add(column.getSelector().apply(item));
            }

           return this.buildLine(Seq.from(cells));
        });
        
        if (!this.headerIncluded) {
            ret = bodySeq;
        } else {
            ret = Seq.concat(Seq.of(this.buildHeaderLine()), bodySeq);
        }
        
        return ret;
    }
    
    
    public TabularCsvBuilder<T> setColumns(final CsvColumn<T>... columns) {
        this.columns.clear();
        
        if (columns != null) {
            for (final CsvColumn<T> column : columns) {
                this.columns.add(column);
            }
        }
        
        return this;
    }
    
    public TabularCsvBuilder<T> setColumns(final Iterable<CsvColumn<T>> columns) {
        this.columns.clear();
        
        if (columns != null) {
            for (final CsvColumn<T> column : columns) {
                this.columns.add(column);
            }
        }  
        
        return this;
    }
    
    public List<CsvColumn<T>> getColumns() {
        return this.unmodifiableColumns;
    }
    
    public TabularCsvBuilder<T> setHeaderIncluded(final boolean headerIncluded) {
        this.headerIncluded = headerIncluded;
        return this;
    }
    
    public boolean getHeaderIncluded() {
        return this.headerIncluded;
    }
    
    private String buildHeaderLine() {
        final Seq<CsvColumn<T>> columns = Seq.from(this.columns);
        final Seq<String> headerNames = columns.map(CsvColumn::getName);
        
        return this.buildLine(headerNames);
    }
}
