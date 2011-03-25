package nl.mpi.kinnate.entityindexer;

import java.net.URI;
import nl.mpi.kinnate.kindata.GraphDataNode;

/**
 *  Document   : EntityService
 *  Created on : Mar 22, 2011, 1:09:33 PM
 *  Author     : Peter Withers
 */
public interface EntityService {

    public GraphDataNode[] getRelationsOfEgo(URI[] egoNodes, String[] uniqueIdentifiers, String[] kinTypeStrings, IndexerParameters indexParameters) throws EntityServiceException;
}
