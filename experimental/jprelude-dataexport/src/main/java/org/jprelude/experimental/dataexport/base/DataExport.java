package org.jprelude.experimental.dataexport.base;

import org.jprelude.core.util.Seq;

public interface DataExport<T> {
    Seq<T> getRecords();
    
    Object getUniqueKey();
}
