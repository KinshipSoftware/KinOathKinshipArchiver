package nl.mpi.kinnate.entityindexer;

import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;

/**
 *  Document   : QueryBuilder
 *  Created on : Mar 23, 2011, 3:32:23 PM
 *  Author     : Peter Withers
 */
public class QueryBuilder {

    public String asSequenceString(String[] stringArray) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String currentEntry : stringArray) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(",");
            } else {
                stringBuilder.append("(");
            }
            stringBuilder.append("\"");
            stringBuilder.append(currentEntry);
            stringBuilder.append("\"");
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public String asSequenceString(IndexerParam indexerParam) {
        StringBuilder stringBuilder = new StringBuilder();
        for (ParameterElement currentEntry : indexerParam.getValues()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(",");
            } else {
                stringBuilder.append("(");
            }
            stringBuilder.append("\"");
            stringBuilder.append(currentEntry.getXpathString());
            stringBuilder.append("\"");
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public String getLabelsClause(IndexerParameters indexParameters, String entityNodeVar) {
        StringBuilder stringBuilder = new StringBuilder();
        for (ParameterElement currentEntry : indexParameters.labelFields.getValues()) {
            String trimmedXpath = currentEntry.getXpathString().substring("*:Kinnate".length());
            stringBuilder.append("{for $labelNode in ");
            stringBuilder.append(entityNodeVar);
            stringBuilder.append(trimmedXpath);
            stringBuilder.append("\nreturn <String>{$labelNode/text()}</String>}\n");
        }
        return stringBuilder.toString();
    }

    public String getSymbolClause(IndexerParameters indexParameters, String entityNodeVar) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<Symbol>{\n");
        for (ParameterElement currentEntry : indexParameters.symbolFieldsFields.getValues()) {
            String trimmedXpath = currentEntry.getXpathString().substring("*:Kinnate".length());
            stringBuilder.append("if (exists(");
            stringBuilder.append(entityNodeVar);
            stringBuilder.append(trimmedXpath);
            stringBuilder.append(")) then \"");
            stringBuilder.append(currentEntry.getSelectedValue());
            stringBuilder.append("\"\n else ");
        }
        stringBuilder.append("()\n}</Symbol>,\n");
        return stringBuilder.toString();
    }

    public String getArchiveLinksClause() {
        return "for $corpusLink in $entityNode/*:CorpusLink\n"
                + "return <ArchiveLink>{$corpusLink/text()}</ArchiveLink>";
    }

    public String getRelationQuery() {
        return "<Relations>{"
                + "$entityNode/*:Relations\n"
                + "}</Relations>";
    }

    public String getRelationQuery(String uniqueIdentifier, IndexerParameters indexParameters) {
        String ancestorSequence = this.asSequenceString(indexParameters.ancestorFields);
        String decendantSequence = this.asSequenceString(indexParameters.decendantFields);
        return "<Relations>{" // todo: make the results here distinct and preferably only calculate each type once (this is currently handled in the entity data class)
                + "for $relationNode in collection('nl-mpi-kinnate')/*:Kinnate/*:Relation[*:UniqueIdentifier/. = \"" + uniqueIdentifier + "\"]\n"
                + "let $isAncestor := $relationNode/*:Type/text() = " + decendantSequence + "\n" // note that the ancestor and decentant are switched for alter compared to ego
                + "let $isDecendant := $relationNode/*:Type/text() = " + ancestorSequence + "\n"
                + "where $isAncestor or $isDecendant \n"
                + "return \n"
                + "<Relation>{\n"
                + "if ($isAncestor)\n"
                + "then <Type>ancestor</Type>\n"
                + "else if ($isDecendant)\n"
                + "then <Type>descendant</Type>\n"
                + "else <Type>none</Type>,\n"
                + "<Identifier>{$relationNode/../(*:Gedcom|*:Entity)/*:UniqueIdentifier/(*:LocalIdentifier|*:UniqueIdentifier)/text()}</Identifier>,\n"
                // with the type value we are looking for one of GraphDataNode.RelationType: sibling, ancestor, descendant, union, none
                + "<Path>{base-uri($relationNode)}</Path>,\n"
                //                + "<Label>a label</Label>,\n"
                + "<Line>" + DataTypes.RelationLineType.sanguineLine.name() + "</Line>\n"
                + "}</Relation>\n"
                + "} {\n"
                // for $relationNode in collection('nl-mpi-kinnate')/Kinnate/(Gedcom|Relation|Entity)[UniqueIdentifier/. = "742243abdb2468b8df65f16ee562ac10"]
                // + "for $relationNode in collection('nl-mpi-kinnate')/*:Kinnate/(*:Gedcom|*:Entity)[*:UniqueIdentifier/. = \"" + uniqueIdentifier + "\"]\n"
                + "for $relationNode in $entityNode/*:Relation\n"
                + "let $isAncestor := $relationNode/*:Type/text() = " + ancestorSequence + "\n" // note that the ancestor and decentant are switched for alter compared to ego
                + "let $isDecendant := $relationNode/*:Type/text() = " + decendantSequence + "\n"
                + "where $isAncestor or $isDecendant \n"
                + "return \n"
                + "<Relation>{\n"
                + "if ($isAncestor)\n"
                + "then <Type>ancestor</Type>\n"
                + "else if ($isDecendant)\n"
                + "then <Type>descendant</Type>\n"
                + "else <Type>none</Type>,\n"
                + "<Identifier>{$relationNode/*:UniqueIdentifier/(*:LocalIdentifier|*:UniqueIdentifier)/text()}</Identifier>,\n" // todo: check this path to the identifier
                // todo: add the alter unique identifier + "<UniqueIdentifier>" + uniqueIdentifier + "</UniqueIdentifier>,\n"
                // with the type value we are looking for one of GraphDataNode.RelationType: sibling, ancestor, descendant, union, none
                + "<Path>{base-uri(collection('nl-mpi-kinnate')/*:Kinnate[(*:Entity|*:Gedcom)/*:UniqueIdentifier/./text() = $relationNode/*:UniqueIdentifier/(*:LocalIdentifier|*:UniqueIdentifier)/text()])}</Path>,\n"
                //                + "<Label>a label</Label>,\n"
                + "<Line>" + DataTypes.RelationLineType.sanguineLine.name() + "</Line>\n"
                + "}</Relation>"
                + "}</Relations>\n";
    }

    public String getEntityWithRelationsQuery(UniqueIdentifier uniqueIdentifier, String[] excludeUniqueIdentifiers, IndexerParameters indexParameters) {
        return "for $entityNode in collection('nl-mpi-kinnate')//*:UniqueIdentifier[. = \"" + uniqueIdentifier.getQueryIdentifier() + "\"]/ancestor::*:Kinnate\n"
                + "where 0 = ($entityNode/(*:Entity|*:Gedcom)/*:UniqueIdentifier/. = " + asSequenceString(excludeUniqueIdentifiers) + ")\n"
                + getEntityQueryReturn(uniqueIdentifier, indexParameters);
    }

    public String getEntityQuery(UniqueIdentifier uniqueIdentifier, IndexerParameters indexParameters) {
        return "let $entityNode := collection('nl-mpi-kinnate')/*:Kinnate[(*:Entity|*:Gedcom)/*:UniqueIdentifier/. = \"" + uniqueIdentifier.getQueryIdentifier() + "\"]\n"
                + getEntityQueryReturn(uniqueIdentifier, indexParameters);
    }

    private String getEntityQueryReturn(UniqueIdentifier uniqueIdentifier, IndexerParameters indexParameters) {
        return "return"
                + "<Entity>{\n"
                + "<Identifier>{$entityNode/(*:Entity|*:Gedcom)/*:UniqueIdentifier//text()}</Identifier>,\n"
                // todo: check that DOB is read and that there is data to sort by
                + "<DateOfBirth>{$entityNode/(*:Entity|*:Gedcom)/DOB}</DateOfBirth>,\n"
                + "<Path>{base-uri($entityNode)}</Path>,\n"
                + getSymbolClause(indexParameters, "$entityNode")
                + "<Labels>\n"
                // loop the label fields and add a node for any that exist
                + this.getLabelsClause(indexParameters, "$entityNode")
                + "</Labels>,"
                + this.getArchiveLinksClause()
                + "}"
                + this.getRelationQuery(uniqueIdentifier.getQueryIdentifier(), indexParameters)
                //                + this.getRelationQuery()
                + "</Entity>\n";
    }

    public String getTermQuery(KinTypeStringConverter.KinTypeElement queryTerms) {
//        for $entityNode in collection('nl-mpi-kinnate')/Kinnate[(Entity|Gedcom)]
//        where $entityNode//*="Bob /Cox/"
//        return
//        $entityNode/(Entity|Gedcom)/UniqueIdentifier/*/text()
        StringBuilder stringBuilder = new StringBuilder();
        for (String[] term : queryTerms.queryTerm) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" and ");
            } else {
//                stringBuilder.append("(");
            }
            stringBuilder.append("$entityNode//");
            stringBuilder.append(term[0]);
            stringBuilder.append("[text() contains text \"");
            stringBuilder.append(term[1]);
            stringBuilder.append("\"]");
        }
//        stringBuilder.append(")");
        return "<IdentifierArray xmlns=\"http://mpi.nl/tla/kin\">{"
                + "for $entityNode in collection('nl-mpi-kinnate')/*:Kinnate\n"
                + "where " + stringBuilder.toString() + "\n"
                + "return\n"
                + "$entityNode/*:Entity/*:Identifier\n"
                + "}</IdentifierArray>";
    }
}
