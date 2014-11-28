package org.jprelude.common.csv3;

import java.util.Arrays;
import java.util.List;
import org.jprelude.common.function.UnaryFunction;

public class CsvLineSplitter implements UnaryFunction<String, List<String>> {
    private String separator;
    private String aliasForNull;
    private String aliasForEmptyString;
    
    public CsvLineSplitter() {
        this.separator = ",";
        this.aliasForNull = "";
        this.aliasForEmptyString = "";
    }
    
    public String getSeparator() {
        return separator;
    }

    public CsvLineSplitter setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public String getAliasForNull() {
        return aliasForNull;
    }

    public CsvLineSplitter setAliasForNull(String aliasForNull) {
        this.aliasForNull = aliasForNull;
        return this;
    }

    public String getAliasForEmptyString() {
        return aliasForEmptyString;
    }

    public CsvLineSplitter setAliasForEmptyString(String aliasForEmptyString) {
        this.aliasForEmptyString = aliasForEmptyString;
        return this;
    }    
    
    @Override
    public List<String> apply(final String csvLine) {
        final String line = csvLine == null ? "" : csvLine;
        return Arrays.asList(line.split(this.separator));
    }
}
