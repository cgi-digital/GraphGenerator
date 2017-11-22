package graphgenerator.models.dao;

import graphgenerator.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;

@Service("PersonService")
public class PersonServiceImpl implements PersonService{

    @PersistenceContext
     private EntityManager em;

    @Autowired
     private PersonDAO personDAO;

    @Override
    public List<Person> findAllPersons() {
        return personDAO.findAll();
    }

    @Override
    public Set<String> findAllNames()
    {
        return personDAO.findAllNames();
    }

    @Modifying
    @Transactional
    @Override
    public Person createPerson(Person person)
    {
        return personDAO.save(person);
    }

    @Modifying
    @Transactional
    @Override
    public Person updatePerson(Person person) {
        return em.merge(person);
    }

}
