package org.jprelude.common.csv3;

import java.util.List;
import org.jprelude.common.function.UnaryFunction;
import org.jprelude.common.util.Seq;

public class CsvLineJoiner implements UnaryFunction<List<?>, String> {
    private String separator;
    private String aliasForNull;
    private String aliasForEmptyString;
    private boolean quotingForced ;
    private boolean autoTrimmed;
    
    public CsvLineJoiner() {
        this.separator = ",";
        this.aliasForNull = "";
        this.aliasForEmptyString = "";
        this.quotingForced = false;
        this.autoTrimmed = false;
    }

    
    public String getSeparator() {
        return separator;
    }

    public CsvLineJoiner setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public String getAliasForNull() {
        return aliasForNull;
    }

    public CsvLineJoiner setAliasForNull(String aliasForNull) {
        this.aliasForNull = aliasForNull;
        return this;
    }

    public String getAliasForEmptyString() {
        return aliasForEmptyString;
    }

    public CsvLineJoiner setAliasForEmptyString(String aliasForEmptyString) {
        this.aliasForEmptyString = aliasForEmptyString;
        return this;
    }    
    
    public void setQuotingForced(final boolean quotingForced) {
        this.quotingForced = quotingForced;
    }
    
    public boolean getQuotingForced() {
        return this.quotingForced;
    }
    
    public void setAutoTrimmed(final boolean autoTrimmed) {
        this.autoTrimmed = autoTrimmed;
    }
    
    public boolean getAutoTrimmed() {
        return this.autoTrimmed;
    }
   
    @Override
    public String apply(final List<?> cells) {
        final StringBuilder strBuilder = new StringBuilder();

        if (cells == null) {
            strBuilder.append("\n");
        } else {
            Seq.from(cells).forEach((cellObject, idx) -> {
                String cell = (cellObject == null ? null : cellObject.toString());

                if (idx > 0) {
                    strBuilder.append(this.separator);
                }

                if (cell == null) {
                    cell = this.aliasForNull;
                }

                cell = "" + cell;

                if (this.autoTrimmed) {
                    cell = cell.trim();
                }

                if (cell.length() == 0) {
                    cell = this.aliasForEmptyString;
                }

                cell = cell.replace("\"", "\"\"");

                if (this.quotingForced
                        || cell.contains("\"")
                        || cell.contains(this.separator)
                        || cell.contains("\n")
                        || cell.contains("\r")) {
                    strBuilder.append("\"").append(cell).append("\"");
                } else {
                    strBuilder.append(cell);
                }
            });
        }

        return strBuilder.toString();
    }    
}
