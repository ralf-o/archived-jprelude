package org.jprelude.common.csv2;

import org.jprelude.common.function.UnaryFunction;

public class CsvColumn<T> {
    private final String name;
    private final UnaryFunction<T, ?> selector;
    
    public CsvColumn(final String name, UnaryFunction<T, ?> selector) {
        this.name = name;
        this.selector = selector;
    }
    
    public String getName() {
        return this.name;
    }
    
    public UnaryFunction<T, ?> getSelector() {
        return this.selector;
    }
}
