package graphgenerator.graph;

import org.neo4j.graphdb.RelationshipType;

public enum EdgeTypes implements RelationshipType
{
    ASSOCIATES_WITH,
    PROBABLY_ASSOCIATES_WITH,
    HAS_COMMITED,
    HAS_PROBABLY_COMMITED,
    HAS
}
