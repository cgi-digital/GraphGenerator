package graphgenerator.pdfimport;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import javax.persistence.*;

@Entity
@Table(name = "raw_person", uniqueConstraints= @UniqueConstraint(columnNames = {"id"}))
public class RawPerson {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id = -1;
    private String originalName;
    private String firstName;
    private String familyName;
    private String aka;
    private boolean facebookName = false;
    private String originalDateofBirthString;
    @Convert(converter = LocalDateAttributeConverter.class)
    private LocalDate dateOfBirth;
    private String rawCrimeData;
    private String additionalInformation;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(targetEntity=FacebookDetail.class, mappedBy="rawPerson", cascade = CascadeType.ALL)
    private List<FacebookDetail> facebookDetails;
    
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(targetEntity=Associate.class, mappedBy="rawPerson", cascade = CascadeType.ALL)
    private List<Associate> knownAssociates;
    
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(targetEntity=AssociatedCrime.class, mappedBy="rawPerson", cascade = CascadeType.ALL)
    private List<AssociatedCrime> associatedCrimes;

    // Empty constructor for Hibernate
    public RawPerson() {
    }

    public RawPerson(String name) {
        this.originalName = name;
        parseName();
    }

    private void parseName() {
        if(originalName.startsWith("Facebook")) {
            originalName = findName(originalName);
            facebookName = true;
        }

        if(originalName.split(" ").length > 2) {
            familyName = originalName.split(" ")[originalName.split(" ").length-1];
            firstName = "";
            for(int i = 0; i < originalName.split(" ").length-1; i++)
                firstName += originalName.split(" ")[i] + " ";
            firstName = firstName.trim();
        }
        else if(originalName.split(" ").length == 2) {
            firstName = originalName.split(" ")[0];
            familyName =originalName.split(" ")[1];
        } else {
            familyName = originalName;
            firstName = "";
        }
    }

    public RawPerson(String name, String aka) {
        this.originalName = name;
        parseName();
        this.aka = aka;
    }

    private String findName(String name) {
        int nameStartPostion = name.indexOf("‘") + 1;
        int nameFinishPositon = name.indexOf("’");
        return name.substring(nameStartPostion, nameFinishPositon);
    }

    public String getName() {
        return this.originalName;
    }

    public long getId() {
        return this.id;
    }

    private void setDateOfBirth(String dobString) throws DateTimeParseException {
        this.originalDateofBirthString = dobString;
        if(dobString != null && !dobString.trim().isEmpty()) {
            dobString = dobString.trim();
            if(dobString.indexOf("(") >= 0)
                dobString = dobString.split("\\(")[0].trim();
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
                this.dateOfBirth = LocalDate.parse(dobString, formatter);
            } catch (DateTimeParseException e) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");
                this.dateOfBirth = LocalDate.parse(dobString, formatter);
            }
        }
    }

    public void setFaceBookDetails(List<FacebookDetail> details) {
        this.facebookDetails = details;
    }

    public void setKnownAssociates(List<Associate> associates) {
        this.knownAssociates = associates;
    }

    public void setAdditionalInformation(String additionalInfo) {
        this.additionalInformation = additionalInfo;
    }

    public void setAssocatedCrimes(List<AssociatedCrime> crimes) {
        this.associatedCrimes = crimes;
    }

    public void setRawAssociatedCrimes(String rawAssociatedCrimes) {
        this.rawCrimeData = rawAssociatedCrimes;
    }

    /* Methods below this point are all for PDF Imports */
    public static RawPerson rawPersonFromPDFString(String stringFromPDF) throws RawPersonProcessingException {
        Logger logger = LoggerFactory.getLogger("ImportRestService");
        String[] personArr = stringFromPDF.split("\n");
        Map<String, DataPosition> dataPositions = getDataPositions(personArr);
        RawPerson person = null;
        String data;
        if(dataPositions.containsKey("name")) {
            data = getData(personArr, " ", dataPositions.get("name"));
            if(data.length() > 0 && data.split(": ").length > 1) {
                data = data.split(": ")[1];
                if(data.split("\\(").length > 1) {
                    String name = data.split("\\(")[0];
                    String aka = data.split("\\(")[1].trim().substring(0, data.split("\\(")[1].trim().length() - 1);
                    person = new RawPerson(name, aka);
                } else {
                    person = new RawPerson(data);
                }
            } else {
                throw new RawPersonProcessingException("Person parsed does not have a valid name field.  Name field specified was:\n" + data);
            }
        } else {
            throw new RawPersonProcessingException("Person parsed does not have a name field specified");
        }
        
        if(dataPositions.containsKey("dob")) {
            data = getData(personArr, " ", dataPositions.get("dob"));
            if(data.split(": ").length > 1) {
                data = data.split(": ")[1];
                try {
                    person.setDateOfBirth(data);
                } catch(DateTimeParseException e) {
                    logger.warn("Unable to parse date of birth data for person " + person.getName() + " with id " + person.getId());
                }
            }
            else {
                logger.warn("Unable to parse date of birth data for person " + person.getName() + " with id " + person.getId());
            }
        }

        if(dataPositions.containsKey("facebook")) {
            data = getData(personArr, "\n", dataPositions.get("facebook"));
            if(data.length() > 0 && data.split(": ").length > 1) {
                data = data.split(": ")[1];
                if(!data.contains("None")) {
                    List<FacebookDetail> facebookDetails = new ArrayList<>();
                    String[] facebookData = data.split("\n");
                    if(facebookData.length > 0) {
                        for(String link : facebookData) {
                            if(link.split("\\(").length > 1) {
                                String href = link.split("\\(")[0];
                                String status = link.split("\\(")[1].trim().substring(0, link.split("\\(")[1].trim().length()-1);
                                facebookDetails.add(new FacebookDetail(href.trim(), status.trim(), person));
                            } else {
                                facebookDetails.add(new FacebookDetail(link.trim(), person));
                            }
                        }
                        person.setFaceBookDetails(facebookDetails);
                    }
                }
            } else {
                logger.warn("Unable to parse facebook data for person " + person.getName() + " with id " + person.getId());
            }
        }

        if(dataPositions.containsKey("associates")) {
            data = getData(personArr, " ", dataPositions.get("associates"));
            if(data.length() > 0 && data.split(": ").length > 1) {
                data = data.split(": ")[1];
                List<Associate> associates = parseAssociates(data, person);
                person.setKnownAssociates(associates);
            } else {
                logger.warn("Unable to parse associates data for person " + person.getName() + " with id " + person.getId());
            }
        }

        if(dataPositions.containsKey("crimes")) {
            data = getData(personArr, "\n", dataPositions.get("crimes"));
            if(data.length() > 0) {
                data = data.substring(18).trim();
                person.setRawAssociatedCrimes(data);
                if(data.trim().length() > 0) {
                    List<AssociatedCrime> crimes = new ArrayList<>();
                    for(String crimeData : data.split("\n \n")) {
                        crimes.add(new AssociatedCrime(crimeData.replaceAll("\n", ""), person));
                    }
                    person.setAssocatedCrimes(crimes);
                }
            }
        } else {
            logger.warn("Crime data position data : " + dataPositions.get("crimes"));
        }
        
        if(dataPositions.containsKey("additionalInfo")) {
            data = getData(personArr, " ", dataPositions.get("additionalInfo"));
            if(data.split(": ").length > 1) {
                person.setAdditionalInformation(data.split(": ")[1]);
            }
        }
        
        return person;
    }

    private static List<Associate> parseAssociates(String data, RawPerson person) {
        List<Associate> associates = new ArrayList<>();
        boolean relation = false;
        String currentAssociate = "";
        String relationship = "";
        int nesting = -1;

        while(data.length() > 0) {
            char currentChar = data.charAt(0);
            data = data.substring(1);
            if(currentChar == ',' && !relation) {
                if(relationship.isEmpty()) {
                    associates.add(new Associate(currentAssociate.trim(), person));
                } else {
                    associates.add(new Associate(currentAssociate.trim(), relationship.trim(), person));
                }
                relationship = "";
                currentAssociate = "";
                data = data.substring(1);
            }
            else if(currentChar == '(') {
                relation = true;
                nesting++;
            }
            else if(currentChar == ')') {
                if(nesting == 0) {
                    relation = false;
                    nesting--;
                } else {
                    nesting--;
                }
            }
            else if(relation)
                relationship += currentChar;
            else
                currentAssociate += currentChar;
        }
        if(relationship.isEmpty()) {
            associates.add(new Associate(currentAssociate.trim(), person));
        } else {
            associates.add(new Associate(currentAssociate, relationship.trim(), person));
        }
        return associates;
    }

    private static String getData(String[] personArr, String seperator, DataPosition position) {
        String data = "";
        for(int i = position.start; i <= position.finish; i++) {
            data += personArr[i] + seperator;
        }
        return data;
    }
    
    private static Map<String, DataPosition> getDataPositions(String[] personArr) {
        List<DataPosition> positions = new ArrayList<>();
        for(int i = 0; i < personArr.length; i++) {
            if(personArr[i].contains("Name:")) {
                positions.add(new DataPosition("name", i));
            } else if(personArr[i].contains("Date of Birth:")) {
                positions.add(new DataPosition("dob", i));
            } else if(personArr[i].contains("Facebook page:") ||
                            personArr[i].contains("Facebook page(s):") ||
                            personArr[i].contains("Facebook pages:")) {
                positions.add(new DataPosition("facebook", i));
            } else if(personArr[i].contains("Known associates:")) {
                positions.add(new DataPosition("associates", i));
            } else if(personArr[i].contains("Associated crimes:")) {
                positions.add(new DataPosition("crimes", i));
            } else if(personArr[i].contains("Additional information:") ||
                            personArr[i].contains("Additional Information:")) {
                positions.add(new DataPosition("additionalInfo", i));
            }
        }

        Collections.sort(positions);

        for(int i = 0; i < positions.size()-1; i++) {
            positions.get(i).finish = positions.get(i+1).start - 1;
        }
        positions.get(positions.size()-1).finish = personArr.length-1;

        Map<String, DataPosition> dataPositions = new HashMap<>();
        for(DataPosition position : positions)
            dataPositions.put(position.name, position);
        return dataPositions;
    }
}