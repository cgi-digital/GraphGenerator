package graphgenerator.pdfimport;

import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@RestController
public class ImportRestService<Public> {
    private static final AtomicLong currentId = new AtomicLong();
    private static ConcurrentMap<Long, RawPerson> persons = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger("ImportRestService");
    @PersistenceContext
    private EntityManager em;

    @Autowired
    private RawPersonDAO personDAO;

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
    
    @Modifying
    @Transactional
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
                personDAO.save(person);
                persons.put(person.getId(), person);
            } catch(RawPersonProcessingException e) {
                logger.warn(e.getMessage());
            }
        }
        return "{\"status\":\"done\"}";
        //Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //return gson.toJson(persons);
    }

    public static long getImportId() {
        Long importId = currentId.get();
        currentId.getAndIncrement();
        return importId;
    }

}