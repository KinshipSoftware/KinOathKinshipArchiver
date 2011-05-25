package nl.mpi.kinnate.entityindexer;

import java.net.URI;
import java.util.HashSet;
import nl.mpi.kinnate.entityindexer.QueryParser.ParserHighlight;
import nl.mpi.kinnate.kindata.EntityData;

/**
 *  Document   : EntityService
 *  Created on : Mar 22, 2011, 1:09:33 PM
 *  Author     : Peter Withers
 */
public interface EntityService {

//    public void primeWithEntities(EntityData[] preLoadedEntities); // it would seem not to be a good idea to try and use existing entities from an svg file when their relations might not exist, so we will allow the existing entities to be used on the graph but not for database actions

    public EntityData[] getRelationsOfEgo(URI[] egoNodes, HashSet<String> egoIdentifiers, HashSet<String> requiredEntityIdentifiers, String[] kinTypeStrings, ParserHighlight[] parserHighlight, IndexerParameters indexParameters) throws EntityServiceException;
}
