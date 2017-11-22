package graphgenerator.models.dao;

import graphgenerator.models.Crime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service("CrimeService")
public class CrimeServiceImpl implements CrimeService
{

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private CrimeDAO crimeDAO;

    @Override
    public List<Crime> findAll() {
        return crimeDAO.findAll();
    }

    @Override
    public Crime findById(Long id) {
        return crimeDAO.findById(id);
    }
//
//    @Modifying
//    @Transactional
//    @Override
//    public Crime createCrime(Crime crime)
//    {
//        return crimeDAO.save(crime);
//    }

    @Modifying
    @Transactional
    @Override
    public Crime updateCrime(Crime crime) {
        return em.merge(crime);
    }

    @Override
    public void deleteCrime(Crime crime) {
        crimeDAO.delete(crime);
    }
}
