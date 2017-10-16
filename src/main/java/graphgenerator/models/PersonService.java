package graphgenerator.models;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface PersonService {

    public List<Person> findAllPersons();

    public Person createPerson(Person person);

    public Person updatePerson(Person person);
}
