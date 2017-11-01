package graphgenerator.pdfimport;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import com.google.gson.Gson;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class ImportRestService {
    private final AtomicLong importId = new AtomicLong();
    private static List<RawPerson> persons = new CopyOnWriteArrayList<>();

    @PostMapping("/import-person")
    public String importPersonJson(@RequestBody String personJson) {
        Gson gson = new Gson();
        RawPerson person = gson.fromJson(personJson, RawPerson.class);
        persons.add(importId.intValue(), person);
        person.fixPerson(importId.get());
        importId.incrementAndGet();
        return gson.toJson(person);
    }

    @GetMapping("/get-person/{id}")
    public String getPerson(@PathVariable int id) {
        Gson gson = new Gson();
        return gson.toJson(persons.get(id));
    }

    @DeleteMapping("/remove-person/{id}")
    public String removePerson(@PathVariable int id) {
        this.persons.remove(id);
        return "Success";
    }

    @GetMapping("/get-all-person-ids")
    public String getAllPersons() {
        Gson gson = new Gson();
        SimplePersonList simplePersons = new SimplePersonList(persons);
        return gson.toJson(simplePersons);
    }

}