package graphgenerator.graph;

import graphgenerator.configuration.PicturesConfiguration;
import graphgenerator.models.dao.PersonDAO;
import graphgenerator.models.Association;
import graphgenerator.models.Crime;
import graphgenerator.models.Person;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.*;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.neo4j.driver.v1.Values.parameters;

@Component
public class GraphBuilderCypher
{
    private static final Logger logger = LoggerFactory.getLogger("GraphBuilderCypher");


    private int numberOFConnectionsForKeyIndividuals;
    private String neo4jBoltAddress;
    private String serverPort;

    @Autowired
    PersonDAO personDAO;

    private final Session session;

    public GraphBuilderCypher(@Value("${neo4jBoltAddress}") String neo4jBoltAddress,
                              @Value("${numberOFConnectionsForKeyIndividuals}") int numberOFConnectionsForKeyIndividuals,
                              @Value("${server.port}") String serverPort)
    {
        logger.info("Initialising connection to the Neo4j graph database");

        GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector( "0" );

        this.neo4jBoltAddress = neo4jBoltAddress;
        this.numberOFConnectionsForKeyIndividuals = numberOFConnectionsForKeyIndividuals;
        this.serverPort = serverPort;

        Driver driver = GraphDatabase.driver( "bolt://"+neo4jBoltAddress, AuthTokens.basic( "neo4j", "neo4j" ) );
        session = driver.session();

        logger.info("Connection to the Neo4j graph database is now live");
    }

    public void buildGraph() {

        List<Long> crimesIDs = new ArrayList<>();

        Map<Long, Long> personIdToNodeMap = new HashMap<>();
        Map<String, List<Long>> nameToId = new HashMap<>();
        Map<Long, List<Association>> associationsMap = new HashMap<>();


        logger.info("Clearing the Neo4j graph database");

        //clearing the graph before re-inserting nodes
        session.run("MATCH (n) DETACH DELETE n");

        logger.info("Neo4j graph database cleared");

        logger.info("Inserting new nodes and edges to the Neo4j graph database");

        for (Person person : personDAO.findAll()) {

//            String image = person.getPictureFilePath().equals("") ? "http://localhost:"+serverPort+"/files/person.png?type=person" : "http://localhost:"+serverPort+"/files/"+person.getPictureFilePath()+"?type=person";
            String keyIndividual = person.getKnownAssociations().size() > numberOFConnectionsForKeyIndividuals ? "Yes" : "No";

            StatementResult personCreationResult = session.run("CREATE (a:Person " +
                            "{Id: {id}," +
                            "Name: {name}, " +
                            "Alias: {alias}," +
                            "Date_of_Birth: {dob}, " +
                            "Node_Type: {nodeType}," +
                            "Additional_Information: {info}," +
//                            "image_url: {image}," +
                            "Key_Individual: {key} }) RETURN a",
                    parameters("id", "" + person.getId() + "",
                            "name", "" + person.getOriginalName().trim() + "",
                            "alias", "" + person.getAka() + "",
                            "dob", "" + person.getOriginalDateofBirthString().trim() + "",
                            "nodeType", "Person",
                            "info", "" + person.getAdditionalInformation() != null ? person.getAdditionalInformation() : "" + "",
//                            "image", "" + image +"",
                            "key", ""+keyIndividual+""));

            long personNodeId = ((InternalNode) personCreationResult.list().get(0).asMap().get("a")).id();

            //mapping persons to their respective nodes
            personIdToNodeMap.put(person.getId(), personNodeId);

            //mapping names to the ids of the persons
            if (nameToId.get(person.getOriginalName().trim()) == null) {
                nameToId.put(person.getOriginalName().trim(), new ArrayList<>());
            }
            nameToId.get(person.getOriginalName().trim()).add(person.getId());

//
//            for (int a = 0; a < person.getAliases().size(); a++) {
//                session.run("MATCH (s) WHERE ID(s) = " + personNodeId + " \n" +
//                        "SET s.Alias" + (a + 1) + " = '" + person.getAliases().get(a).getAlias() + "' \n" +
//                        "RETURN s ");
//            }

            for (int f = 0; f < person.getFacebookDetails().size(); f++) {
                session.run("MATCH (s) WHERE ID(s) = " + personNodeId + " \n" +
                        "SET s.Facebook" + (f + 1) + " = '" + person.getFacebookDetails().get(f).getLink() + " (" + person.getFacebookDetails().get(f).getAdditionalFacebookPageInformation() + ")' \n" +
                        "RETURN s ");
            }

                for(Crime crime : person.getCrimes())
                {
                    String crimeIcon = "http://localhost:"+serverPort+"/files/" + categorizeCrime(crime.getDescription())+"?type=crime";

                    StatementResult crimeCreationResult = session.run("MATCH (s) WHERE ID(s) = " + personNodeId + " \n" +
                            "CREATE (a:Crime " +
                            "  {Crime: {crime}, " +
                            "   Node_Type: {nodeType}," +
                            "   image_url: {image} })" +
                            "CREATE(s)-[r:HAS_COMMITED]->(a)" +
                            "RETURN a",
                            parameters("id", "" + person.getId() + "",
                                    "crime", "" + crime.getDescription() + "",
                                    "nodeType", "Crime",
                                    "image", "" + crimeIcon +""));


                    long crimeNodeId = ((InternalNode) crimeCreationResult.list().get(0).asMap().get("a")).id();

                    crimesIDs.add(crimeNodeId);
                }

                for(Association association : person.getKnownAssociations())
                {
                    if(associationsMap.get(person.getId()) == null)
                    {
                        associationsMap.put(person.getId(), new ArrayList<>());
                    }
                    associationsMap.get(person.getId()).add(association);
                }
            }

            //Validating and building the association edges

            //iterating through the nodes
            for(Long personFromId : associationsMap.keySet())
            {
                //iterating through the associations registered for the node
                if(associationsMap.get(personFromId) != null && !associationsMap.get(personFromId).isEmpty())
                {
                    for(Association associationFrom : associationsMap.get(personFromId))
                    {
                        String personNameFrom = associationFrom.getPersonNameFrom().trim();
                        String personNameTo = associationFrom.getPersonNameTo().trim();

                        //iterating through the persons whose name matches the other end of the association
                        if(nameToId.get(personNameTo) != null)
                        {
                            for(Long personToId : nameToId.get(personNameTo))
                            {
                                //iterating through the associations of the person at the other end of the association

                                if(personFromId.longValue() != personToId.longValue())
                                {
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

        logger.info("The Neo4j graph database has been updated");

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
}
