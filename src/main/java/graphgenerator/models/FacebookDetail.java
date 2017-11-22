package graphgenerator.models;

import javax.persistence.*;

@Entity
@Table(name = "facebook")
public class FacebookDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String link;

    @Column(columnDefinition = "TEXT")
    private String additionalFacebookPageInformation;

    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name="person_id")
    private Person person;

    public FacebookDetail()
    { }

    public FacebookDetail(String link, Person owner) {
        this.link = link;
        this.person = owner;
    }

    public FacebookDetail(String link, String additionalFacebookPageInformation, Person owner) {
        this.link = link;
        this.additionalFacebookPageInformation = additionalFacebookPageInformation;
        this.person = owner;
    }

    public  String getLink() {
        return this.link;
    }

    public String getAdditionalFacebookPageInformation() {
        return this.additionalFacebookPageInformation;
    }

}