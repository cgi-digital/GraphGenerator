package graphgenerator.graph;

import graphgenerator.PicturesConfiguration;
import graphgenerator.models.Association;
import graphgenerator.models.Crime;
import graphgenerator.models.Person;
import graphgenerator.models.PersonService;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.*;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.schema.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.neo4j.driver.v1.Values.parameters;

@Component
public class GraphBuilderCypher
{
    private int numberOFConnectionsForKeyIndividuals;
    private String neo4jBoltAddress;
    private String serverPort;

    @Autowired
    PersonService personService;

    private final Session session;

    public GraphBuilderCypher(@Value("${neo4jBoltAddress}") String neo4jBoltAddress,
                              @Value("${numberOFConnectionsForKeyIndividuals}") int numberOFConnectionsForKeyIndividuals,
                              @Value("${server.port}") String serverPort)
    {
        GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector( "0" );

        this.neo4jBoltAddress = neo4jBoltAddress;
        this.numberOFConnectionsForKeyIndividuals = numberOFConnectionsForKeyIndividuals;
        this.serverPort = serverPort;

        Driver driver = GraphDatabase.driver( "bolt://"+neo4jBoltAddress, AuthTokens.basic( "neo4j", "neo4j" ) );
        session = driver.session();
    }

    public void buildGraph() {

        List<Long> crimesIDs = new ArrayList<>();

        Map<Long, Long> personIdToNodeMap = new HashMap<>();
        Map<String, List<Long>> nameToId = new HashMap<>();
        Map<Long, List<Association>> associationsMap = new HashMap<>();

        //clearing the graph before re-inserting nodes
        session.run("MATCH (n) DETACH DELETE n");

        for (Person person : personService.findAllPersons()) {

            String image = person.getPictureFilePath().equals("") ? "http://localhost:"+serverPort+"/files/person.png?type=person" : "http://localhost:"+serverPort+"/files/"+person.getPictureFilePath()+"?type=person";
            String keyIndividual = person.getAssociations().size() > numberOFConnectionsForKeyIndividuals ? "Yes" : "No";

            StatementResult personCreationResult = session.run("CREATE (a:Person " +
                            "{Id: {id}," +
                            "Name: {name}, " +
                            "Date_of_Birth: {dob}, " +
                            "Node_Type: {nodeType}," +
                            "Additional_Information: {info}," +
                            "image_url: {image}," +
                            "Key_Individual: {key} }) RETURN a",
                    parameters("id", "" + person.getPersonId() + "",
                            "name", "" + person.getName() + "",
                            "dob", "" + person.getDob() + "",
                            "nodeType", "Person",
                            "info", "" + person.getInfo() != null ? person.getInfo() : "" + "",
                            "image", "" + image +"",
                            "key", ""+keyIndividual+""));

            long personNodeId = ((InternalNode) personCreationResult.list().get(0).asMap().get("a")).id();

            //mapping persons to their respective nodes
            personIdToNodeMap.put(person.getPersonId(), personNodeId);

            //mapping names to the ids of the persons
            if (nameToId.get(person.getName()) == null) {
                nameToId.put(person.getName(), new ArrayList<>());
            }
            nameToId.get(person.getName()).add(person.getPersonId());


            for (int a = 0; a < person.getAliases().size(); a++) {
                session.run("MATCH (s) WHERE ID(s) = " + personNodeId + " \n" +
                        "SET s.Alias" + (a + 1) + " = '" + person.getAliases().get(a).getAlias() + "' \n" +
                        "RETURN s ");
            }

            for (int f = 0; f < person.getFacebook().size(); f++) {
                session.run("MATCH (s) WHERE ID(s) = " + personNodeId + " \n" +
                        "SET s.Facebook" + (f + 1) + " = '" + person.getFacebook().get(f).getFacebookPage() + " (" + person.getFacebook().get(f).getAdditionalFacebookPageInformation() + ")' \n" +
                        "RETURN s ");
            }


                for(Crime crime : person.getCrimes())
                {
                    String crimeIcon = "http://localhost:"+serverPort+"/files/" + categorizeCrime(crime.getCrime())+"?type=crime";

                    StatementResult crimeCreationResult = session.run("MATCH (s) WHERE ID(s) = " + personNodeId + " \n" +
                            "CREATE (a:Crime " +
                            "  {Crime: {crime}, " +
                            "   Node_Type: {nodeType}," +
                            "   image_url: {image} })" +
                            "CREATE(s)-[r:HAS_COMMITED]->(a)" +
                            "RETURN a",
                            parameters("id", "" + person.getPersonId() + "",
                                    "crime", "" + crime.getCrime() + "",
                                    "nodeType", "Crime",
                                    "image", "" + crimeIcon +""));


                    long crimeNodeId = ((InternalNode) crimeCreationResult.list().get(0).asMap().get("a")).id();

                    crimesIDs.add(crimeNodeId);
                }

                for(Association association : person.getAssociations())
                {
                    if(associationsMap.get(person.getPersonId()) == null)
                    {
                        associationsMap.put(person.getPersonId(), new ArrayList<>());
                    }
                    associationsMap.get(person.getPersonId()).add(association);
                }
            }

            //Validating and building the association edges

            //iterating through the nodes
            for(Long personFromId : associationsMap.keySet())
            {
                //iterating through the associations registered for the node
                for(Association associationFrom : associationsMap.get(personFromId))
                {
                    String personNameFrom = associationFrom.getPersonNameFrom();
                    String personNameTo = associationFrom.getPersonNameTo();

                    //iterating through the persons whose name matches the other end of the association
                    if(nameToId.get(personNameTo) != null)
                    {
                        for(Long personToId : nameToId.get(personNameTo))
                        {
                            //iterating through the associations of the person at the other end of the association
                            if(associationsMap.get(personToId) != null)
                            {
                                List<Association> associationsToRemove = new ArrayList<>();
                                for(Association associationTo : associationsMap.get(personToId))
                                {
                                    if(associationTo.getPersonNameTo().equals(personNameFrom) && nameToId.get(personNameFrom).contains(personFromId))
                                    {

                                        boolean alreadyConnected = false;

                                        StatementResult alreadyConnectedCheck = session.run("MATCH(s:Person)-[r]-(t:Person) WHERE ID(s) = "+personIdToNodeMap.get(personFromId)+" AND ID(t) = "+personIdToNodeMap.get(personToId)+" RETURN r");

                                        if(!alreadyConnectedCheck.list().isEmpty())
                                        {
                                            alreadyConnected = true;
                                        }

                                        if(!alreadyConnected)
                                        {
                                            if(nameToId.get(personNameTo).size() == 1)
                                            {
                                                String associationTypeSetting = associationFrom.getType() != null ? "{ Association_Type: '"+associationFrom.getType()+"'}" : "";

                                                StatementResult associationCreationResult = session.run(
                                                        "MATCH (f:Person) WHERE ID(f) = "+personIdToNodeMap.get(personFromId)+" " +
                                                                "MATCH (t:Person) WHERE ID(t) = "+personIdToNodeMap.get(personToId)+"  " +
                                                                "CREATE(f)-[r:ASSOCIATES_WITH "+ associationTypeSetting +"]->(t) " +
                                                                "return f,t,r");

                                                //matched a single name to another single name, remove the second association
                                                if(nameToId.get(personNameFrom).size() == 1)
                                                {
                                                    associationsToRemove.add(associationTo);
                                                }
                                            }
                                            else
                                            {
                                                String associationTypeSetting = associationFrom.getType() != null ? "{ Association_Type: '"+associationFrom.getType()+"'}" : "";

                                                StatementResult associationCreationResult = session.run(
                                                        "MATCH (f:Person) WHERE ID(f) = "+personIdToNodeMap.get(personFromId)+" " +
                                                                "MATCH (t:Person) WHERE ID(t) = "+personIdToNodeMap.get(personToId)+"  " +
                                                                "CREATE(f)-[r:PROBABLY_ASSOCIATES_WITH "+ associationTypeSetting +"]->(t) " +
                                                                "return f,t,r");
                                            }
                                        }
                                    }
                                }
                                associationsMap.get(personToId).removeAll(associationsToRemove);
                            }
                        }
                    }
                }
            }



//        Map<Long,List<String>> crimeToNamesMap = new HashMap<>();
//
//
//        //detecting names referred in the crime's description
//        try(Transaction txCrimes = graphDb.beginTx() )
//        {
//            for(Long id : crimesIDs)
//            {
//                Node crimeNode = graphDb.getNodeById(id);
//                String crime = (String) crimeNode.getProperty("Crime");
//                for(String name : nameToId.keySet())
//                {
//                    if(crime.contains(name)) {
//                        if(crimeToNamesMap.get(id) == null)
//                        {
//                            crimeToNamesMap.put(id,new ArrayList<>());
//                        }
//                        crimeToNamesMap.get(id).add(name);
//                    }
//                }
//            }
//
//            List<Long> groupedCrimes = new ArrayList<>();
//
//            for(Long id : crimesIDs)
//            {
//                if(crimeToNamesMap.get(id)!= null && crimeToNamesMap.get(id).size() > 1)
//                {
//                    for(Long otherCrimeID : crimeToNamesMap.keySet())
//                    {
//                        if(id != otherCrimeID && !groupedCrimes.contains(otherCrimeID) && !groupedCrimes.contains(id))
//                        {
//                            if(compareNamesLists(crimeToNamesMap.get(id), crimeToNamesMap.get(otherCrimeID)))
//                            {
//                                Relationship rel = graphDb.getNodeById(otherCrimeID).getSingleRelationship(EdgeTypes.HAS_COMMITED,Direction.INCOMING);
//                                Node otherPersonNode = rel.getStartNode();
//                                if(nameToId.get(otherPersonNode.getProperty("Name")) != null && nameToId.get(otherPersonNode.getProperty("Name")).size() > 1)
//                                {
//                                    otherPersonNode.createRelationshipTo(graphDb.getNodeById(id), EdgeTypes.HAS_PROBABLY_COMMITED);
//                                }
//                                else
//                                {
//                                    otherPersonNode.createRelationshipTo(graphDb.getNodeById(id), EdgeTypes.HAS_COMMITED);
//                                }
//                                rel.delete();
//                                graphDb.getNodeById(otherCrimeID).delete();
//                                groupedCrimes.add(otherCrimeID);
//                            }
//                        }
//                    }
//                }
//                groupedCrimes.add(id);
//            }
//
//            txCrimes.success();
//        }

    }


    private String categorizeCrime(String crimeDescription)
    {
        if(crimeDescription.contains("theft") ||
                crimeDescription.contains("stole") ||
                crimeDescription.contains("stealing") ||
                crimeDescription.contains("quick change scam") ||
                crimeDescription.contains("flim-flam scam") ||
                crimeDescription.contains("short change"))
        {
            return PicturesConfiguration.pictures.get("theft");
        }
        else if (crimeDescription.contains("distraction scam"))
        {

        }
        else if (crimeDescription.contains("fraud"))
        {

        }
        else if(crimeDescription.contains("battery"))
        {
            return PicturesConfiguration.pictures.get("assault");
        }
        return "crime.png";
    }

    private boolean compareNamesLists(List<String> a, List<String> b)
    {
        boolean match = true;
        if(a.size() == b.size())
        {
            for(int i = 0 ; i < a.size() ; i++)
            {
                if(! a.get(i).equals(b.get(i)))
                {
                    match = false;
                    break;
                }
            }
        }
        else
        {
            match = false;
        }
        return match;
    }


    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }
}
