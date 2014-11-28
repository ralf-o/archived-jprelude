package org.jprelude.common.csv2;

import org.jprelude.common.util.Seq;
import rx.Observable;

public interface CsvConsumer<T> {
    Seq<T> applyOn(Seq<String> seq);
    Observable<T> applyOn(Observable<String> observable);
}
