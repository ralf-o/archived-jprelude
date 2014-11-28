package org.jprelude.common.csv2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jprelude.common.function.UnaryFunction;
import org.jprelude.common.util.Seq;
import rx.Observable;
import rx.functions.Func1;

public class CsvBuilder<T> extends CsvProducerBuilder<CsvBuilder<?>> {
    private final List<CsvColumn<T>> columns;
    private final List<CsvColumn<T>> unmodifiableColumns;
    private boolean headerIncluded;
    
    public CsvBuilder() {
        this.columns = new ArrayList();
        this.unmodifiableColumns = Collections.unmodifiableList(this.columns);
        this.headerIncluded = true;
    }

    public Seq<String> applyOn(final Seq<T> seq) {
        final Seq<String> ret;
        
        final Seq<String> bodySeq = seq.map((item, idx) -> {
            final List<Object> cells = new ArrayList();
           
            this.columns.forEach(column ->
                    cells.add(column.getSelector().apply(item)));
        
            return this.buildCsvLine(Seq.from(cells));
        });
        
        if (!this.headerIncluded) {
            ret = bodySeq;
        } else {
            ret = Seq.concat(Seq.of(this.buildHeaderLine()), bodySeq);
        }
        
        return ret;
    }
    
    public Observable<String> applyOn(final Observable<T> observable) {
        final Observable<String> ret;
        
        if (observable == null) {
            ret = Observable.empty();
        } else {
            final Func1<T, String> f = this.asMapper().toUnaryFunction();
            ret = observable.map(f);
        }
        
        return ret;
    }

    public UnaryFunction<T, String> asMapper() {
        return item -> {
            final StringBuilder strBuilder = new StringBuilder();
        
            this.columns.forEach(column ->
                strBuilder.append(column.getSelector().apply(item)));
            
            return strBuilder.toString();
        };
    }
    
    public CsvBuilder<T> setColumns(final CsvColumn<T>... columns) {
        this.columns.clear();
        
        if (columns != null) {
            for (final CsvColumn<T> column : columns) {
                this.columns.add(column);
            }
        }
        
        return this;
    }
    
    public CsvBuilder<T> setColumns(final Iterable<CsvColumn<T>> columns) {
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
    
    public CsvBuilder<T> setHeaderIncluded(final boolean headerIncluded) {
        this.headerIncluded = headerIncluded;
        return this;
    }
    
    public boolean getHeaderIncluded() {
        return this.headerIncluded;
    }
    
    private String buildHeaderLine() {
        final Seq<String> headerNames = Seq.from(this.columns).map(CsvColumn::getName);
        
        return this.buildCsvLine(headerNames);
    }    
}
