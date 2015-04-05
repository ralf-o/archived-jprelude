package org.jprelude.common.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jprelude.common.io.TextWriter;
import org.jprelude.common.util.Seq;

/*
new CsvExport()
  .columns("First Name", "Last lastName", "Date of birth")
  .seperator(",")
  .autoTrim(true)
  .addFieldMapper(null, "<NULL>")
  .addFieldMapper("", "-")
  .body((person, row) ->
    row.add(
        person.getFirstName(),
        person.getLastName(),
        person.getDateOfBirth()))
  .write(persons, TextWriter.from("~/output.csv));
*/

public final class CsvExport<T> {
    private String separator = "\"";
    private boolean autoTrim = false;
    private final List<Function<String, String>> fieldMappers = new ArrayList<>();
    private final List<CsvColumn> columns = new ArrayList<>();
    private Consumer<CsvRowHandler> headerGenerator;
    private Consumer<CsvRowHandler> footerGenerator;
    private BiConsumer<T, CsvRowHandler> bodyGenerator;
    
    public CsvExport separtor(final String separator) {
        this.separator = separator;
        return this;
    }
    
    public String getSeparator() {
        return this.separator;
    }
    
    public CsvExport autoTrim(final boolean autoTrim) {
        this.autoTrim = autoTrim;
        return this;
    }
    
    public boolean isAutoTrim() {
        return this.autoTrim;
    }
    
    public CsvExport addFieldMapper(final Function<String, String> fieldMapper) {
        if (fieldMapper != null) {
            this.fieldMappers.add(fieldMapper);
        }
        
        return this;
    }
        
    public CsvExport addFieldMapper(final String value, final String substitution) {
        this.addFieldMapper(field ->
                field == null && value == null || field != null && field.equals(value)
                ? substitution
                : field
        );
        
        return this;
    }
    
    public CsvExport addFieldMappers(final Map<String, String> substitutions) {
        if (substitutions != null) {
            for (final String key :substitutions.keySet()) {
                this.addFieldMapper(key, substitutions.get(key));
            }
        }
        
        return this;
    }

    
    public CsvExport addFieldMappers(final Function<String, String>... fieldMappers) {
        if (fieldMappers != null) {
            for (final Function<String, String> fieldMapper : fieldMappers) {
                this.addFieldMapper(fieldMapper);
            }
        }
        
        return this;
    }
        
    public CsvExport clearFieldMappers() {
        this.fieldMappers.clear();
        return this;
    }

    public List<Function<String, String>> getFieldMappers() {
        return new ArrayList<>(this.fieldMappers);
    }
        
    public CsvExport columns(String... columnNames) {
        this.columns.clear();
        
        if (columnNames != null) {
            for (final String columnName : columnNames) {
                this.columns.add(new CsvColumn(columnName));
            }
        }
        
        return this;
    }
    
    public CsvExport columns(final CsvColumn... columns) {
        return this.columns(Arrays.asList(columns));
    }
    
    public CsvExport columns(final List<CsvColumn> columns) {
        this.columns.clear();
        
        if (columns != null) {
            for (final CsvColumn column : columns) {
                if (column != null) {
                    this.columns.add(column);
                }
            }
        }
        
        return this;        
    }
    
    public List<CsvColumn> getColumns() {
        return new ArrayList<>(this.columns);
    }
    
    public CsvExport header(final Consumer<CsvRowHandler> generator) {
        this.headerGenerator = generator;
        return this;
    }
    
    public Consumer<CsvRowHandler> getHeaderGenerator() {
        return this.headerGenerator;
    }
    
    public CsvExport footer(final Consumer<CsvRowHandler> generator) {
        this.footerGenerator = generator;
        return this;
    }
    
    public Consumer<CsvRowHandler> getFooterGenerator() {
        return this.footerGenerator;
    }
    
    public CsvExport body(final BiConsumer<T, CsvRowHandler> generator) {
        this.bodyGenerator = generator;
        return this;
    }
    
    public BiConsumer<T, CsvRowHandler> getBodyGenerator() {
        return this.bodyGenerator;
    }

    public Seq<String> map(final Seq<T> items) {
        return null; // TODO
    }
    
    public void write(final Seq<T> items, final TextWriter writer) throws IOException {
        // TODO
    }

 /*
    private String buildLine(final Seq<?> cells) {
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
*/
}

    