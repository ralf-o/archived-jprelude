package org.jprelude.csv.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CsvValidationException extends RuntimeException {
    private final String source;
    private final long recordIndex; 
    private final long characterPosition;
    private final List<String> violations;

    public CsvValidationException(
            final String message,
            final String source,
            final long recordIndex,
            final long characterPosition,
            final List<String> violations) {
        
        super(message);
        
        Objects.requireNonNull(violations);
        
        if (recordIndex < 0) {
            throw new IllegalArgumentException();
        }
        
        this.source = source;
        this.recordIndex = recordIndex;
        this.characterPosition = characterPosition;
        
        this.violations = Collections.unmodifiableList(
                new ArrayList<>(violations));
    }
    
    public String getSource() {
        return this.source;
    }
    
    public long getRecordIndex() {
        return this.recordIndex;
    }
    
    public List<String> getViolations() {
        return this.violations;
    }
}
