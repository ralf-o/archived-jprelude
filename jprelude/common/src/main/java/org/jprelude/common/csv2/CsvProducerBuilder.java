package org.jprelude.common.csv2;

import org.jprelude.common.util.Seq;

public abstract class CsvProducerBuilder<U extends CsvProducerBuilder<?>> {
    private String separator;
    private String aliasForNull;
    private String aliasForEmptyString;
    private boolean quotingForced ;
    private boolean autoTrimmed;
    
    public CsvProducerBuilder() {
        this.separator = ",";
        this.aliasForNull = "";
        this.aliasForEmptyString = "";
        this.quotingForced = false;
        this.autoTrimmed = false;
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
    
   protected String buildCsvLine(final Seq<?> cells) {
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
