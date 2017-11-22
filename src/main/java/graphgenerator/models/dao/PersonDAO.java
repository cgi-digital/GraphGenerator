package graphgenerator.models.dao;

import graphgenerator.models.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface PersonDAO extends JpaRepository<Person, Long>
{
    public List<Person> findAll();

    @Query(value = "select originalName from Person ")
    Set<String> findAllNames();



}