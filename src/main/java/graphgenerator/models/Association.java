package graphgenerator.models;

import javax.persistence.*;

@Entity
@Table(name = "association")
public class Association
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name="personId")
    private Person person;

    private String personNameFrom;
    private String personNameTo;
    private String type;


    public Association() {
    }

    public Association(Person person, String personNameFrom, String personNameTo, String type)
    {
        this.person = person;
        this.personNameFrom = personNameFrom;
        this.personNameTo = personNameTo;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getPersonNameFrom() {
        return personNameFrom;
    }

    public void setPersonNameFrom(String personNameFrom) {
        this.personNameFrom = personNameFrom;
    }

    public String getPersonNameTo() {
        return personNameTo;
    }

    public void setPersonNameTo(String personNameTo) {
        this.personNameTo = personNameTo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
