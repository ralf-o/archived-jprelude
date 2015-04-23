package org.jprelude.csv.base;

public final class CsvColumn {
    private final String name;
    
    public CsvColumn(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
}

