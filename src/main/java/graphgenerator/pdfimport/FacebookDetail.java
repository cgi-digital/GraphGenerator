package graphgenerator.pdfimport;

import javax.persistence.*;

@Entity
@Table(name = "raw_facebook_details")
class FacebookDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String link;
    private String status;

    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name="raw_person_id")
    private RawPerson rawPerson;

    public FacebookDetail() {
        
    }

    public FacebookDetail(String link, RawPerson owner) {
        this.link = link;
        this.rawPerson = owner;
    }

    public FacebookDetail(String link, String status, RawPerson owner) {
        this.link = link;
        this.status = status;
        this.rawPerson = owner;
    }

    String getLink() {
        return this.link;
    }

    String status() {
        return this.status;
    }

}