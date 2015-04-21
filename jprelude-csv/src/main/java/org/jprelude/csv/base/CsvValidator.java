package org.jprelude.csv.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CsvValidator {
    private List<ColumnConstraint> columnRules;
    private List<RecordConstraint> recordRules;
    
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
        
        final StringBuilder violation = new StringBuilder();
        final List<String> violations = new ArrayList<>();
        
        this.columnRules.forEach(columnRule -> {
            final String columnName = columnRule.getColumnName();
            final String columnValue = rec.get(columnName);        
            final Checker checker = new Checker(columnValue);
            
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
                final Predicate<Checker> predicate) {
            
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
    
    public static final class Checker {
        private final String val;
        
        private Checker(final String value) {
            this.val = value;
        }
        
        public boolean isNull() {
            return this.val == null;
        }
        
        public boolean isNotNull() {
            return this.val != null;
        }
        
        public boolean hasLength(final int n) {
            return (this.val == null && n == 0)
                    || (this.val != null && this.val.length() == n);
        }
        
        public boolean isGreater(final float x) {
            return !this.isNull() && this.isFloat() && Float.parseFloat(this.val) > x;
        }
        
        public boolean isInteger() {
            boolean ret;
         
            try {
                Integer.parseInt(this.val);
                ret = true;
            } catch (final NumberFormatException e) {
                ret = false;
            }
            
            return ret;
        }

        public boolean isFloat() {
            boolean ret;
         
            try {
                Float.parseFloat(this.val);
                ret = true;
            } catch (final NumberFormatException e) {
                ret = false;
            }
            
            return ret;
        }

        public boolean isDouble() {
            boolean ret;
         
            try {
                Double.parseDouble(this.val);
                ret = true;
            } catch (final NumberFormatException e) {
                ret = false;
            }
            
            return ret;
        }
    }
    
    private final static class ColumnConstraint {
        private String columnName;
        private String hint;
        private Predicate<Checker> predicate;
        
        private ColumnConstraint(
                final String columnName,
                final String hint,
                final Predicate<Checker> predicate) {
            
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
        
        private Predicate<Checker> getPredicate() {
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
