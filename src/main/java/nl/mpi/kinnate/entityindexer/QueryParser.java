package nl.mpi.kinnate.entityindexer;

/**
 *  Document   : QueryParser
 *  Created on : Mar 31, 2011, 2:38:20 PM
 *  Author     : Peter Withers
 */
public class QueryParser {

    public String[] getQueryStrings(String kinTypeString) {
        //String[] queryParts = kinTypeString.split("=\\[");
        String[] queryParts = kinTypeString.split(",");
        return queryParts;
    }
}
