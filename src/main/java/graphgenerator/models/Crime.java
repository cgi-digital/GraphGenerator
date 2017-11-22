package graphgenerator.models;

import graphgenerator.utilities.LocalDateAttributeConverter;

import java.time.LocalDate;
import java.util.Set;
import javax.persistence.*;

@Entity
@Table(name = "Crime")
public class Crime {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;
    @Convert(converter = LocalDateAttributeConverter.class)
    private LocalDate reportedDate;

//    @ManyToOne(cascade= CascadeType.ALL)
//    @JoinColumn(name="raw_person_id")

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "PersonCrime", joinColumns = @JoinColumn(name = "crime_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "person_id", referencedColumnName = "id"))
    private Set<Person> persons;

    public Crime() {

    }

    public Crime(String description, Set<Person> owner)
    {
        this.description = description;
        this.persons = owner;
    }


    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getReportedDate() {
        return reportedDate;
    }

    public Set<Person> getPersons() {
        return persons;
    }

    public void setPersons(Set<Person> persons) {
        this.persons = persons;
    }

    public void addPerson(Person person)
    {
        this.persons.add(person);
    }

    public void removePerson(Person person)
    {
        this.persons.remove(person);
    }
}