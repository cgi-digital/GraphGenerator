package graphgenerator.pdfimport;

import java.util.List;
import java.util.ArrayList;

public class SimplePersonList {
    
    private List<SimplePerson> persons;

    SimplePersonList() {
        persons = new ArrayList<>();
    }

    SimplePersonList(List<RawPerson> persons) {
        this.persons = new ArrayList<>();
        for(RawPerson person : persons)
            this.persons.add(new SimplePerson(person.getId(), person.getName()));
    }

    void add(long id, String name) {
        this.persons.add(new SimplePerson(id, name));
    }

    private class SimplePerson {
        private long id;
        private String name;
    
        SimplePerson(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}