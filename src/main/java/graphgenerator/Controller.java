package graphgenerator;

import graphgenerator.graph.GraphBuilderCypher;
import graphgenerator.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.exit;

@RestController
public class Controller {

    @Autowired
    PersonService personService;

    @Autowired
    FileService fileService;

    @Autowired
    GraphBuilderCypher graphBuilder;

    @RequestMapping(value = "/graph", method = RequestMethod.GET)
    public void buildGraph(HttpServletResponse httpServletResponse) throws IOException {
        graphBuilder.buildGraph();

        httpServletResponse.sendRedirect("http://localhost:8080/");
    }
//
//    @RequestMapping(value="/person", method = RequestMethod.POST)
//    public void create(@RequestParam String name,
//                         @RequestParam String alias,
//                         @RequestParam String dob,
//                         @RequestParam String facebook,
//                         @RequestParam String associates,
//                         @RequestParam String crimes,
//                         @RequestParam String info,
//                         @RequestParam String pictureFilePath,
//                         HttpServletResponse httpServletResponse) throws IOException {
//        Person person = new Person();
//
//        person.setName(extractName(name));
//        person.setAdditionalNameInformation(extractAdditionalNameInformation(name));
//        person.setDob(dob);
//
////        person = personService.createPerson(person);
//
//        if (!alias.equals(""))
//        {
//            person.setAliases(createAliasList(person, alias));
//        }
//        if (!facebook.equals("")) {
//            person.setFacebook(createFacebookPagesList(person, facebook));
//        }
//        if (!associates.equals("")) {
//            person.setAssociations(createAssociationsList(person, name, associates));
//        }
//        if (!crimes.equals("")) {
//            person.setCrimes(createCrimes(person, crimes));
//        }
//        if (!info.equals("")) {
//            person.setInfo(info);
//        }
//
//        person.setPictureFilePath(pictureFilePath);
//
//        person = personService.updatePerson(person);
//
//        httpServletResponse.sendRedirect("http://localhost:8888/");
//    }
//
//    @RequestMapping(value="/files/{filename:.+}", method = RequestMethod.GET)
//    public ResponseEntity<Resource> serveFile(@PathVariable String filename, @RequestParam String type) throws IOException {
//
//
//        Resource file = fileService.loadAsResource(filename,type);
//        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
//                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
//    }

    private String extractName(String name)
    {
        if(name.contains("("))
        {
            return name.substring(0, name.indexOf("(")).trim();
        }
        return name;
    }

    private String extractAdditionalNameInformation(String name)
    {
        if(name.contains("("))
        {
            return name.substring(name.indexOf("(")+1, name.indexOf(")"));
        }
        return null;
    }

    private List<FacebookPage> createFacebookPagesList(Person person, String facebook)
    {
        List<FacebookPage> pages = new ArrayList<>();
        String additionalInformation = null;
        for(String page : Arrays.asList(facebook.split(",")))
        {
            additionalInformation = null;
            if(page.contains("("))
            {
                additionalInformation = page.substring(page.indexOf("(")+1, page.indexOf(")"));
                page = page.substring(0, page.indexOf("(")).trim();
            }
            pages.add(new FacebookPage(person,page,additionalInformation));
        }

        return  pages;
    }

    private List<Alias> createAliasList(Person person, String aliases)
    {
        List<Alias> al = new ArrayList<>();
        for(String alias : Arrays.asList(aliases.split(",")))
        {
            al.add(new Alias(person,alias.trim()));
        }

        return  al;
    }

    private List<Association> createAssociationsList(Person person, String name, String associates)
    {
        List<Association> associations = new ArrayList<>();
        String type;
        for(String association : Arrays.asList(associates.split(",")))
        {
            type = null;
            if(association.contains("("))
            {
                type = association.substring(association.indexOf("(")+1, association.indexOf(")"));
                association = association.substring(0, association.indexOf("(")).trim();
            }
            associations.add(new Association(person, name, association.trim(), generalizeAssociation(type)));
        }

        return  associations;
    }

    private String generalizeAssociation(String type)
    {
        if(type != null)
        {
            if(type.toLowerCase().equals("brother") || type.toLowerCase().equals("sister"))
            {
                type = "Siblings";
            }
            else if(type.toLowerCase().contains("husband") || type.toLowerCase().contains("wife"))
            {
                type = type.toLowerCase().replace("husband", "Spouse").replace("wife", "Spouses");
            }
            else if(type.toLowerCase().contains("son") || type.toLowerCase().contains("daughter") ||
                    type.toLowerCase().contains("mother") || type.toLowerCase().contains("father")) {
                type = type.toLowerCase().replace("son", "Parent - Child").replace("daughter", "Parent - Child")
                        .replace("father", "Parent - Child").replace("mother", "Parent - Child");
            }
            else if(type.toLowerCase().equals("cousin"))
            {
                type = "Cousins";
            }
            else if(type.toLowerCase().contains("uncle") || type.toLowerCase().contains("aunt") ||
                    type.toLowerCase().contains("nephew") || type.toLowerCase().contains("niece"))
            {
                type = type.toLowerCase().replace("uncle", "Uncle/Aunt - Nephew/Niece").replace("aunt", "Uncle/Aunt - Nephew/Niece")
                        .replace("nephew", "Uncle/Aunt - Nephew/Niece").replace("niece", "Uncle/Aunt - Nephew/Niece");
            }
            else
            {
                //Assuming the type is a Date type
                type = "since: " + type;
            }
        }


        return type;
    }

    private List<Crime> createCrimes(Person person, String crime)
    {
        List<Crime> crimes = new ArrayList<>();
        for(String crm : Arrays.asList(crime.split(",")))
        {
            crimes.add(new Crime(person,crm.trim()));
        }

        return  crimes;
    }
}
