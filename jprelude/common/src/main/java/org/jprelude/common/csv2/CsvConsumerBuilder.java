package org.jprelude.common.csv2;

public abstract class CsvConsumerBuilder<U extends CsvConsumerBuilder<?>> {
    private String separator;
    private String aliasForNull;
    private String aliasForEmptyString;
    
    public CsvConsumerBuilder() {
        this.separator = ",";
        this.aliasForNull = "";
        this.aliasForEmptyString = "";
    }
    
    public String getSeparator() {
        return separator;
    }

    public U setSeparator(String separator) {
        this.separator = separator;
        return (U) this;
    }

    public String getAliasForNull() {
        return aliasForNull;
    }

    public U setAliasForNull(String aliasForNull) {
        this.aliasForNull = aliasForNull;
        return (U) this;
    }

    public String getAliasForEmptyString() {
        return aliasForEmptyString;
    }

    public U setAliasForEmptyString(String aliasForEmptyString) {
        this.aliasForEmptyString = aliasForEmptyString;
        return (U) this;
    }    
}
