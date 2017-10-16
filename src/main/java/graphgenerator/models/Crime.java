package graphgenerator.models;

import javax.persistence.*;

@Entity
@Table(name = "crime")
public class Crime
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name="personId")
    private Person person;
    private String crime;

    public Crime() {
    }

    public Crime(Person person, String crime)
    {
        this.person = person;
        this.crime = crime;
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

    public String getCrime() {
        return crime;
    }

    public void setCrime(String crime) {
        this.crime = crime;
    }
}
