package org.jprelude.common.csv;

import org.jprelude.common.function.UnaryFunction;
import org.jprelude.common.util.Seq;
import rx.Observable;

public class CsvMapper extends CsvHandler<CsvMapper> implements UnaryFunction<Seq<?>, String> {
    @Override
    public String apply(final Seq<?> cells) {
        return this.buildLine(cells);
    }
    
    public Seq<String> applyOn(final Seq<Seq<?>> rows) {
        final Seq<String> ret;
        
        if (rows == null) {
            ret = Seq.empty();
        } else {
            ret = rows.map(this);
        }
        
        return ret;
    }
    
    public Observable<String> applyOn(final Observable<Seq<?>> rows) {
        final Observable<String> ret;
        
        if (rows == null) {
            ret = Observable.empty();
        } else {
            ret = rows.map(this.toUnaryFunction());
        }
        
        return ret;
    }
}
