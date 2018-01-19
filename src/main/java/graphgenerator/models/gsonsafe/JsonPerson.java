package graphgenerator.models.gsonsafe;

import graphgenerator.models.Association;
import graphgenerator.models.AssociationType;
import graphgenerator.models.Crime;
import graphgenerator.models.FacebookDetail;
import graphgenerator.models.Person;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class JsonPerson {
    private long id = -1;
    private String originalName;
    private String firstName;
    private String familyName;
    private String aka;
    private boolean facebookName = false;
    private String originalDateofBirthString;
    private LocalDate dateOfBirth;
    private String rawCrimeData;
    private String additionalInformation;
    private List<JsonFacebookDetail> facebookDetails;
    private List<JsonAssociation> knownAssociations;
    private Set<JsonCrime> crimes;

    public static List<JsonPerson> fromUnsafeList(Collection<Person> collection) {
        List<JsonPerson> safePersons = new ArrayList<>();
        for(Person person: collection) {
            safePersons.add(new JsonPerson(person));
        }
        return safePersons;
    }

    public JsonPerson(Person person) {
        id = person.getId();
        originalName = person.getOriginalName();
        firstName = person.getFirstName();
        familyName = person.getFamilyName();
        aka = person.getAka();
        facebookName = person.isFacebookName();
        originalDateofBirthString = person.getOriginalDateofBirthString();
        dateOfBirth = person.getDateOfBirth();
        rawCrimeData = person.getRawCrimeData();
        additionalInformation = person.getAdditionalInformation();
        facebookDetails = new ArrayList<JsonFacebookDetail>();
        knownAssociations = new ArrayList<JsonAssociation>();
        crimes = new HashSet<JsonCrime>();
        if(person.getFacebookDetails() != null) {
            for(FacebookDetail facebookDetail : person.getFacebookDetails()) {
                facebookDetails.add(new JsonFacebookDetail(facebookDetail));
            }
        }
        if(person.getKnownAssociations() != null) {
            for(Association associate : person.getKnownAssociations()) {
                knownAssociations.add(new JsonAssociation(associate));
            }
        }
        if(person.getCrimes() != null) {
            for(Crime crime : person.getCrimes()) {
                crimes.add(new JsonCrime(crime));
            }
        }
    }

    private class JsonFacebookDetail {
        private Long id;
        private String link;
        private String additionalFacebookPageInformation;

        public JsonFacebookDetail(FacebookDetail facebookDetail) {
            id = facebookDetail.getId();
            link = facebookDetail.getLink();
            additionalFacebookPageInformation =facebookDetail.getAdditionalFacebookPageInformation();
        }
    }

    private class JsonAssociation {
        private Long id;
        private String personNameTo;
        private String personNameFrom;
        private String relation = null;
        private AssociationType type;

        public JsonAssociation(Association associate) {
            id = associate.getId();
            personNameTo = associate.getPersonNameTo();
            personNameFrom = associate.getPersonNameFrom();
            relation = associate.getRelation();
            type = associate.getType();
        }
    }

    private class JsonCrime {
        private Long id;
        private String description;
        private String location;
        private LocalDate reportedDate;
        
        public JsonCrime(Crime crime) {
            id = crime.getId();
            description = crime.getDescription();
            location = crime.getLocation();
            reportedDate = crime.getReportedDate();
        }
    }

}