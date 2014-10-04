/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jprelude.common.csv;

import org.jprelude.common.function.Function1;

public class CsvColumn<T> {
    private final String name;
    private final Function1<T, ?> selector;
    
    public CsvColumn(final String name, Function1<T, ?> selector) {
        this.name = name;
        this.selector = selector;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Function1<T, ?> getSelector() {
        return this.selector;
    }
}
