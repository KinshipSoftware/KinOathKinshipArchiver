package nl.mpi.kinnate.kintypestrings;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Document : MessageStringParser
 * Created on : Apr 20, 2012, 3:33:48 PM
 * Author : Peter Withers
 */
public class MessageStringParser {

    QuerySectionParser querySectionParser;
    KinTypeElement queryElement;
    String messageString = null;
    URI importURI = null;

    public boolean foundQueryCondition() {
        return (queryElement != null); //  && queryElement.queryTerms != null && !queryElement.queryTerms.isEmpty()
    }

    public boolean foundSyntaxError() {
        return ((querySectionParser != null && !querySectionParser.foundKinType) || queryElement.queryTerms == null);
    }

    public KinTypeElement getQueryElement() {
        return queryElement;
    }

    public URI getImportURI() {
        return importURI;
    }

    public String getMessageString() {
        return messageString;
    }

    public void checkForMessages(String consumableString, ParserHighlight parserHighlight) {
        if (consumableString.startsWith("@")) {
            int initialLength = consumableString.length();
            boolean foundKinType = true;
            String errorMessage = null;
            parserHighlight = parserHighlight.addHighlight(ParserHighlight.ParserHighlightType.Message, initialLength - consumableString.length(), "A message will be shown if the required condition is met");
            consumableString = consumableString.substring(1);
            if (consumableString.startsWith("ShowMessageIfNotFound")) {
                consumableString = consumableString.substring("ShowMessageIfNotFound".length());
                querySectionParser = new QuerySectionParser(consumableString, parserHighlight, foundKinType, errorMessage);
                queryElement = new KinTypeElement();
                querySectionParser.parseQuerySection(queryElement, initialLength);

                consumableString = querySectionParser.consumableString;
                parserHighlight = querySectionParser.parserHighlight;
                foundKinType = querySectionParser.foundKinType;
                errorMessage = querySectionParser.errorMessage;
                if (!foundKinType) {
                    parserHighlight = parserHighlight.addHighlight(ParserHighlight.ParserHighlightType.Error, initialLength - consumableString.length(), errorMessage);
                } else if (queryElement.queryTerms == null) {
                    parserHighlight = parserHighlight.addHighlight(ParserHighlight.ParserHighlightType.Error, initialLength - consumableString.length(), "Incorrect syntax: a query is required eg [search term]");
                } else {
                    parserHighlight = parserHighlight.addHighlight(ParserHighlight.ParserHighlightType.Message, initialLength - consumableString.length(), "A message will be shown if the required condition is met");
                    String[] messageParts = consumableString.split("\\\"");
                    if (messageParts.length > 1) {
                        messageString = messageParts[1];
                    }
                    if (messageParts.length > 2) {
                        final String uriSection = messageParts[2];
                        try {
                            importURI = new URI(uriSection.trim());
                            if (importURI.getScheme() == null || importURI.getScheme().length() == 0) {
                                throw new URISyntaxException("", "scheme is required");
                            }
                        } catch (URISyntaxException exception) {
                            parserHighlight = parserHighlight.addHighlight(ParserHighlight.ParserHighlightType.Error, initialLength - uriSection.length(), "Incorrect URL syntax");
                        }
                    }
                    if (messageParts.length <= 1) {
                        parserHighlight = parserHighlight.addHighlight(ParserHighlight.ParserHighlightType.Error, initialLength - consumableString.length(), "Incorrect syntax: a message in quotes is required followed by and an optional URL");
                    }
                }
            } else {
                parserHighlight = parserHighlight.addHighlight(ParserHighlight.ParserHighlightType.Error, initialLength - consumableString.length(), "Incorrect syntax");
            }
        }
    }
}
