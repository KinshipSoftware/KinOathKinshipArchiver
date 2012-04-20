package nl.mpi.kinnate.kintypestrings;

/**
 * Document : MessageStringParser
 * Created on : Apr 20, 2012, 3:33:48 PM
 * Author : Peter Withers
 */
public class MessageStringParser {

    QuerySectionParser querySectionParser;
    KinTypeElement queryElement;

    public boolean foundQueryCondition() {
        return (queryElement != null); //  && queryElement.queryTerms != null && !queryElement.queryTerms.isEmpty()
    }

    public boolean foundSyntaxError() {
        return (querySectionParser != null && !querySectionParser.foundKinType);
    }

    public KinTypeElement getQueryElement() {
        return queryElement;
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
                } else {
                    parserHighlight = parserHighlight.addHighlight(ParserHighlight.ParserHighlightType.Message, initialLength - consumableString.length(), "A message will be shown if the required condition is met");
                }
            } else {
                parserHighlight = parserHighlight.addHighlight(ParserHighlight.ParserHighlightType.Error, initialLength - consumableString.length(), "Incorrect syntax");
            }
        }
    }
}
