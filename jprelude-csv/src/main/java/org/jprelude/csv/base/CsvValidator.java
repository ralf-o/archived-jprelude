package org.jprelude.csv.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CsvValidator {
    private final List<ColumnConstraint> columnRules;
    private final List<RecordConstraint> recordRules;
    
    private CsvValidator(
            final List<ColumnConstraint> columnRules,
            List<RecordConstraint> recordRules) {
        
        assert columnRules != null;
        assert recordRules != null;
        
        this.columnRules = columnRules;
        this.recordRules = recordRules;
    }
    
    public Optional<CsvValidationException> validate(final CsvRecord rec) {
        final Optional<CsvValidationException> ret;
        
        final List<String> violations = new ArrayList<>();
        
        this.columnRules.forEach(columnRule -> {
            final StringBuilder violation = new StringBuilder();
            final String columnName = columnRule.getColumnName();
            final String columnValue = rec.get(columnName);        
            final CsvField checker = new CsvField(columnValue);
            
            if (!columnRule.getPredicate().test(checker)) {
                String shortenedColumnValue =
                        (columnValue != null ? columnValue : "")
                        .replaceAll("(\\r|\\n|\\t| )+", " ");
                
                if (shortenedColumnValue.length() > 20) {
                    shortenedColumnValue = shortenedColumnValue.substring(0, 17);
                }
                
                violation.append("Column \"");
                violation.append(columnName);
                violation.append("\" violates rule \"");
                violation.append(columnRule.getRule());
                violation.append("\", value is \"");
                violation.append(shortenedColumnValue);
                violation.append("\"");
                
                violations.add(violation.toString());
            }
        });
        
        this.recordRules.forEach(recordRule -> {
            if (recordRule.getPredicate().test(rec)) {
                violations.add("Record validates rule \""
                        + recordRule.getRule()
                        + "\"");
            }
        });
        
        if (violations.isEmpty()) {
            ret = Optional.empty();
        } else {
            final long violationCount = violations.size();
            
            final String errorMsg = "Invalid CSV record #"
                    + (rec.getIndex() + 1)
                    + ", source: " + rec.getSource()
                    + ", position: " + rec.getCharacterPosition()
                    + ", message: \"" + violations.get(0)
                    + "\""
                    + (violationCount > 1
                            ? " and " + (violationCount - 1)  + " other violation(s)..."
                            : "");

            ret = Optional.of(new CsvValidationException(
                    errorMsg, 
                    rec.getSource(),
                    rec.getIndex(),
                    rec.getCharacterPosition(),
                    violations));
        }
        
        return ret;
    }
    
    public Consumer<CsvRecord> asConsumer() {
        return rec -> {
            final Optional<CsvValidationException> error = this.validate(rec);

            if (error.isPresent()) {
                throw error.get();
            }
        };
    }
    
    public Predicate<CsvRecord> asFilter() {
        return this.asFilter(error -> {});
    }
    
    public Predicate<CsvRecord> asFilter(final Consumer<CsvValidationException> onError) {
        return rec -> {
            final boolean ret;
            final Optional<CsvValidationException> error = this.validate(rec);
            
            if (!error.isPresent()) {
                ret = true;
            } else {
                ret = false;
                onError.accept(error.get());
            }
            
            return ret;
        };
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        private final List<ColumnConstraint> columnRules;
        private final List<RecordConstraint> recordRules;
        
        private Builder() {
            this.columnRules = new ArrayList();
            this.recordRules = new ArrayList();
        }
        
        public CsvValidator build() {
            return new CsvValidator(this.columnRules, this.recordRules);
        }
        
        public Builder validateColumn(
                final String columnName,
                final String hint,
                final Predicate<CsvField> predicate) {
            
            Objects.requireNonNull(columnName);
            Objects.requireNonNull(hint);
            Objects.requireNonNull(predicate);
            
            this.columnRules.add(new ColumnConstraint(columnName, hint, predicate));
            return this;
        }
        
        public Builder validateRecord(
                final String hint,
                final Predicate<CsvRecord> predicate) {
        
            Objects.requireNonNull(hint);
            Objects.requireNonNull(predicate);
            
            this.recordRules.add(new RecordConstraint(hint, predicate));
            return this;
        }
        
    }
        
    private final static class ColumnConstraint {
        private String columnName;
        private String hint;
        private Predicate<CsvField> predicate;
        
        private ColumnConstraint(
                final String columnName,
                final String hint,
                final Predicate<CsvField> predicate) {
            
            assert columnName != null;
            assert hint != null;
            assert predicate != null;
            
            this.columnName = columnName;
            this.hint = hint;
            this.predicate = predicate;
        }
        
        private String getColumnName() {
            return this.columnName;
        }
        
        private String getRule() {
            return this.hint;
        }
        
        private Predicate<CsvField> getPredicate() {
            return this.predicate;
        }
    }
    
    private final static class RecordConstraint {
        private String hint;
        private Predicate<CsvRecord> predicate;
        
        private RecordConstraint(
                final String hint,
                final Predicate<CsvRecord> predicate) {
            
            assert hint != null;
            assert predicate != null;
        }
        
        private String getRule() {
            return this.hint;
        }
        
        private Predicate<CsvRecord> getPredicate() {
            return this.predicate;
        }
    }
}
