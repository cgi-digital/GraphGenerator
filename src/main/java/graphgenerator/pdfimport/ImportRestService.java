package graphgenerator.pdfimport;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.PostMapping;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class ImportRestService<Public> {
    private static final AtomicLong currentId = new AtomicLong();
    private static ConcurrentMap<Long, RawPerson> persons = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger("ImportRestService");

    // @PostMapping("/import-person")
    // public String importPersonJson(@RequestBody String personJson) {
    //     Gson gson = new Gson();
    //     RawPerson person = gson.fromJson(personJson, RawPerson.class);
    //     Long importId = currentId.incrementAndGet();
    //     persons.put(importId, person);
    //     person.fixPerson(importId);
    //     return gson.toJson(person);
    // }

    @GetMapping("/get-person/{id}")
    public String getPerson(@PathVariable Long id) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(persons.get(id));
    }

    @DeleteMapping("/remove-person/{id}")
    public String removePerson(@PathVariable long id) {
        ImportRestService.persons.remove(id);
        return "Success";
    }

    @GetMapping("/get-all-person-ids")
    public String getAllPersons() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SimplePersonList simplePersons = new SimplePersonList(ImportRestService.persons.values());
        return gson.toJson(simplePersons);
    }

    @PostMapping("/import-pdf")
    public String importPdf(@RequestParam("file") MultipartFile file) throws IOException  {
        PDDocument doc = PDDocument.load(file.getBytes());
        String docContent =  new PDFTextStripper().getText(doc);
        String content = "";
        
        // Remove heading
        for(String line : docContent.split("\n")) {
            if(!line.contains("Page")) {
                content += line + "\n";
            }
        }
        
        docContent = content;
        content = "";
        String[] personData = docContent.split("Name: ");
        for(int i = 1; i < personData.length; i++) {
            try {
                RawPerson person = RawPerson.rawPersonFromPDFString("Name: " + personData[i].trim());
                persons.put(person.getId(), person);
            } catch(RawPersonProcessingException e) {
                logger.warn(e.getMessage());
            }
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return "<!DOCTYPE html><html><head><title>Test Response</title></head><body><pre>"+gson.toJson(persons)+"\n----------------------\n"+content+"</pre></body></html>";
    }

    public static long getImportId() {
        Long importId = currentId.get();
        currentId.getAndIncrement();
        return importId;
    }

}