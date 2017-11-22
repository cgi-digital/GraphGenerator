package graphgenerator.models.dao;

import graphgenerator.models.Person;

import java.util.List;
import java.util.Set;

public interface PersonService
{
    public List<Person> findAllPersons();
    public Set<String> findAllNames();
    public Person createPerson(Person person);
    public Person updatePerson(Person person);

}
