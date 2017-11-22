package graphgenerator.utilities;

import graphgenerator.models.Crime;
import graphgenerator.models.Person;
import graphgenerator.models.dao.CrimeDAO;
import graphgenerator.models.dao.CrimeService;
import graphgenerator.models.dao.PersonDAO;
import graphgenerator.models.dao.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CrimeDeduplicator
{
    @Autowired
    PersonService personService;

    @Autowired
    CrimeService crimeService;

    private CrimeDeduplicator()
    {

    }

    public final void deduplicateCrimes()
    {
        Set<String> names = personService.findAllNames();
        List<Crime> crimes = crimeService.findAll();


        Map<Long,List<String>> crimeToNamesMap = new HashMap<>();

        for(Crime crime : crimes)
        {
            for(String personName : names)
            {
                personName = personName.trim();
                if (crime.getDescription().contains(personName))
                {
                    if (crimeToNamesMap.get(crime.getId()) == null)
                    {
                        crimeToNamesMap.put(crime.getId(), new ArrayList<>());
                    }
                    crimeToNamesMap.get(crime.getId()).add(personName);
                }
            }
        }

        List<Long> groupedCrimes = new ArrayList<>();


        for(Crime crime : crimes)
        {
            if(crimeToNamesMap.get(crime.getId())!= null && crimeToNamesMap.get(crime.getId()).size() > 1)
            {
                for(Long otherCrimeID : crimeToNamesMap.keySet())
                {
                    if(crime.getId() != otherCrimeID)
//                            && !groupedCrimes.contains(otherCrimeID)
//                            && !groupedCrimes.contains(crime.getId()))
                    {
                        if(compareNamesLists(crimeToNamesMap.get(crime.getId()), crimeToNamesMap.get(otherCrimeID)))
                        {
                            Crime otherCrime = crimeService.findById(otherCrimeID);

                            for(Person person : otherCrime.getPersons())
                            {
                                person.addCrime(crime);
                                person.removeCrime(otherCrime);

                                personService.updatePerson(person);
                            }
                            crimeService.deleteCrime(otherCrime);
                        }
                    }
                }
            }
        }
    }

    private boolean compareNamesLists(List<String> a, List<String> b)
    {
        boolean match = true;
        if(a.size() == b.size())
        {
            for(String name : b)
            {
                if(!a.contains(name))
                {
                    match = false;
                    break;
                }
            }
        }
        else
        {
            match = false;
        }
        return match;
    }

}
