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


Query query = dbRepo.from(QPerson.class).where(QPerson.country.eq("Germany"));
Seq<Person> persons = Seq.from((start, count) -> query.limit(start, count).fetch(), 100);


CsvExport.<Person>builder()
  .seperator(";")
  .autoTrim(true)
  .columns(
      "FIRST_NAME",
      "LAST_NAME",
      "COUNTRY")
  .rowProcessor((person, row) -> 
      row.set(0, person.getFirstName());
      row.set(1, person.getLastName));
      row.set(2, person.getCountry());
  .build()
  .write(persons, TextWriter.from(new File("~/output.csv")));


CsvExport.<Person>builder(CsvFormat.MYSQL)
  .columns(
      "FIRST_NAME",
      "LAST_NAME",
      "COUNTRY")
  .rowProcessor((person, row) -> 
      row.set(0, person.getFirstName());
      row.set(1, person.getLastName));
      row.set(2, person.getCountry());
   })
  .build()
  .write(persons, TextWriter.from(new File("~/output.csv")));


CsvExport.<Person>builder(CsvFormat.MYSQL)
  .columns(
      new CsvColumn("firstName", "FIRST_NAME"),
      new CsvColumn("lastName", "LAST_NAME"),
      new CsvColumn("country", "COUNTRY")
  .rows((person, row) -> {
      row.set("firstName", person.getFirstName());
      row.set("lastName", person.getLastName));
      row.set("country", person.getCountry();
   })
  .build()
  .write(persons, TextWriter.from(new File("~/output.csv")));


CsvExport.<Person>builder(CsvFormat.MYSQL)
  .columns(
      new CsvColumn("FIRST_NAME", Person::getFirstName), // Closure-Kurzform von: person -> person.getFirstName()
      new CsvColumn("LAST_NAME", Person::getLastName),
      new CsvColumn("COUNTRY", Person::getCountry)
  .build()
  .write(persons, TextWriter.from(new File("~/output.csv")));


CsvExport.<Person>builder(CsvFormat.MYSQL)
  .columns(
      "FIRST_NAME",
      "LAST_NAME",
      "COUNTRY")
  .rowProcessor((person, row) -> 
      row.set(0, person.getFirstName());
      row.set(1, person.getLastName));
      row.set(2, person.getCountry());
   })
  .build()
  .write(persons, TextWriter.from(new File("~/output.csv")));

CsvExport.<Person>builder(CsvFormat.MYSQL)
  .columns(
      "FIRST_NAME",
      "LAST_NAME",
      "COUNTRY")
  .rowProcessor((person, row) -> 
      row.set(0, person.getFirstName());
      row.set(1, person.getLastName));
      row.set(2, person.getCountry());
   })
  .build()
  .write(persons, TextWriter.from(new File("~/output.csv")));



CsvExport.<Person>builder(CsvFormat.MYSQL)
  .columns(
      "FIRST_NAME",
      "LAST_NAME",
      "COUNTRY" )
  .entityMapper(person -> 
      Arrays.asList(
        person.getFirstName(),
        person.getLastName(),
        person.Country()))
  .build()
  .write(persons, TextWriter.from(new File("~/output.csv")));


CsvExport.<Person>builder(CsvFormat.MYSQL)
  .columns(
      "FIRST_NAME",
      "LAST_NAME",
      "COUNTRY")
  .entityMultiMapper(person -> 
      Seq.of(
        Arrays.asList(
          person.getFirstName(),
          person.getLastName(),
          person.Country())))
  .build()
  .write(persons, TextWriter.from(new File("~/output.csv")));




CsvFormat.builder()
  .separator("\t")
  .autoTrim(true)
  .build();




CsvExport.<Person>builder()
  .seperator(",")
  .autoTrim(true)
  .addFieldMapper(null, "<NULL>")
  .addFieldMapper("", "-")
  .columns(
      "FIRST_NAME",
      "Last lastName",
      "Date of birth")
  .rowProcessor((person, row) -> 
      row.set(0, person.getFirstName());
      row.set(1, person.getLastName));
      row.set(2, person.getDateOfBirth());

      row.setAllFields(
        person.getFirstName(),
        person.getLastName(),
        person.getDateOfBirth()))
  .build()
  .write(persons, TextWriter.from("~/output.csv));



CsvExport.<Person>builder()
  .columns(
     new CsvColumn("FIRST_NAME", Product::getFirstName)
     new CsvColumn("LAST_NAME", Product::getLastName)
     new CsvColumn("Date of Birth", Product::getDateOfBirth)
  .seperator(",")
  .autoTrim(true)
  .addFieldMapper(null, "<NULL>")
  .addFieldMapper("", "-")
  .build()
  .write(persons, TextWriter.from("~/output.csv));

*/



public final class CsvExport<T> {
    private final CsvFormat csvFormat;
    private final List<CsvColumn> columns;
    private final Function<T, Seq<List<?>>> recordMultiMapper;

    private CsvExport(final Builder builder) {
        this.csvFormat = CsvFormat.builder()
                .delimiter(builder.delimiter)
                .recordSeparator(builder.recordSeparator)
                .autoTrim(builder.autoTrim)
                .build();
        
        this.columns = builder.columns;
        this.recordMultiMapper = builder.recordMapper;
    }
            
    public CsvFormat csvFormat() {
        return this.csvFormat;
    }
    
   public List<CsvColumn> getColumns() {
        return new ArrayList<>(this.columns);
    }
    
    public Function<T, Seq<List<?>>> getRecordMultiMapper() {
        return this.recordMultiMapper;
    }

    public Seq<String> map(final Seq<T> records) {
        return Seq.sequential(records).map(this.recordMultiMapper).flatMap(Function.identity()).map(fieldList -> { 
            return this.buildCsvLine(fieldList);
        });
    }
    
    public void write(final Seq<T> records, final TextWriter writer) throws IOException {
        if (writer != null) {
            final String recordSeparator = this.csvFormat.getRecordSeparator();
            
            writer.write(printStream -> {
                this.map(records).forEach(line -> {
                    printStream.append(line);
                    printStream.append(recordSeparator);
                });
            });
        }
    }
    
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static <T> Builder<T> builder(final CsvExport prototype) {
        final Builder<T> builder = new Builder();
        builder.delimiter = prototype.csvFormat.getDelimiter();
        builder.recordSeparator = prototype.csvFormat.getRecordSeparator();
        builder.autoTrim = prototype.csvFormat.isAutoTrim();
        builder.columns.addAll(prototype.columns);
        builder.recordMapper = prototype.recordMultiMapper;
        return builder;
    }    
    
    private String buildCsvLine(final List<?> fields) {
        final StringBuilder strBuilder = new StringBuilder();
        final String delimiter = this.csvFormat.getDelimiter();
        final String recordSeparator = this.csvFormat.getRecordSeparator();
        final String quoting = "\""; // TODO
        
        if (fields == null) {
            strBuilder.append(recordSeparator);
        } else {
            Seq.from(fields).forEach((field, idx) -> {
                String cell = (field == null ? "" : field.toString());

                if (idx > 0) {
                    strBuilder.append(delimiter);
                }

                if (this.csvFormat.isAutoTrim()) {
                    cell = cell.trim();
                }


                cell = cell.replace(quoting, quoting + quoting);

                if (/*this.quotingForced
                        ||*/ cell.contains("\"")
                        || cell.contains(delimiter)
                        || cell.contains("\n")
                        || cell.contains("\r")
                        || cell.contains(recordSeparator)) {
                    strBuilder.append(quoting).append(cell).append(quoting);
                } else {
                    strBuilder.append(cell);
                }
            });
        }

        return strBuilder.toString();
    }
    
    public static final class Builder<T> {
        private String delimiter = ",";
        private String recordSeparator = "\r\n";
        private boolean autoTrim = false;
        private final List<Function<String, String>> fieldMappers = new ArrayList<>();
        private final List<CsvColumn> columns = new ArrayList<>();
        private Function<T, Seq<List<?>>> recordMapper;

        public Builder recordSepartor(final String recordSeparator) {
            this.recordSeparator = recordSeparator;
            return this;
        }

        public Builder autoTrim(final boolean autoTrim) {
            this.autoTrim = autoTrim;
            return this;
        }

        public Builder addFieldMapper(final Function<String, String> fieldMapper) {
            if (fieldMapper != null) {
                this.fieldMappers.add(fieldMapper);
            }

            return this;
        }

        public Builder addFieldMapper(final String value, final String substitution) {
            this.addFieldMapper(field ->
                    field == null && value == null || field != null && field.equals(value)
                    ? substitution
                    : field
            );

            return this;
        }

        public Builder addFieldMappers(final Map<String, String> substitutions) {
            if (substitutions != null) {
                for (final String key :substitutions.keySet()) {
                    this.addFieldMapper(key, substitutions.get(key));
                }
            }

            return this;
        }


        public Builder addFieldMappers(final Function<String, String>... fieldMappers) {
            if (fieldMappers != null) {
                for (final Function<String, String> fieldMapper : fieldMappers) {
                    this.addFieldMapper(fieldMapper);
                }
            }

            return this;
        }

        public Builder clearFieldMappers() {
            this.fieldMappers.clear();
            return this;
        }

        public Builder columns(String... columnNames) {
            this.columns.clear();

            if (columnNames != null) {
                for (final String columnName : columnNames) {
                    this.columns.add(new CsvColumn(columnName));
                }
            }

            return this;
        }

        public Builder columns(final CsvColumn... columns) {
            return this.columns(Arrays.asList(columns));
        }

        public Builder columns(final List<CsvColumn> columns) {
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
        
        public Builder recordMapper(final Function<T, List<?>> recordMapper)  {
            if (recordMapper == null) {
                this.recordMapper = null;
            } else {
                this.recordMapper = rec -> Seq.of(recordMapper.apply(rec));
            }

            return this;
        }
        
        public Builder recordMultiMapper(final Function<T, Seq<List<?>>> recordMapper) {
            this.recordMapper = recordMapper;
            return this;
        }
        
        public CsvExport build() {
            return new CsvExport(this);
        }
    }
}

    