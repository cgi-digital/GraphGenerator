//package graphgenerator.graph;
//
//import graphgenerator.PicturesConfiguration;
//import graphgenerator.models.*;
//import org.neo4j.graphdb.*;
//import org.neo4j.graphdb.factory.GraphDatabaseFactory;
//import org.neo4j.graphdb.factory.GraphDatabaseSettings;
//import org.neo4j.graphdb.schema.Schema;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class GraphBuilder
//{
//    private String dbPath;
//    private int numberOFConnectionsForKeyIndividuals;
//    private String neo4jBoltAddress;
//    private String serverPort;
//
//    @Autowired
//    PersonService personService;
//
//    private final GraphDatabaseService graphDb;
//
//    public GraphBuilder(@Value("${dbPath}") String dbPath,
//                        @Value("${neo4jBoltAddress}") String neo4jBoltAddress,
//                        @Value("${numberOFConnectionsForKeyIndividuals}") int numberOFConnectionsForKeyIndividuals,
//                        @Value("${server.port}") String serverPort)
//    {
//        GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector( "0" );
//
//        this.dbPath = dbPath;
//        this.neo4jBoltAddress = neo4jBoltAddress;
//        this.numberOFConnectionsForKeyIndividuals = numberOFConnectionsForKeyIndividuals;
//        this.serverPort = serverPort;
//
//        graphDb = new GraphDatabaseFactory()
//                .newEmbeddedDatabaseBuilder( new File(dbPath) )
//                .setConfig( bolt.type, "BOLT" )
//                .setConfig( bolt.enabled, "true" )
//                .setConfig( bolt.address, neo4jBoltAddress )
//                .newGraphDatabase();
//
//        registerShutdownHook( graphDb );
//    }
//
//    public void buildGraph()
//    {
//
//        List<Long> crimesIDs = new ArrayList<>();
//
//        Map<Long,Long> personIdToNodeMap = new HashMap<>();
//        Map<String,List<Long>> nameToId = new HashMap<>();
//        Map<Long,List<Association>> associationsMap = new HashMap<>();
//
//        //clearing the graph before re-inserting nodes
//        try ( Transaction tx = graphDb.beginTx() )
//        {
//            graphDb.execute("MATCH (n) DETACH DELETE n");
//
//            tx.success();
//        }
//
//        try ( Transaction tx = graphDb.beginTx() )
//        {
//            Schema schema = graphDb.schema();
//
//            for(Person person : personService.findAllPersons())
//            {
//                Node personNode = graphDb.createNode(new Label() {
//                    @Override
//                    public String name() {
//                        return "Person";
//                    }
//                });
//
//                //mapping persons to their respective nodes
//                personIdToNodeMap.put(person.getPersonId(),personNode.getId());
//
//                //mapping names to the ids of the persons
//                if(nameToId.get(person.getName()) == null)
//                {
//                    nameToId.put(person.getName(), new ArrayList<>());
//                }
//                nameToId.get(person.getName()).add(person.getPersonId());
//
//                personNode.setProperty("Name", person.getName());
//
//                for(int a = 0 ; a < person.getAliases().size() ; a++)
//                {
//                    personNode.setProperty("Alias " + a+1, person.getAliases().get(a).getAlias());
//                }
//
//                personNode.setProperty("Additional Name Information", person.getAdditionalNameInformation() != null ? person.getAdditionalNameInformation() : "");
//                personNode.setProperty("Id", person.getPersonId());
//                personNode.setProperty("Date of Birth", person.getDob() != null ? person.getDob() : "");
//
//                for(int f = 0 ; f < person.getFacebook().size() ; f++)
//                {
//                    personNode.setProperty("Facebook " + f+1, person.getFacebook().get(f).getFacebookPage() + " (" + person.getFacebook().get(f).getAdditionalFacebookPageInformation() + ")");
//                }
//
//                personNode.setProperty("Additional Information", person.getInfo() != null ? person.getInfo() : "");
//                personNode.setProperty("image_url",person.getPictureFilePath().equals("") ? "http://localhost:"+serverPort+"/files/person.png?type=person" : "http://localhost:"+serverPort+"/files/"+person.getPictureFilePath()+"?type=person");
//                personNode.setProperty("Node Type", "Person");
//
//                if(person.getAssociations().size() > numberOFConnectionsForKeyIndividuals)
//                {
//                    personNode.setProperty("Key_Individual","Yes");
//                }
//
//
////                for(Alias alias : person.getAliases())
////                {
////
////                    Node aliasNode = graphDb.createNode(new Label() {
////                        @Override
////                        public String name() {
////                                return "Alias";
////                            }
////                    });
////                    aliasNode.setProperty("Alias", alias.getAlias());
////                    aliasNode.setProperty("Node Type", "Alias");
////
////                    personNode.createRelationshipTo( aliasNode, EdgeTypes.HAS );
////                }
//
////                for(FacebookPage page : person.getFacebook())
////                {
////                    Node facebookNode = graphDb.createNode(new Label() {
////                        @Override
////                        public String name() {
////                            return "Facebook";
////                        }
////                    });
////                    facebookNode.setProperty("Facebook", page.getFacebookPage());
////                    facebookNode.setProperty("Additional Information", page.getAdditionalFacebookPageInformation() != null ? page.getAdditionalFacebookPageInformation() : "");
////                    facebookNode.setProperty("image_url","http://localhost:8888/images/fb.png");
////                    facebookNode.setProperty("Node Type", "Facebook Page");
////
////                    personNode.createRelationshipTo( facebookNode, EdgeTypes.HAS );
////                }
//
//                for(Crime crime : person.getCrimes())
//                {
//                    Node crimeNode = graphDb.createNode(new Label() {
//                        @Override
//                        public String name() {
//                            return "Crime";
//                        }
//                    });
//
//                    crimeNode.setProperty("Crime", crime.getCrime());
//                    //TODO elaborate more on the crime with additional properties
//                    crimeNode.setProperty("Node Type", "Crime");
//                    personNode.createRelationshipTo( crimeNode, EdgeTypes.HAS_COMMITED );
//                    crimeNode.setProperty("image_url","http://localhost:"+serverPort+"/files/" + categorizeCrime(crime.getCrime())+"?type=crime");
//                    crimesIDs.add(crimeNode.getId());
//                }
//
//                for(Association association : person.getAssociations())
//                {
//                    if(associationsMap.get(person.getPersonId()) == null)
//                    {
//                        associationsMap.put(person.getPersonId(), new ArrayList<>());
//                    }
//                    associationsMap.get(person.getPersonId()).add(association);
//                }
//            }
//
//            //Validating and building the association edges
//
//            //iterating through the nodes
//            for(Long personFromId : associationsMap.keySet())
//            {
//                //iterating through the associations registered for the node
//                for(Association associationFrom : associationsMap.get(personFromId))
//                {
//                    String personNameFrom = associationFrom.getPersonNameFrom();
//                    String personNameTo = associationFrom.getPersonNameTo();
//
//                    //iterating through the persons whose name matches the other end of the association
//                    if(nameToId.get(personNameTo) != null)
//                    {
//                        for(Long personToId : nameToId.get(personNameTo))
//                        {
//                            //iterating through the associations of the person at the other end of the association
//                            if(associationsMap.get(personToId) != null)
//                            {
//                                List<Association> associationsToRemove = new ArrayList<>();
//                                for(Association associationTo : associationsMap.get(personToId))
//                                {
//                                    //Additional checks
//                                    if(associationTo.getPersonNameTo().equals(personNameFrom) && nameToId.get(personNameFrom).contains(personFromId))
//                                    {
//                                        boolean alreadyConnected = false;
//                                        for(Relationship rel : graphDb.getNodeById(personIdToNodeMap.get(personFromId)).getRelationships())
//                                        {
//                                            if( ( rel.getOtherNode(graphDb.getNodeById(personIdToNodeMap.get(personFromId))).
//                                                    equals(graphDb.getNodeById(personIdToNodeMap.get(personToId))) ) )
//                                            {
//                                                alreadyConnected = true;
//                                                break;
//                                            }
//                                        }
//
//                                        if(!alreadyConnected)
//                                        {
//                                            if(nameToId.get(personNameTo).size() == 1)
//                                            {
//                                                Relationship relationship = graphDb.getNodeById(personIdToNodeMap.get(personFromId))
//                                                        .createRelationshipTo( graphDb.getNodeById(personIdToNodeMap.get(personToId)), EdgeTypes.ASSOCIATES_WITH );
//                                                if(associationFrom.getType() != null)
//                                                {
//                                                    relationship.setProperty("Association Type",associationFrom.getType());
//                                                }
//                                                //matched a single name to another single name, remove the second association
//                                                if(nameToId.get(personNameFrom).size() == 1)
//                                                {
//                                                    associationsToRemove.add(associationTo);
//                                                }
//                                                break;
//                                            }
//                                            else
//                                            {
//                                                Relationship relationship = graphDb.getNodeById(personIdToNodeMap.get(personFromId))
//                                                        .createRelationshipTo( graphDb.getNodeById(personIdToNodeMap.get(personToId)), EdgeTypes.PROBABLY_ASSOCIATES_WITH );
//                                                if(associationFrom.getType() != null)
//                                                {
//                                                    relationship.setProperty("Association Type",associationFrom.getType());
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                                associationsMap.get(personToId).removeAll(associationsToRemove);
//                            }
//                        }
//                    }
//                }
//            }
//
//            tx.success();
//        }
//
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
//    }
//
//    private String categorizeCrime(String crimeDescription)
//    {
//        if(crimeDescription.contains("theft") ||
//                crimeDescription.contains("stole") ||
//                crimeDescription.contains("stealing") ||
//                crimeDescription.contains("quick change scam") ||
//                crimeDescription.contains("flim-flam scam") ||
//                crimeDescription.contains("short change"))
//        {
//            return PicturesConfiguration.pictures.get("theft");
//        }
//        else if (crimeDescription.contains("distraction scam"))
//        {
//
//        }
//        else if (crimeDescription.contains("fraud"))
//        {
//
//        }
//        else if(crimeDescription.contains("battery"))
//        {
//            return PicturesConfiguration.pictures.get("assault");
//        }
//        return "crime.png";
//    }
//
//    private boolean compareNamesLists(List<String> a, List<String> b)
//    {
//        boolean match = true;
//        if(a.size() == b.size())
//        {
//            for(int i = 0 ; i < a.size() ; i++)
//            {
//                if(! a.get(i).equals(b.get(i)))
//                {
//                    match = false;
//                    break;
//                }
//            }
//        }
//        else
//        {
//            match = false;
//        }
//        return match;
//    }
//
//
//    private static void registerShutdownHook( final GraphDatabaseService graphDb )
//    {
//        // Registers a shutdown hook for the Neo4j instance so that it
//        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
//        // running application).
//        Runtime.getRuntime().addShutdownHook( new Thread()
//        {
//            @Override
//            public void run()
//            {
//                graphDb.shutdown();
//            }
//        } );
//    }
//}
