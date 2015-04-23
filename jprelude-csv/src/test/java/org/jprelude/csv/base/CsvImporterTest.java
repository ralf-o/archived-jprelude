package org.jprelude.csv.base;

import org.jprelude.core.io.TextReader;
import org.jprelude.core.util.LineSeparator;
import org.jprelude.core.util.Seq;
import org.jprelude.csv.base.CsvFormat;
import org.jprelude.csv.base.CsvImporter;
import org.jprelude.csv.base.CsvQuoteMode;
import org.jprelude.csv.base.CsvValidator;
import org.junit.Test;

public class CsvImporterTest {
    @Test
    public void myTest() {
        final String csvData =
                "COLUMN1,COLUMN2,COLUMN3\n"
                + "a1,b1,c1\n"
                + "a2x,b2,c2\n"
                + "a3,b3,c3\n";

        final CsvFormat format = CsvFormat.builder()
                .columns(
                    "COLUMN1",
                    "COLUMN2",
                    "COLUMN3"
                )
                .autoTrim(true)
                .escape(null)
                .recordSeparator(LineSeparator.SYSTEM)
                .quoteMode(CsvQuoteMode.ALL)
                .build();

        final CsvValidator validator = CsvValidator.builder()
                .validateColumn(
                        "COLUMN1",
                        "Must have a fixed length of 2",
                        col1 -> col1.hasLength(2))
                .build();

        final CsvImporter<String> importer = CsvImporter.<String>builder()
                .format(format)
                .validator(validator)
                .failOnValidationError(false)
                .onValidationSuccess(() -> System.out.println("Juhu"))
                .onValidationError((e) -> System.out.println("Doooh"))
                .mapper(rec -> rec.get("COLUMN1") + "|" + rec.get("COLUMN2") + "|" + rec.get("COLUMN3"))
                .build();

        final Seq<String> strings = importer.parse(TextReader.forString(csvData));

        strings.forEach(System.out::println);
    }
}
