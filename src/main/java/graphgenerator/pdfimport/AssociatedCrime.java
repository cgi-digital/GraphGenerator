package graphgenerator.pdfimport;

import java.time.LocalDate;

public class AssociatedCrime {
    private String description;
    private String location;
    private LocalDate reportedDate;

    public AssociatedCrime(String description) {
        this.description = description;
    }
}