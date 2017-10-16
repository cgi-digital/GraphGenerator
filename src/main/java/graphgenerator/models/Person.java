package graphgenerator.models;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;

import java.util.List;

@Entity
@Table(name = "person", uniqueConstraints= @UniqueConstraint(columnNames = {"personId"}))
public class Person
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long personId;

    private String name;
    private String additionalNameInformation;
    private String dob;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(targetEntity=Alias.class, mappedBy="person", cascade = CascadeType.ALL)
    private List<Alias> aliases;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(targetEntity=FacebookPage.class, mappedBy="person", cascade = CascadeType.ALL)
    private List<FacebookPage> facebook;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(targetEntity=Association.class, mappedBy="person", cascade = CascadeType.ALL)
    private List<Association> associations;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(targetEntity=Crime.class, mappedBy="person", cascade = CascadeType.ALL)
    private List<Crime> crimes;

    private String info;
    private String pictureFilePath;

    public Person()
    {

    }

    public Long getPersonId()
    {
        return this.personId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdditionalNameInformation() {
        return additionalNameInformation;
    }

    public void setAdditionalNameInformation(String additionalNameInformation) {
        this.additionalNameInformation = additionalNameInformation;
    }

    public List<Alias> getAliases() {
        return aliases;
    }

    public void setAliases(List<Alias> aliases) {
        this.aliases = aliases;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public List<FacebookPage> getFacebook() {
        return facebook;
    }

    public void setFacebook(List<FacebookPage> facebook) {
        this.facebook = facebook;
    }

    public List<Association> getAssociations() {
        return associations;
    }

    public void setAssociations(List<Association> associations) {
        this.associations = associations;
    }

    public List<Crime> getCrimes() {
        return crimes;
    }

    public void setCrimes(List<Crime> crimes) {
        this.crimes = crimes;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getPictureFilePath() {
        return pictureFilePath;
    }

    public void setPictureFilePath(String pictureFilePath) {
        this.pictureFilePath = pictureFilePath;
    }
}
