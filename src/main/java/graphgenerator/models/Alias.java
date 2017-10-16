package graphgenerator.models;

import javax.persistence.*;


@Entity
@Table(name = "alias")
public class Alias {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name="personId")
    private Person person;
    private String alias;

    public Alias() {
    }

    public Alias(Person person, String alias) {
        this.person = person;
        this.alias = alias;
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
