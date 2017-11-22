package graphgenerator.models.dao;

import graphgenerator.models.Crime;

import java.util.List;

public interface CrimeService
{
    public List<Crime> findAll();

    public Crime findById(Long id);

    public Crime updateCrime(Crime crime);

    public void deleteCrime(Crime crime);
}
