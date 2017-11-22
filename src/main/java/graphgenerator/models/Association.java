package graphgenerator.models;

import javax.persistence.*;

@Entity
@Table(name = "Association")
public class Association {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String personNameTo;
    private String personNameFrom;
    private String relation = null;
    private AssociationType type;

    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name="person_id")
    private Person person;

    public Association()
    { }

    public Association(String personNameTo, Person owner) {
        this.personNameTo = personNameTo.trim();
        this.personNameFrom = owner.getOriginalName().trim();
        this.type = AssociationType.OTHER;
        this.person = owner;
    }
    
    public Association(String personNameTo, String relation, Person owner)
    {
        this.personNameFrom = owner.getName();
        this.personNameTo = personNameTo;
        this.relation = relation;
        this.person = owner;

        if(relation != null && !relation.trim().isEmpty())
        {
            if(relation.toLowerCase().contains("wife") || relation.toLowerCase().contains("husband") || relation.toLowerCase().contains("partner"))
                type = AssociationType.SPOUSE;
            else if(relation.toLowerCase().contains("grand mother") || relation.toLowerCase().contains("grand father") || relation.toLowerCase().contains("grand parent"))
                type = AssociationType.GRAND_PARENT;
            else if(relation.toLowerCase().contains("mother") || relation.toLowerCase().contains("father") || relation.toLowerCase().contains("parent"))
                type = AssociationType.PARENT;
            else if(relation.toLowerCase().contains("brother") || relation.toLowerCase().contains("sister"))
                type = AssociationType.SIBLING;
            else if(relation.toLowerCase().contains("son") || relation.toLowerCase().contains("daughter") || relation.toLowerCase().contains("child"))
                type = AssociationType.CHILD;
            else if(relation.toLowerCase().contains("uncle") || relation.toLowerCase().contains("aunt") ||
                relation.toLowerCase().contains("nephew") || relation.toLowerCase().contains("niece") ||
                relation.toLowerCase().contains("cousin"))
                type = AssociationType.EXTENDED_FAMILY;
            else
                type = AssociationType.OTHER;
        }
    }


    public Long getId() {
        return id;
    }

    public String getPersonNameTo() {
        return personNameTo;
    }

    public String getPersonNameFrom() {
        return personNameFrom;
    }

    public String getRelation() {
        return relation;
    }

    public AssociationType getType() {
        return type;
    }

    public Person getPerson() {
        return person;
    }

    @Override
    public String toString() {
        return "Owner: "+ this.personNameFrom +" Association Name: " + this.personNameTo + ", Relationship: " + this.relation + ", Association Type: " + type.name();
    }
}