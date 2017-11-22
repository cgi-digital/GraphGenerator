package graphgenerator.models.dao;

import graphgenerator.models.Crime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface CrimeDAO extends JpaRepository<Crime, Long>
{
    public List<Crime> findAll();

    public Crime findById(Long id);

}