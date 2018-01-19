package graphgenerator.controllers;

import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletResponse;

import graphgenerator.graph.GraphBuilderCypher;
import graphgenerator.models.dao.PersonDAO;
import graphgenerator.models.gsonsafe.JsonPerson;
import graphgenerator.models.RawPersonProcessingException;
import graphgenerator.models.Person;
import graphgenerator.utilities.CrimeDeduplicator;
import graphgenerator.utilities.SimplePersonList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class Controller<Public>
{
    private static final AtomicLong currentId = new AtomicLong();
    private static ConcurrentMap<Long, Person> persons = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger("Controller");

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    GraphBuilderCypher graphBuilder;

    @Autowired
    private Environment env;

    @Autowired
    CrimeDeduplicator crimeDeduplicator;

    private String neo4jVisualisationUrl;

    public Controller(@Value("${neo4jVisualisationUrl}") String neo4jVisualisationUrl)
    {
        this.neo4jVisualisationUrl = neo4jVisualisationUrl;
    }

    @GetMapping("/person/{id}")
    public JsonPerson getPerson(@PathVariable Long id) {
        return new JsonPerson(personDAO.findOne(id));
    }

    @GetMapping("/property/{key}")
    public JsonObject getProperty(@PathVariable String key) {
        String value = env.getProperty(key);
        JsonObject property = new JsonObject();
        property.addProperty(key, value);
        return property;        
    }

    @GetMapping("/personcount")
    public JsonObject getPersonCount() {
        JsonObject count = new JsonObject();
        count.addProperty("count", personDAO.findAll().size());
        return count;
    }

    @DeleteMapping("/person/{id}")
    public String removePerson(@PathVariable long id)
    {
        Controller.persons.remove(id);
        return "Success";
    }

    @GetMapping("/person")
    public SimplePersonList getAllPersons()
    {
        SimplePersonList simplePersons = new SimplePersonList(personDAO.findAll());
        return simplePersons;
    }
    
    @Modifying
    @Transactional
    @PostMapping("/import")
    public List<JsonPerson> importPdf(@RequestParam("file") MultipartFile file) throws IOException  {

        logger.info("Importing records from the pdf document");

        PDDocument doc = PDDocument.load(file.getBytes());
        String docContent =  new PDFTextStripper().getText(doc);
        String content = "";
        
        // Remove heading
        for(String line : docContent.split("\n"))
        {
            if(!line.contains("Page"))
            {
                content += line + "\n";
            }
        }
        
        docContent = content;
        content = "";
        String[] personData = docContent.split("Name: ");
        for(int i = 1; i < personData.length; i++) {
            try
            {
                Person person = Person.rawPersonFromPDFString("Name: " + personData[i].trim());
                person = personDAO.save(person);
                persons.put(person.getId(), person);
            } catch(RawPersonProcessingException e) {
                logger.warn(e.getMessage());
            }
        }

        doc.close();

        logger.info("Records imported successfully.");

        //return "{\"status\":\"done\"}";
        return JsonPerson.fromUnsafeList(persons.values());
    }

    @RequestMapping(value = "/graph", method = RequestMethod.GET)
    public void buildGraph(HttpServletResponse httpServletResponse) throws IOException
    {
//        crimeDeduplicator.deduplicateCrimes();
        graphBuilder.buildGraph();

        httpServletResponse.sendRedirect(neo4jVisualisationUrl);
    }


    public static long getImportId() {
        Long importId = currentId.get();
        currentId.getAndIncrement();
        return importId;
    }

}