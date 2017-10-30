package graphgenerator.pdfimport;

public class Associate {
    private String name;
    private String relation = null;
    private AssociateType type = AssociateType.OTHER;

    void fixAssociate() {
        if(relation.toLowerCase().contains("wife") || relation.toLowerCase().contains("husband") || relation.toLowerCase().contains("partner"))
            type = AssociateType.SPOUSE;
        else if(relation.toLowerCase().contains("grand mother") || relation.toLowerCase().contains("grand father") || relation.toLowerCase().contains("grand parent"))
            type = AssociateType.GRAND_PARENT;
        else if(relation.toLowerCase().contains("mother") || relation.toLowerCase().contains("father") || relation.toLowerCase().contains("parent"))
            type = AssociateType.PARENT;
        else if(relation.toLowerCase().contains("brother") || relation.toLowerCase().contains("sister"))
            type = AssociateType.SIBLING;
        else if(relation.toLowerCase().contains("uncle") || relation.toLowerCase().contains("aunt") ||
            relation.toLowerCase().contains("nephew") || relation.toLowerCase().contains("niece") ||
            relation.toLowerCase().contains("cousin"))
            type = AssociateType.EXTENDED_FAMILY;
        else
            type = AssociateType.OTHER;
    }
}