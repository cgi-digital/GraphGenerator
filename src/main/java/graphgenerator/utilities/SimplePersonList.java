package graphgenerator.utilities;

import graphgenerator.models.Person;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

public class SimplePersonList {
    
    private List<SimplePerson> persons;

    SimplePersonList() {
        persons = new ArrayList<>();
    }

    public SimplePersonList(Collection<Person> persons) {
        this.persons = new ArrayList<>();
        for(Person person : persons)
            this.persons.add(new SimplePerson(person.getId(), person.getName()));
    }

    public int size() {
        if(persons == null)
            return 0;
        
        return persons.size();
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