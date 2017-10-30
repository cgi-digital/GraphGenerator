package graphgenerator.pdfimport;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class ImportRestService {
    private final AtomicLong importId = new AtomicLong();
    private static List<RawPerson> persons = new CopyOnWriteArrayList<>();

    @PostMapping("/import-person")
    public RawPerson importPersonJson(@RequestBody RawPerson person) {
        person.fixPerson(importId.get());
        persons.add(importId.intValue(), person);
        importId.incrementAndGet();
        return person;
    }

    @GetMapping("/get-person/{id}")
    public RawPerson getPerson(@PathVariable int id) {
        return persons.get(id);
    }

}