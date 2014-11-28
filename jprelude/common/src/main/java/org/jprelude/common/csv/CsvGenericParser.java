
/*
package org.jprelude.common.csv;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jprelude.common.function.UnaryFunction;
import org.jprelude.common.io.TextReader;
import org.jprelude.common.util.Seq;
import rx.Observable;

public class CsvGenericParser<T> extends CsvHandler {
    private UnaryFunction<CsvRecord, T> recordMapper;
    
    public CsvGenericParser() {
    }
    
    public void setRecordMapper(final UnaryFunction<CsvRecord, T> recordMapper) {
        this.recordMapper = recordMapper;
    }
    
    private List<String> tokenizeLine(final String line) {
        final List<String> ret;
        
        if (line == null) {
            ret = Collections.emptyList();
        } else {
            final String[] tokens = line.split(this.getSeparator());
            
            ret = Collections.unmodifiableList(Arrays.asList(tokens));
        }
        
        return ret;
    }
    
    public Seq<T> applyOn(final Seq<String> seq) {
        final CsvRecord[] csvRecordWrapper = { null };
        
        Seq.from(seq).map(this);
    }
    
    public Observable<T> applyOn(final Observable<String> observable) {
        return null;
    }
    
    
    public void test() {
        final CsvGenericParser<Person> csvParser = new CsvGenericParser<>()
            .setSeparator(";")
                
                
        TextReader reader = new TextReader("c:/test.csv");
        
        reader.readLines().map(csvParser);
    
    }
}
*/