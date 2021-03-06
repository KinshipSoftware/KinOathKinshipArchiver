/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.kintypestrings;

import java.util.ArrayList;

/**
 * Document : QuerySectionParser
 * Created on : Apr 20, 2012, 12:03:34 PM
 * Author : Peter Withers
 */
public class QuerySectionParser {

    String consumableString;
    ParserHighlight parserHighlight;
    boolean foundKinType;
    String errorMessage;

    public QuerySectionParser(String consumableString, ParserHighlight parserHighlight, boolean foundKinType, String errorMessage) {
        this.consumableString = consumableString;
        this.parserHighlight = parserHighlight;
        this.foundKinType = foundKinType;
        this.errorMessage = errorMessage;
    }

    protected String parseQuerySection(KinTypeElement currentElement, int initialLength) {
        if (consumableString.startsWith("[")) {
            // todo: Ticket #1087 Multiple query terms should be possible in the kin type string queries. Eg: Ef=[Kundarr]PM=[Louise] (Based on Joe's data).
            int highlightPosition = initialLength - consumableString.length();
            String highlightMessage = "Query: ";
//                        consumableString = consumableString.substring("=".length());
            while (consumableString.startsWith("[")) {
                // todo: allow multiple terms such as "=[foo][bar]" or "=[foo][bar][NAME=Bob]"
                int queryStart = "[".length();
                int queryEnd = consumableString.indexOf("]");
                if (queryEnd == -1) {
                    // if the terms are incomplete then ignore the rest of the line
                    highlightMessage += "No closing bracket ']' found";
                    errorMessage = highlightMessage;
                    foundKinType = false;
                    break;
                }
                if (queryEnd - queryStart < 3) {
                    // the query string must be more than 2 chars
                    highlightMessage += "Query must be over 2 chars long";
                    errorMessage = highlightMessage;
                    foundKinType = false;
                    break;
                }
                if (currentElement.queryTerms == null) {
                    currentElement.queryTerms = new ArrayList<QueryTerm>();
                }
                String queryText = consumableString.substring(queryStart, queryEnd);
                consumableString = consumableString.substring(queryEnd + 1);
                String[] queryTerm;
                KinTypeStringConverter.QueryType currentQueryType = null;
                queryTerm = queryText.split("=="); // detect which comparitor is used
                if (queryTerm.length > 1) {
                    currentQueryType = KinTypeStringConverter.QueryType.Equals;
                } else {
                    queryTerm = queryText.split("=");
                    if (queryTerm.length > 1) {
                        currentQueryType = KinTypeStringConverter.QueryType.Contains;
                    } else {
                        queryTerm = queryText.split("\\>");
                        if (queryTerm.length > 1) {
                            currentQueryType = KinTypeStringConverter.QueryType.Greater;
                        } else {
                            queryTerm = queryText.split("\\<");
                            if (queryTerm.length > 1) {
                                currentQueryType = KinTypeStringConverter.QueryType.Less;
                            }
                        }
                    }
                }

                if (currentQueryType == null) {
                    currentElement.queryTerms.add(new QueryTerm("*", KinTypeStringConverter.QueryType.Contains, queryText));
                    highlightMessage += "Any field containing '" + queryText + "'";
                } else {
                    if (queryTerm[0].length() > 0 && queryTerm[1].length() > 0) {
                        // namespace wild cards *:* are inserted here so that the user does not need to specify the namespace
                        currentElement.queryTerms.add(new QueryTerm("*:" + queryTerm[0].replaceAll("\\.", "/*:"), currentQueryType, queryTerm[1]));
                        highlightMessage += "Only the field '" + queryTerm[0] + "' containing '" + queryTerm[1] + "'";
                    }
                }
            }
            // todo: this causes issues by concatonating messages eg query and error messages, should remove boolean foundKinType, String errorMessage from this class
            parserHighlight = parserHighlight.addHighlight(ParserHighlight.ParserHighlightType.Query, highlightPosition, highlightMessage);
        }
        return consumableString;
    }
}
