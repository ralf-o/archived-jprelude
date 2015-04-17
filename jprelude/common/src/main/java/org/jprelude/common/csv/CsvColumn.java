package org.jprelude.common.csv;

public final class CsvColumn {
    private final String name;
    private final String title;
    
    public CsvColumn(final String name) {
        this(name, name);
    }
    
    public CsvColumn(final String name, final String title) {
        this.name = name;
        this.title = title;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getTitle() {
        return this.title;
    }
}

