package nl.mpi.kinnate.entityindexer;

import java.util.ArrayList;

/**
 *  Document   : QueryParser
 *  Created on : Mar 31, 2011, 2:38:20 PM
 *  Author     : Peter Withers
 */
public class QueryParser {

    public String[][] getQueryStrings(String kinTypeString) {
        String kinType = null;
        ArrayList<String[]> queryTerms = new ArrayList<String[]>();
//        String[] queryParts = kinTypeString.split("(=\\[)|([\\]])");
        String[] queryParts = kinTypeString.split("[\\]]");
//        String[] queryParts = kinTypeString.split("\n");
        for (String querySection : queryParts) {
            String[] subParts = querySection.split("=\\[");
            if (subParts.length == 2) {
                String queryText = subParts[1];
                kinType = subParts[0];
                //queryText = queryText.split("\\]")[0];
                if (!queryText.contains("=")) {
                    if (queryText.length() > 2) {
                        queryTerms.add(new String[]{"*", queryText});
                    }
                } else {
                    String[] queryTerm = queryText.split("=");
                    if (queryTerm.length == 2) {
                        if (queryTerm[0].length() > 2 && queryTerm[1].length() > 2) {
                            queryTerms.add(new String[]{queryTerm[0], queryTerm[1]});
                        }
                    }
                }
            }
        }
        return queryTerms.toArray(new String[][]{});
    }
}
