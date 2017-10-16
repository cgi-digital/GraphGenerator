# GraphGenerator

Custom project that integrates input data into a specified RDBMS and then build a graph representation of the data using Neo4j and the Neo4j browser

How to build and run the project

1. Clone the project on to your machine and build it using maven
2. Copy application.properties to your preferable location
3. Edit application.properties and specify the following

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

4. Run the built jar file making it use your edited properties file, by typing the follow in your terminal
**java -jar GraphGenerator-0.1.0.jar --spring.config.location="your_prefered_path/application.propeties"**
5. Clone neo4j-browser, follow the yarn build instructions.
6. Copy **init.coffee** to this location, replacing the original.
**/neo4j-browser/src/browser/modules/D3Visualization/lib/visualization/renders/init.coffee**
7. Copy **graphStyle.js** to this location, replacing the original.
**/neo4j-browser/src/browser/modules/D3Visualization/**
8. Run Neo4j Browser by running "**yarn start**"
9. When prompted for connection details for a Neo4j bolt use the details you specified in you application.properties and use neo4j/neo4j as the default username/password combination