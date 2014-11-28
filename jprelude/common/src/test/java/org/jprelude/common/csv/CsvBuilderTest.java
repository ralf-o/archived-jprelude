package org.jprelude.common.csv;

import org.jprelude.common.util.Seq;
import org.junit.Test;

public class CsvBuilderTest {
    @Test
    public void testCsvBuilder() {
        final Seq<Person> persons = Seq.of(
            new Person("Meryl", "Streep"),
            new Person("Al", "Pacino")
        );

        final CsvBuilder<Person> csvBuilder = new CsvBuilder();

        csvBuilder.setColumns(
            new CsvColumn<>("FIRST_NAME", Person::getFirstName),
            new CsvColumn<>("LAST_NAME", Person::getLastName),
            new CsvColumn<>("INITIALS", p ->
                p.getFirstName().charAt(0) + "" + p.getLastName().charAt(0))
        );

        persons
            .map(csvBuilder.asMapper())
            .forEach(System.out::println);
        
        csvBuilder
            .applyOn(persons)
            .forEach(System.out::println);
    }
}

class Person {
    private final String firstName;
    private final String lastName;
    
    public Person(final String firstName, final String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    public String getFirstName() {
        return this.firstName;
    }
    
    public String getLastName() {
        return this.lastName;
    }
}
