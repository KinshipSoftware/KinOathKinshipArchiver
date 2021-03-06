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
package nl.mpi.kinnate.entityindexer;

import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kintypestrings.KinTypeElement;
import nl.mpi.kinnate.kintypestrings.QueryTerm;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Created on : Mar 23, 2011, 3:32:23 PM
 *
 * @author Peter Withers
 */
public class QueryBuilder {

    public String asSequenceString(String[] stringArray) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String currentEntry : stringArray) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(",");
            } else {
                stringBuilder.append("{");
            }
            stringBuilder.append("\"");
            stringBuilder.append(escapeBadChars(currentEntry));
            stringBuilder.append("\"");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public String asContainsString(String keyWords) {
        String[] stringArray = keyWords.split("\\s");
        StringBuilder stringBuilder = new StringBuilder();
        for (String currentEntry : stringArray) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" and ");
            } else {
                stringBuilder.append("[");
            }
            stringBuilder.append("//. contains text \"");
            stringBuilder.append(escapeBadChars(currentEntry));
            stringBuilder.append("\" using fuzzy using case insensitive");
        }
        stringBuilder.append("]");
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
            stringBuilder.append(escapeBadChars(currentEntry.getXpathString()));
            stringBuilder.append("\"");
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
// todo: escape quotes from symbol queries
// todo: when getting the icon clause and the symbol the string input must be encoded for ' " &
// ref:       http://www.balisage.net/Proceedings/vol7/html/Vlist02/BalisageVol7-Vlist02.html#d38243e274

    static String escapeBadChars(String inputString) {
        // our queries use double quotes so single quotes are allowed
        // todo: could ; cause issues?
        return inputString.replace("&", "&amp;").replace("\"", "&quot;"); // .replace("'", "&apos;")
    }

    public String getLabelsClause(IndexerParameters indexParameters, String docRootVar) {
        StringBuilder stringBuilder = new StringBuilder();
        for (ParameterElement currentEntry : indexParameters.labelFields.getValues()) {
            stringBuilder.append("for $labelNode in ");
            stringBuilder.append(docRootVar);
            stringBuilder.append(currentEntry.getXpathString());
            stringBuilder.append("\nreturn\ninsert node <kin:Label xmlns:kin=\"http://mpi.nl/tla/kin\">{$labelNode/text()}</kin:Label> after $copyNode/*:Identifier,\n"); // into $copyNode
        }
        return stringBuilder.toString();
    }

    public String getDatesClause(IndexerParameters indexParameters, String docRootVar) {
        StringBuilder stringBuilder = new StringBuilder();
        for (ParameterElement currentEntry : indexParameters.dateOfBirthField.getValues()) {
            stringBuilder.append("for $dateOfBirthNode in ");
            stringBuilder.append(docRootVar);
            stringBuilder.append(currentEntry.getXpathString());
            stringBuilder.append("\nreturn\ninsert node <kin:DateOfBirth xmlns:kin=\"http://mpi.nl/tla/kin\">{$dateOfBirthNode/text()}</kin:DateOfBirth> after $copyNode/*:Identifier,\n"); // into $copyNode
        }
        for (ParameterElement currentEntry : indexParameters.dateOfDeathField.getValues()) {
            stringBuilder.append("for $dateOfDeathNode in ");
            stringBuilder.append(docRootVar);
            stringBuilder.append(currentEntry.getXpathString());
            stringBuilder.append("\nreturn\ninsert node <kin:DateOfDeath xmlns:kin=\"http://mpi.nl/tla/kin\">{$dateOfDeathNode/text()}</kin:DateOfDeath> after $copyNode/*:Identifier,\n"); // into $copyNode
        }
        return stringBuilder.toString();
    }

    public String getSymbolClause(IndexerParameters indexParameters, String docRootVar) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean notFirst = false;
        for (ParameterElement currentEntry : indexParameters.symbolFieldsFields.getValues()) {
            if (notFirst) {
                stringBuilder.append(",\n");
            }
            notFirst = true;
            String trimmedXpath = currentEntry.getXpathString().replaceFirst("^\\*:Kinnate", "");
            stringBuilder.append("if (exists(");
            stringBuilder.append(docRootVar);
            stringBuilder.append(trimmedXpath);
            stringBuilder.append(")) then ");
            stringBuilder.append("insert node <kin:Symbol xmlns:kin=\"http://mpi.nl/tla/kin\">");
            stringBuilder.append(currentEntry.getSelectedValue());
            stringBuilder.append("</kin:Symbol> after $copyNode/*:Identifier "); // into $copyNode
            stringBuilder.append("else ()");
        }
        return stringBuilder.toString();
    }

//    public String getArchiveLinksClause() {
//        return "for $corpusLink in $entityNode/*:CorpusLink\n"
//                + "return <ArchiveLink>{$corpusLink/text()}</ArchiveLink>";
//    }

    public String getRelationQuery() {
        return "<Relations>{"
                + "$entityNode/*:Relations\n"
                + "}</Relations>";
    }

//    public String getRelationQuery(String uniqueIdentifier, IndexerParameters indexParameters) {
//        String ancestorSequence = this.asSequenceString(indexParameters.ancestorFields);
//        String decendantSequence = this.asSequenceString(indexParameters.decendantFields);
//        return "<Relations>{" // todo: make the results here distinct and preferably only calculate each type once (this is currently handled in the entity data class)
//                + "for $relationNode in collection('"+databaseName+"')/*:Kinnate/*:Relation[*:UniqueIdentifier/. = \"" + uniqueIdentifier + "\"]\n"
//                + "let $isAncestor := $relationNode/*:Type/text() = " + decendantSequence + "\n" // note that the ancestor and decentant are switched for alter compared to ego
//                + "let $isDecendant := $relationNode/*:Type/text() = " + ancestorSequence + "\n"
//                + "where $isAncestor or $isDecendant \n"
//                + "return \n"
//                + "<Relation>{\n"
//                + "if ($isAncestor)\n"
//                + "then <Type>ancestor</Type>\n"
//                + "else if ($isDecendant)\n"
//                + "then <Type>descendant</Type>\n"
//                + "else <Type>none</Type>,\n"
//                + "<Identifier>{$relationNode/../(*:Gedcom|*:Entity)/*:UniqueIdentifier/(*:LocalIdentifier|*:UniqueIdentifier)/text()}</Identifier>,\n"
//                // with the type value we are looking for one of GraphDataNode.RelationType: sibling, ancestor, descendant, union, none
//                + "<Path>{base-uri($relationNode)}</Path>,\n"
//                //                + "<Label>a label</Label>,\n"
//                + "<Line>" + DataTypes.RelationLineType.sanguineLine.name() + "</Line>\n"
//                + "}</Relation>\n"
//                + "} {\n"
//                // for $relationNode in collection('"+databaseName+"')/Kinnate/(Gedcom|Relation|Entity)[UniqueIdentifier/. = "742243abdb2468b8df65f16ee562ac10"]
//                // + "for $relationNode in collection('"+databaseName+"')/*:Kinnate/(*:Gedcom|*:Entity)[*:UniqueIdentifier/. = \"" + uniqueIdentifier + "\"]\n"
//                + "for $relationNode in $entityNode/*:Relation\n"
//                + "let $isAncestor := $relationNode/*:Type/text() = " + ancestorSequence + "\n" // note that the ancestor and decentant are switched for alter compared to ego
//                + "let $isDecendant := $relationNode/*:Type/text() = " + decendantSequence + "\n"
//                + "where $isAncestor or $isDecendant \n"
//                + "return \n"
//                + "<Relation>{\n"
//                + "if ($isAncestor)\n"
//                + "then <Type>ancestor</Type>\n"
//                + "else if ($isDecendant)\n"
//                + "then <Type>descendant</Type>\n"
//                + "else <Type>none</Type>,\n"
//                + "<Identifier>{$relationNode/*:UniqueIdentifier/(*:LocalIdentifier|*:UniqueIdentifier)/text()}</Identifier>,\n" // todo: check this path to the identifier
//                // todo: add the alter unique identifier + "<UniqueIdentifier>" + uniqueIdentifier + "</UniqueIdentifier>,\n"
//                // with the type value we are looking for one of GraphDataNode.RelationType: sibling, ancestor, descendant, union, none
//                + "<Path>{base-uri(collection('"+databaseName+"')/*:Kinnate[(*:Entity|*:Gedcom)/*:UniqueIdentifier/./text() = $relationNode/*:UniqueIdentifier/(*:LocalIdentifier|*:UniqueIdentifier)/text()])}</Path>,\n"
//                //                + "<Label>a label</Label>,\n"
//                + "<Line>" + DataTypes.RelationLineType.sanguineLine.name() + "</Line>\n"
//                + "}</Relation>"
//                + "}</Relations>\n";
//    }
    public String getEntityQueryReturn(IndexerParameters indexParameters) {
        return "return copy $copyNode := $entityNode\n"
                + "modify (\n"
                // loop the label fields and add a node for any that exist
                + this.getLabelsClause(indexParameters, "root($entityNode)/")
                + this.getDatesClause(indexParameters, "root($entityNode)/")
                + this.getSymbolClause(indexParameters, "root($entityNode)/")
                // replaced the use of path by the ID get path in project method, which alows reindexing and would make the query a little faster
                //                + "insert nodes <kin:Path xmlns:kin=\"http://mpi.nl/tla/kin\">{fn:substring-after(base-uri($entityNode), '/')}</kin:Path> after $copyNode/*:Identifier\n" // when using a basex version younger than 6.62 the "after" fails re attributes: after $copyNode/*:Identifier" maybe copy is failing to keep the namespace, for earlier version the following can be used "into $copyNode"
                // todo: test if "insert after" take longer than "insert into"
                + ")\n"
                + "return $copyNode\n";
    }

    public String getEntityByEndPointQuery(DataTypes.RelationType relationType, IndexerParameters indexParameters, String databaseName) {
        // find and remove all of these: '"+databaseName+"' 
        return "<Entities> { for $doc in collection('" + databaseName + "') where not (/*:Kinnate/*:Entity/*:Relations/*:Relation/@*:Type = \"" + relationType.name() + "\")\n"
                + "and not (/*:Kinnate/*:CustomData/*:Type/text() = \"Gedcom Family Group\")\n"
                //                + "order by /*:Entity/*:Label \n" // this will not sort because the label nodes do not exist yet
                + "return let $entityNode := $doc/*:Kinnate/*:Entity\n"
                + getEntityQueryReturn(indexParameters)
                + "}</Entities>";
    }

    public String getEntityByKeyWordQuery(String keyWords, IndexerParameters indexParameters, String databaseName) {
//        String escapedKeywordSequence = asSequenceString(keyWords.split("\\s"));
        return "<Entities> { "
                + "for $KinnateNode in collection('" + databaseName + "')/*:Kinnate"
                + asContainsString(keyWords)
                + "\n"
                //                + "[//. contains text "
                //                + escapedKeywordSequence
                //                + "\""
                //                + escapeBadChars(keyWords)
                //                + "\""
                //                + " using fuzzy using case insensitive]\n"
                //                + " using case insensitive distance at most 2 words]\n"
                + "return let $entityNode := $KinnateNode/*:Entity\n"
                //                + "for $doc in collection('"+databaseName+"') where contains(string-join($doc//text()), \"" + keyWords + "\")\n"
                //                + "return let $entityNode := $doc/*:Kinnate/*:Entity\n"
                + getEntityQueryReturn(indexParameters)
                + "}</Entities>";
    }

    public String getEntityWithRelationsQuery(UniqueIdentifier uniqueIdentifier, String[] excludeUniqueIdentifiers, IndexerParameters indexParameters, String databaseName) {
        return "for $entityNode := collection('" + databaseName + "')/*:Kinnate/*:Entity[**/*:Identifier/text() = \"" + uniqueIdentifier.getQueryIdentifier() + "\"]\n" //                + getEntityQueryReturn(uniqueIdentifier, indexParameters);
                + getEntityQueryReturn(indexParameters);
//        return "for $entityNode in collection('"+databaseName+"')//*:UniqueIdentifier[. = \"" + uniqueIdentifier.getQueryIdentifier() + "\"]/ancestor::*:Kinnate\n"
//                + "where 0 = ($entityNode/(*:Entity|*:Gedcom)/*:UniqueIdentifier/. = " + asSequenceString(excludeUniqueIdentifiers) + ")\n"
//                + getEntityQueryReturn(uniqueIdentifier, indexParameters);
    }

    public String getDeleteQuery(UniqueIdentifier uniqueIdentifier, String databaseName) {
        return "for $identifierNode in collection('" + databaseName + "')/*:Kinnate[*:Entity/*:Identifier/text() = \"" + uniqueIdentifier.getQueryIdentifier() + "\"]"
                + "return db:delete('" + databaseName + "', fn:substring-after(base-uri($identifierNode), '/'))";
    }

//    public String getEntityPath(String projectName, String projectPathString, UniqueIdentifier uniqueIdentifier) {
//        // replaced the path stored in the entity by ID get path in project method, which alows reindexing and would make the query a little faster
//        return "let $identifierNode := collection('"+databaseName+"')/*:Kinnate/*:Entity[*:Identifier/text() = \"" + uniqueIdentifier.getQueryIdentifier() + "\"]\n"
//                + "return replace(replace(base-uri($identifierNode),'" + projectName + "/file:','file:'),'" + projectName + "/','file://" + projectPathString + "/')";
//    }
//    public String getEntityPaths(UniqueIdentifier[] uniqueIdentifier) {
//        StringBuilder builder = new StringBuilder();
//        for (UniqueIdentifier identifier : uniqueIdentifier) {
//            if (builder.length() == 0) {
//                builder.append("[");
//            } else {
//                builder.append(",");
//            }
//            builder.append(identifier.getQueryIdentifier());
//        }
//        builder.append("]");
//        return "for $identifierNode in collection('"+databaseName+"')/*:Kinnate/*:Entity[*:Identifier/text() = \"" + builder.toString() + "\"]\n"
//                + "return\n"
//                + "<String>base-uri($identifierNode)</String>,"
//                + "$identifierNode";
//    }
    public String getDatabaseStats(String databaseName) {
        // the use of facets here seems to depend on the version of the database, so we avoid it
//        return "let $identifierNode := index:facets('" + databaseName + "', 'flat')/document-node/element[@name contains text 'Identifier']\n"
//                + "return concat(count($identifierNode/*),':',$identifierNode/@count/string())";
        return "concat(count(collection('" + databaseName + "')),':',"
                + "count(collection('" + databaseName + "')/*:Kinnate/*:Entity/*:Relations/*:Relation))";
    }

    public String getAllFieldNamesQuery(String databaseName) {
        return "for $facetEntry in index:facets('" + databaseName + "')/document-node/element/element[@name='CustomData']/element/@name\n"
                + "order by lower-case($facetEntry)\n return data($facetEntry)";
    }

    public String getEntityQuery(UniqueIdentifier uniqueIdentifier, IndexerParameters indexParameters, String databaseName) {
        return "for $entityNode in collection('" + databaseName + "')/*:Kinnate/*:Entity[*:Identifier/text() = \"" + uniqueIdentifier.getQueryIdentifier() + "\"]\n" //                + getEntityQueryReturn(uniqueIdentifier, indexParameters);
                + getEntityQueryReturn(indexParameters);
    }

//    private String getEntityQueryReturn(UniqueIdentifier uniqueIdentifier, IndexerParameters indexParameters) {
//        return "return"
//                + "<Entity>{\n"
//                + "<Identifier>{$entityNode/(*:Entity|*:Gedcom)/*:UniqueIdentifier//text()}</Identifier>,\n"
//                // todo: check that DOB is read and that there is data to sort by
//                + "<DateOfBirth>{$entityNode/(*:Entity|*:Gedcom)/DOB}</DateOfBirth>,\n"
//                + "<Path>{base-uri($entityNode)}</Path>,\n"
//                + getSymbolClause(indexParameters, "$entityNode")
//                + "<Labels>\n"
//                // loop the label fields and add a node for any that exist
//                + this.getLabelsClause(indexParameters, "$entityNode")
//                + "</Labels>,"
//                + this.getArchiveLinksClause()
//                + "}"
//                + this.getRelationQuery(uniqueIdentifier.getQueryIdentifier(), indexParameters)
//                //                + this.getRelationQuery()
//                + "</Entity>\n";
//    }
    public String getTermQuery(KinTypeElement queryTerms, String databaseName) {
//        for $entityNode in collection('"+databaseName+"')/Kinnate[(Entity|Gedcom)]
//        where $entityNode//*="Bob /Cox/"
//        return
//        $entityNode/(Entity|Gedcom)/UniqueIdentifier/*/text()
        StringBuilder stringBuilder = new StringBuilder();
        for (QueryTerm queryTerm : queryTerms.queryTerms) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" and ");
            } else {
//                stringBuilder.append("(");
            }
            // todo: add sample diagram that demonstrates this syntax
            // todo: update the samples from =[ to [ format
            stringBuilder.append("$entityNode//*/");
            stringBuilder.append(queryTerm.fieldXPath);
            stringBuilder.append("[text() ");
            switch (queryTerm.comparatorType) {
                case Equals:
                    stringBuilder.append("=");
                    break;
                case Greater:
                    stringBuilder.append(">");
                    break;
                case Less:
                    stringBuilder.append("<");
                    break;
                case Contains:
                default:
                    stringBuilder.append("contains text");
            }
            stringBuilder.append(" \"");
            stringBuilder.append(escapeBadChars(queryTerm.searchValue));
            stringBuilder.append("\"]");
        }
//        stringBuilder.append(")");
        return "<IdentifierArray xmlns=\"http://mpi.nl/tla/kin\">{"
                + "for $entityNode in collection('" + databaseName + "')\n"
                + "where " + stringBuilder.toString() + "\n"
                + "return\n"
                + "$entityNode/*:Kinnate/*:Entity/*:Identifier\n"
                + "}</IdentifierArray>";
    }
//    public static void main(String args[]){
//        EntityCollection entityCollection = new EntityCollection();
//        EntityData queryNode = entityCollection.getEntity(new UniqueIdentifier("564953b7-3c6a-451a-9e9b-aa92d2cd9254", UniqueIdentifier.IdentifierType.tid), new IndexerParameters());
//    }
}
