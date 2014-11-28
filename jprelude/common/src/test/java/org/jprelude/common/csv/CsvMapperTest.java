package org.jprelude.common.csv;

import java.util.stream.Collectors;
import org.jprelude.common.util.Seq;
import org.junit.Test;

public class CsvMapperTest {
    @Test
    public void testApplyMethod() {
        Seq<Seq<?>> rows = Seq.of(
            Seq.of("Col1", "Col2", "Col3"),
            Seq.of("A1", "A2", "A3"),
            Seq.of("B1", "B2", "B3")
        );
        
        CsvMapper csvMapper = new CsvMapper().setSeparator("|");
        System.out.println(Seq.concat(rows.map(csvMapper)).collect(Collectors.joining()));
        rows.map(csvMapper::apply).forEach(System.out::println);
        csvMapper.applyOn(rows).forEach(System.out::println);
    }
}
