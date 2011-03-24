package nl.mpi.kinnate.entityindexer;

import nl.mpi.kinnate.entityindexer.IndexerParameters.IndexerParam;

/**
 *  Document   : QueryBuilder
 *  Created on : Mar 23, 2011, 3:32:23 PM
 *  Author     : Peter Withers
 */
public class QueryBuilder {

    public String asSequenceString(IndexerParam indexerParam) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String[] currentEntry : indexerParam.getValues()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(",");
            } else {
                stringBuilder.append("(");
            }
            stringBuilder.append("\"");
            stringBuilder.append(currentEntry[0]);
            stringBuilder.append("\"");
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public String asIfExistsString(IndexerParam indexerParam, String entityNodeVar) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String[] currentEntry : indexerParam.getValues()) {
            String trimmedXpath = currentEntry[0].substring("Kinnate".length());
            stringBuilder.append("{if (exists(");
            stringBuilder.append(entityNodeVar);
            stringBuilder.append(trimmedXpath);
            stringBuilder.append(")) then ");
            stringBuilder.append("<String>{");
            stringBuilder.append(entityNodeVar);
            stringBuilder.append(trimmedXpath);
            stringBuilder.append("/text()}</String>else()}\n");
        }
        return stringBuilder.toString();
    }
}
