package graphgenerator.pdfimport;

import javax.persistence.*;

@Entity
@Table(name = "raw_associate")
public class Associate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String name;
    private String relation = null;
    private AssociateType type;

    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name="raw_person_id")
    private RawPerson rawPerson;

    public Associate() {
        
    }

    public Associate(String name, RawPerson owner) {
        this.name = name;
        this.type = AssociateType.OTHER;
        this.rawPerson = owner;
    }
    
    public Associate(String name, String relation, RawPerson owner) {
        this.name = name;
        this.relation = relation;
        this.rawPerson = owner;

        if(relation != null && !relation.trim().isEmpty()) {
            if(relation.toLowerCase().contains("wife") || relation.toLowerCase().contains("husband") || relation.toLowerCase().contains("partner"))
                type = AssociateType.SPOUSE;
            else if(relation.toLowerCase().contains("grand mother") || relation.toLowerCase().contains("grand father") || relation.toLowerCase().contains("grand parent"))
                type = AssociateType.GRAND_PARENT;
            else if(relation.toLowerCase().contains("mother") || relation.toLowerCase().contains("father") || relation.toLowerCase().contains("parent"))
                type = AssociateType.PARENT;
            else if(relation.toLowerCase().contains("brother") || relation.toLowerCase().contains("sister"))
                type = AssociateType.SIBLING;
            else if(relation.toLowerCase().contains("son") || relation.toLowerCase().contains("daughter") || relation.toLowerCase().contains("child"))
                type = AssociateType.CHILD;
            else if(relation.toLowerCase().contains("uncle") || relation.toLowerCase().contains("aunt") ||
                relation.toLowerCase().contains("nephew") || relation.toLowerCase().contains("niece") ||
                relation.toLowerCase().contains("cousin"))
                type = AssociateType.EXTENDED_FAMILY;
            else
                type = AssociateType.OTHER;
        }

    }

    @Override
    public String toString() {
        return "Associate Name: " + this.name + ", Relationship: " + this.relation + ", Associate Type: " + type.name();
    }
}