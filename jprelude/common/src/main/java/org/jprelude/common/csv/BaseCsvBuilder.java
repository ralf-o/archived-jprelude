package org.jprelude.common.csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jprelude.common.util.Seq;

public abstract class BaseCsvBuilder<T, U extends BaseCsvBuilder<T, ?>> {
    public String getSeparator() {
        return separator;
    }

    public U setSeparator(String separator) {
        this.separator = separator;
        return (U) this;
    }

    public String getLineBreak() {
        return lineBreak;
    }

    public U setLineBreak(String lineBreak) {
        this.lineBreak = lineBreak;
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

    public boolean isQuotingForced() {
        return quotingForced;
    }

    public U setQuotingForced(boolean quotingForced) {
        this.quotingForced = quotingForced;
        return (U) this;
    }

    public boolean isAutoTrimmed() {
        return autoTrimmed;
    }

    public U setAutoTrimmed(boolean autoTrimmed) {
        this.autoTrimmed = autoTrimmed;
        return (U) this;
    }
    
    private String separator;
    private String lineBreak;
    private String aliasForNull;
    private String aliasForEmptyString;
    private boolean quotingForced;
    private boolean autoTrimmed;

    abstract Seq<String> buildSeq(Seq<T> source);

    protected BaseCsvBuilder() {
        this.separator = ",";
        this.lineBreak = "\n";
        this.aliasForNull = "";
        this.aliasForEmptyString = "";
        this.quotingForced = false;
        this.autoTrimmed = false;
    }

    protected String buildLine(final Seq<?> cells) {
        final StringBuilder strBuilder = new StringBuilder();

        if (cells == null) {
            strBuilder.append("\n");
        } else {
            cells.forEach((cellObject, idx) -> {
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
                        || cell.contains(this.lineBreak)) {
                    strBuilder.append("\"").append(cell).append("\"");
                } else {
                    strBuilder.append(cell);
                }
            });
        }

        return strBuilder.toString();
    }
}
