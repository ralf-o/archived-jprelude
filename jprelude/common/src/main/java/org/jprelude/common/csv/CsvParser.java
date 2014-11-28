/*
package org.jprelude.common.csv;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jprelude.common.function.UnaryFunction;
import org.jprelude.common.io.TextReader;
import org.jprelude.common.util.Seq;
import rx.Observable;

public class CsvParser extends CsvHandler<CsvParser> implement UnaryFunction<String, Seq<String>> {
    public CsvParser() {
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
        final CsvParser<Person> csvParser = new CsvParser<>()
            .setSeparator(";")
                
                
        TextReader reader = new TextReader("c:/test.csv");
        
        reader.readLines().map(csvParser);
    
    }
}
*/
