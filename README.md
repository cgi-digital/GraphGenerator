# GraphGenerator

Custom project that integrates input data into a specified RDBMS and then builds a graph representation of the data using Neo4j and the Neo4j browser

## How to Build and Run the Project

1. Clone the project to your local machine using git or use gits download zip feature to get your own local copy.

2. Go in to the `GraphGenreator` directory and copy the `application.properties` from `src/main/java/graphgenerator/resources/` to a good location.

3. Ensure you have a local SQL database setup (PostgreSQL is recommended and tested although the project should work with any Hibernate combatible database).  You'll need to know the connection details for the database including the server address, port, username and password.  GraphGenerator is set up to use a postgrsql database out of the box running on jdbc:postgresql://127.0.0.1:5432/ with a username of `graphgen` and passowrd of `demo234`.  You'll also need to create an empty database on the server manually called `nodes_rdbms_store`.

3. Open the application.properties file using your favourite text editor.  Out of the box the GraphGenerator project is configured to use a series of directories in `/tmp/GraphGenerator/` and to communicate on a set of default ports.  The following options are ones you may wish to update for your particular setup:

Database configuration: Configure your RDBMS details

`spring.datasource.url = 
`

`spring.datasource.username = 
`

`spring.datasource.password = 
`

`spring.datasource.initialize=true
`

The port on which the server will run

`server.port=
`

The path in which the server will build the graph database for Neo4j
dbPath = 
The url on which the server will expose its' Neo4j bolt

`neo4jBoltAddress = localhost:7688
`

The number of edges required for a node to be considered a key node
`numberOFConnectionsForKeyIndividuals = 5
`

The directory containing the pictures to be displayed inside the nodes

`picturesDirectory =`

Default pictures for each node category

`pictures.theft = thief.png
pictures.assault = knife.png`

4. Build the GraphGeneraotr jar using maven:

`mvn clean && mvn install`

5. Run the built jar file making it use your edited properties file, by typing the follow in your terminal
**java -jar GraphGenerator-0.1.0.jar --spring.config.location="your_prefered_path/application.propeties"**

6. Download and install neo4j from the neo4j web site and install it.  Neo4j is available for Mac OS X, Windows and Linux.

7. Configure an empty neo4j database to use the directory specified in your application.properties file that you set up earlier.

8. Run neo4j as a server.  Make sure the neo4j ports for bolt and other neo4j services are the same as configured in your application.properties file.

9. Direct your web browser to 127.0.0.1:<server.port> (The server port is the one configure in the application properties, the default is 8888).  This will present you with a data import wizard which will walk you through uploading a demo PDF of data (Not included with this project please contact D. Sykes) and then take you on to Neo4j so the data can viewed and manipulated.
