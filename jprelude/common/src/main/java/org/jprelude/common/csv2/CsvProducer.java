package org.jprelude.common.csv2;

import org.jprelude.common.util.Seq;
import rx.Observable;

public interface CsvProducer<T> {
    Seq<String> applyOn(Seq<T> seq);
    Observable<String> applyOn(Observable<T> observable);
}