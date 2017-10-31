package graphgenerator.pdfimport;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class RawPerson {
    private String name;
    private String firstName;
    private String familyName;
    private String aka;
    private String dob;
    private LocalDate dateOfBirth;
    private List<FacebookDetail> facebook;
    private List<Associate> knownAssociates;
    private String associatedCrimes;
    private String additionalInformation;
    private long importId = -1;

    public void fixPerson(long importId) {
        this.importId = importId;
        if(name.startsWith("Facebook")) {
            name = findName(name);
        }

        if(name.split(" ").length > 2) {
            familyName = name.split(" ")[name.split(" ").length-1];
            firstName = "";
            for(int i = 0; i < name.split(" ").length-1; i++)
                firstName += name.split(" ")[i] + " ";
            firstName = firstName.trim();
        }
        else if(name.split(" ").length == 2) {
            firstName = name.split(" ")[0];
            familyName =name.split(" ")[1];
        } else {
            familyName = name;
            firstName = "";
        }
        if(dob != null && !dob.trim().isEmpty()) {
            String dobString = dob.trim();
            if(dob.indexOf("(") >= 0)
                dobString = dob.split("\\(")[0].trim();
            
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
                dateOfBirth = LocalDate.parse(dobString, formatter);
            } catch (DateTimeParseException e) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");
                dateOfBirth = LocalDate.parse(dobString, formatter);
            }
        }
        if(knownAssociates.size() > 0) {
            for(Associate associate : knownAssociates)
                associate.fixAssociate();
        }
    }

    private String findName(String name) {
        int nameStartPostion = name.indexOf("‘") + 1;
        int nameFinishPositon = name.indexOf("’");
        return name.substring(nameStartPostion, nameFinishPositon);
    }

}