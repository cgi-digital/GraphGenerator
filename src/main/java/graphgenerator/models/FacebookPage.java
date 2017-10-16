package graphgenerator.models;

import javax.persistence.*;


@Entity
@Table(name = "facebookPage")
public class FacebookPage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name="personId")
    private Person person;
    private String facebookPage;
    private String additionalFacebookPageInformation;

    public FacebookPage() {
    }

    public FacebookPage(Person person, String facebookPage, String additionalFacebookPageInformation) {
        this.person = person;
        this.facebookPage = facebookPage;
        this.additionalFacebookPageInformation = additionalFacebookPageInformation;
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

    public String getFacebookPage() {
        return facebookPage;
    }

    public void setFacebookPage(String facebookPage) {
        this.facebookPage = facebookPage;
    }

    public String getAdditionalFacebookPageInformation() {
        return additionalFacebookPageInformation;
    }

    public void setAdditionalFacebookPageInformation(String additionalFacebookPageInformation) {
        this.additionalFacebookPageInformation = additionalFacebookPageInformation;
    }
}
