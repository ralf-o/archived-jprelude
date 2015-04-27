package org.jprelude.experimental.datasource.demo;

public class Category {
    private long id;
    private String name;
    
    public Category(final long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
}
