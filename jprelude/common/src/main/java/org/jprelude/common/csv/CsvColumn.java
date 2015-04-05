/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jprelude.common.csv;

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