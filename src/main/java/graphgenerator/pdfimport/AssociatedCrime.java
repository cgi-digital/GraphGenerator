package graphgenerator.pdfimport;

import java.time.LocalDate;
import javax.persistence.*;

@Entity
@Table(name = "raw_crime")
public class AssociatedCrime {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String description;
    private String location;
    @Convert(converter = LocalDateAttributeConverter.class)
    private LocalDate reportedDate;

    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name="raw_person_id")
    private RawPerson rawPerson;

    public AssociatedCrime() {

    }

    public AssociatedCrime(String description, RawPerson owner) {
        this.description = description;
        this.rawPerson = owner;
    }
}