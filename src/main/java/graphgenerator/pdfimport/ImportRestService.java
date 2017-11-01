package graphgenerator.pdfimport;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
public class ImportRestService {
    private final AtomicLong currentId = new AtomicLong();
    private static ConcurrentMap<Long, RawPerson> persons = new ConcurrentHashMap<>();

    @PostMapping("/import-person")
    public String importPersonJson(@RequestBody String personJson) {
        Gson gson = new Gson();
        RawPerson person = gson.fromJson(personJson, RawPerson.class);
        Long importId = currentId.incrementAndGet();
        persons.put(importId, person);
        person.fixPerson(importId);
        return gson.toJson(person);
    }

    @GetMapping("/get-person/{id}")
    public String getPerson(@PathVariable Long id) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(persons.get(id));
    }

    @DeleteMapping("/remove-person/{id}")
    public String removePerson(@PathVariable long id) {
        this.persons.remove(id);
        return "Success";
    }

    @GetMapping("/get-all-person-ids")
    public String getAllPersons() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SimplePersonList simplePersons = new SimplePersonList(persons.values());
        return gson.toJson(simplePersons);
    }

}