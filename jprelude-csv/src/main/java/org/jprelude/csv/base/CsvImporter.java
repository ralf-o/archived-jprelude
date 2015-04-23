package org.jprelude.csv.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.jprelude.core.io.TextReader;
import org.jprelude.core.util.Seq;
import org.jprelude.core.util.function.CheckedFunction;
import org.jprelude.core.util.function.Command;

public class CsvImporter<T> {
        private final CsvFormat format;
        private final CsvValidator validator;
        private final boolean failOnValidationError;
        private final Consumer<CsvRecord> onValidationSuccess;
        private final BiConsumer<? super CsvRecord, ? super CsvValidationException> onValidationError;
        private CheckedFunction<? super CsvRecord, ? extends Seq<T>> multiMapper;
        private final CSVFormat apacheCsvFormat;
             
            
    private CsvImporter(final Builder<T> builder) {
        assert builder != null;
        assert builder.format != null;
        
        this.format = builder.format;
        this.validator = builder.validator;
        this.failOnValidationError = builder.failOnValidationError;
        this.onValidationSuccess = builder.onValidationSuccess;
        this.onValidationError = builder.onValidationError;
        this.multiMapper = builder.multiMapper;
        
        final QuoteMode apacheCommonsCsvQuoteMode;
        
        switch (this.format.getQuoteMode()) {
            case ALL:
                apacheCommonsCsvQuoteMode = QuoteMode.ALL;
                break;

            case NONE:
                apacheCommonsCsvQuoteMode = QuoteMode.NONE;
                break;
            
            case NON_NUMERIC:
                apacheCommonsCsvQuoteMode = QuoteMode.NON_NUMERIC;
                break;

            default:
                apacheCommonsCsvQuoteMode = QuoteMode.MINIMAL;
                break;              
        }

        
        CSVFormat apacheFormat = CSVFormat.DEFAULT
                .withDelimiter(format.getDelimiter())
                .withRecordSeparator(this.format.getRecordSeparator().value())
                .withIgnoreSurroundingSpaces(this.format.isAutoTrimmed())
                .withEscape(this.format.getEscapeCharacter())
                .withQuote(this.format.getQuoteCharacter())
                .withQuoteMode(apacheCommonsCsvQuoteMode);
        
        if (!this.format.getColumns().isEmpty()) {
            final String[] columnNames = new String[this.format.getColumns().size()];
            Seq.from(this.format.getColumns()).forEach((col, idx) -> columnNames[idx.intValue()] = col.getName());
            apacheFormat = apacheFormat.withHeader(columnNames).withSkipHeaderRecord();
        }
        
        this.apacheCsvFormat = apacheFormat;
    }

    public Seq<T> parse(final TextReader textReader) {
        Objects.requireNonNull(textReader);
        
        final Seq<CsvRecord> records =  Seq.from(() ->  new Iterator<CsvRecord>() {
            private boolean initialized = false;
            private boolean completed = false;
            private BufferedReader reader =  null;
            private CSVParser parser = null;
            private Iterator<CSVRecord> iterator = null;

            @Override
            public boolean hasNext() {
                final boolean ret;

                if (this.completed) {
                    ret = false;
                } else {
                    try {
                        if (!this.initialized) {
                            this.reader = new BufferedReader(new InputStreamReader(textReader.newInputStream(), textReader.getCharset()));
                            this.parser = new CSVParser(this.reader, CsvImporter.this.apacheCsvFormat);
                            this.iterator = this.parser.iterator();
                            this.initialized = true;
                        }

                        ret = this.iterator.hasNext();

                        if (!ret) {
                            this.completed = true;
                            this.reader.close();
                            this.parser.close();
                        }
                    } catch (final IOException e) {
                        final RuntimeException re = new UncheckedIOException(e);
                        
                        try {
                            this.close();
                        } catch (final Throwable throwable) {
                            re.addSuppressed(throwable);
                        }
                        
                        throw re;
                    }
                }

                return ret;
            }

            @Override
            public CsvRecord next() {
                final CsvRecord ret;

                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    ret = new CsvRecord(this.iterator.next(), Objects.toString(textReader.getUri(), null));
                }

                return ret;
            }

            private void close() throws IOException {
                final Reader theReader = this.reader;
                final CSVParser theParser = this.parser;

                this.completed = true;
                this.reader = null;
                this.parser = null;
                this.iterator = null;

                try {
                    theReader.close();
                } finally {
                    theParser.close();
                }
            }
        });
        
        final Seq<T>  ret;
        
        if (this.validator == null) {
            if (this.onValidationSuccess == null) {
                ret = records.flatMap(this.multiMapper.unchecked());            
            } else {
                ret = records
                        .peek(this.onValidationSuccess)
                        .flatMap(this.multiMapper.unchecked());
            }
        } else {
            ret = records.filter(rec -> {
                final Optional<CsvValidationException> maybeError =
                        this.validator.validate(rec);
                
                if (maybeError.isPresent()) {
                    final CsvValidationException error = maybeError.get();
                    
                    if (this.onValidationError != null) {
                        this.onValidationError.accept(rec, error);
                    }
                   
                    if (this.failOnValidationError) {
                        throw maybeError.get();
                    }
                } else if (this.onValidationSuccess != null) {
                    this.onValidationSuccess.accept(rec);
                }
                
                return !maybeError.isPresent();
            })
            .flatMap(this.multiMapper.unchecked());
        }
        
        return ret;
    }
    
    public static <T> Builder<T> builder() {
        return new Builder<>();
    } 
    
    public static class Builder<T> {
        private CsvFormat format;
        private CsvValidator validator;
        private boolean failOnValidationError;
        private Consumer<CsvRecord> onValidationSuccess;
        private BiConsumer<CsvRecord, CsvValidationException> onValidationError;
        private CheckedFunction<? super CsvRecord, ? extends Seq<T>> multiMapper;
        
        private Builder() {
            this.format = null;
            this.validator = null;
            this.failOnValidationError = true;
            this.onValidationSuccess = null;
            this.onValidationError = null;
            this.multiMapper = null;
        }
        
        public Builder<T> format(final CsvFormat format) {
            Objects.requireNonNull(format);
            
            this.format = format;
            return this;
        }
        
        public Builder<T> validator(final CsvValidator validator) {
            Objects.requireNonNull(validator);
                    
            this.validator = validator;
            return this;
        }
        
        public Builder<T> unvalidated() {
            this.validator = null;
            return this;
        }
        
        
        public Builder<T> failOnValidationError(final boolean failOnValidatioError) {
            this.failOnValidationError = failOnValidatioError;
            return this;
        }
        
        public Builder<T> onValidationError(final Consumer<CsvValidationException> consumer) {
            Objects.requireNonNull(consumer);
            
            this.onValidationError = (rec, error) -> consumer.accept(error);
            return this;
        }

        public Builder<T> onValidationError(final BiConsumer<CsvRecord, CsvValidationException> consumer) {
            Objects.requireNonNull(consumer);
            
            this.onValidationError = consumer;
            return this;
        }
        
        public Builder<T> onValidationSuccess(final Command command) {
            Objects.requireNonNull(command);
            
            this.onValidationSuccess = rec -> command.execute();
            return this;
        }
 
        public Builder<T> onValidationSuccess(final Consumer<CsvRecord> consumer) {
            Objects.requireNonNull(consumer);
            
            this.onValidationSuccess = consumer;
            return this;
        }
        
        public Builder<T> multiMapper(final CheckedFunction<? super CsvRecord, ? extends Seq<T>> multiMapper) {
            Objects.requireNonNull(multiMapper);
            
            this.multiMapper = multiMapper;
            return this;
        }

        
        public Builder<T> mapper(final CheckedFunction<? super CsvRecord, ? extends T> mapper) {
            Objects.requireNonNull(mapper);
            
            this.multiMapper = rec -> Seq.of(mapper.apply(rec));
            return this;
        }
        
        
        public CsvImporter<T> build() {
            if (this.format == null) {
                throw new IllegalStateException("Cannot build CsvImporter "
                        + "as the CSV format has not been defined in the builder");
            } else if (this.multiMapper == null) {
                throw new IllegalStateException("Cannot build CsvImporter "
                        + "as no mapper/multiMapper has defined in the builder");
                
            }
            
            return new CsvImporter(this);
        }
    }
}
