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

    public String getLabelsClause(IndexerParameters indexParameters, String entityNodeVar) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String[] currentEntry : indexParameters.labelFields.getValues()) {
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

    public String getSymbolClause(IndexerParameters indexParameters, String entityNodeVar) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<Symbol>{\n");
        for (String[] currentEntry : indexParameters.symbolFieldsFields.getValues()) {
            String trimmedXpath = currentEntry[0].substring("Kinnate".length());
            stringBuilder.append("if (exists(");
            stringBuilder.append(entityNodeVar);
            stringBuilder.append(trimmedXpath);
            stringBuilder.append(")) then \"");
            stringBuilder.append(currentEntry[1]);
            stringBuilder.append("\"\n else ");
        }
        stringBuilder.append("()\n}</Symbol>,\n");
        return stringBuilder.toString();
    }

    public String getRelationQuery(String uniqueIdentifier, IndexerParameters indexParameters) {
        String ancestorSequence = this.asSequenceString(indexParameters.ancestorFields);
        String decendantSequence = this.asSequenceString(indexParameters.decendantFields);
        return "<Relations>{" // todo: make the results here distinct and preferably only calculate each type once (this is currently handled in the entity data class)
                + "for $relationNode in collection('nl-mpi-kinnate')/Kinnate/Relation[UniqueIdentifier/. = \"" + uniqueIdentifier + "\"]\n"
                + "let $isAncestor := $relationNode/Type/text() = " + decendantSequence + "\n" // note that the ancestor and decentant are switched for alter compared to ego
                + "let $isDecendant := $relationNode/Type/text() = " + ancestorSequence + "\n"
                + "where $isAncestor or $isDecendant \n"
                + "return \n"
                + "<Relation>{\n"
                + "if ($isAncestor)\n"
                + "then <Type>ancestor</Type>\n"
                + "else if ($isDecendant)\n"
                + "then <Type>descendant</Type>\n"
                + "else <Type>none</Type>,\n"
                + "<Identifier>{$relationNode/../(Gedcom|Entity)/UniqueIdentifier/(LocalIdentifier|UniqueIdentifier)/text()}</Identifier>,\n"
                // with the type value we are looking for one of GraphDataNode.RelationType: sibling, ancestor, descendant, union, none
                + "<Path>{base-uri($relationNode)}</Path>,\n"
                //                + "<Label>a label</Label>,\n"
                + "<Line>square</Line>\n"
                + "}</Relation>\n"
                + "} {\n"
                // for $relationNode in collection('nl-mpi-kinnate')/Kinnate/(Gedcom|Relation|Entity)[UniqueIdentifier/. = "742243abdb2468b8df65f16ee562ac10"]
                + "for $relationNode in collection('nl-mpi-kinnate')/Kinnate/(Gedcom|Entity)[UniqueIdentifier/. = \"" + uniqueIdentifier + "\"]\n"
                // todo: this second relation clause is not getting triggered, is this due to the data I am testing or due to an error
                + "let $isAncestor := $relationNode/Type/text() = " + ancestorSequence + "\n" // note that the ancestor and decentant are switched for alter compared to ego
                + "let $isDecendant := $relationNode/Type/text() = " + decendantSequence + "\n"
                + "where $isAncestor or $isDecendant \n"
                + "return \n"
                + "<Relation>{\n"
                + "if ($isAncestor)\n"
                + "then <Type>ancestor</Type>\n"
                + "else if ($isDecendant)\n"
                + "then <Type>descendant</Type>\n"
                + "else <Type>none</Type>,\n"
                + "<Identifier>{$relationNode/../Relation/UniqueIdentifier/(LocalIdentifier|UniqueIdentifier)/text()}</Identifier>,\n" // todo: check this path to the identifier
                // todo: add the alter unique identifier + "<UniqueIdentifier>" + uniqueIdentifier + "</UniqueIdentifier>,\n"
                // with the type value we are looking for one of GraphDataNode.RelationType: sibling, ancestor, descendant, union, none
                + "<Path>{base-uri($relationNode)}</Path>,\n"
                //                + "<Label>a label</Label>,\n"
                + "<Line>square</Line>\n"
                + "}</Relation>"
                + "}</Relations>\n";
    }

    public String getEntityQuery(String uniqueIdentifier, IndexerParameters indexParameters) {
        return "let $entityNode := collection('nl-mpi-kinnate')/Kinnate[(Entity|Gedcom)/UniqueIdentifier/. = \"" + uniqueIdentifier + "\"]\n"
                + "return"
                + "<Entity>{\n"
                + "<Identifier>" + uniqueIdentifier + "</Identifier>,\n"
                + "<Path>{base-uri($entityNode)}</Path>,\n"
                + getSymbolClause(indexParameters, "$entityNode")
                + "<Labels>\n"
                // loop the label fields and add a node for any that exist
                + this.getLabelsClause(indexParameters, "$entityNode")
                + "</Labels>"
                + "}"
                + this.getRelationQuery(uniqueIdentifier, indexParameters)
                + "</Entity>\n";
    }

    public String getTermQuery(String[] queryTerms) {
//        for $entityNode in collection('nl-mpi-kinnate')/Kinnate[(Entity|Gedcom)]
//        where $entityNode//*="Bob /Cox/"
//        return
//        $entityNode/(Entity|Gedcom)/UniqueIdentifier/*/text()
        StringBuilder stringBuilder = new StringBuilder();
        for (String term : queryTerms) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" and ");
            } else {
//                stringBuilder.append("(");
            }
            stringBuilder.append("$entityNode");
            stringBuilder.append(term);
//            stringBuilder.append("\"");
        }
//        stringBuilder.append(")");
        return "for $entityNode in collection('nl-mpi-kinnate')/Kinnate[(Entity|Gedcom)]\n"
                + "where " + stringBuilder.toString() + "\n"
                + "return\n"
                + "$entityNode/(Entity|Gedcom)/UniqueIdentifier/*/text()\n";
    }
}
