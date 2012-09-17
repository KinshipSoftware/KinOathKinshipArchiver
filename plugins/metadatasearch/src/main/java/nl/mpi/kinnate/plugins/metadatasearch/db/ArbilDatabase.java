package nl.mpi.kinnate.plugins.metadatasearch.db;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.arbil.plugin.PluginBugCatcher;
import nl.mpi.arbil.plugin.PluginDialogHandler;
import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.arbil.plugin.PluginSessionStorage;
import nl.mpi.kinnate.entityindexer.QueryException;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;

/**
 * Document : ArbilDatabase Created on : Aug 6, 2012, 11:39:33 AM
 *
 * @author Peter Withers
 */
public class ArbilDatabase {

    static Context context = new Context();
    static final Object databaseLock = new Object();
    private final String databaseName = "ArbilDatabase";
    final private PluginSessionStorage sessionStorage;
    final private PluginDialogHandler dialogHandler;
    final private PluginBugCatcher bugCatcher;

    public enum SearchOption {

        contains(SearchType.contains, SearchNegator.is, "Contains"),
        notcontains(SearchType.contains, SearchNegator.not, "Does not contain"),
        equals(SearchType.equals, SearchNegator.is, "Equals"),
        notequals(SearchType.equals, SearchNegator.not, "Does not equal"),
        fuzzy(SearchType.fuzzy, SearchNegator.is, "Fuzzy match");

        private SearchOption(SearchType searchType, SearchNegator searchNegator, String displayName) {
            this.searchType = searchType;
            this.searchNegator = searchNegator;
            this.displayName = displayName;
        }
        final SearchType searchType;
        final SearchNegator searchNegator;
        final String displayName;

        public SearchType getSearchType() {
            return searchType;
        }

        public SearchNegator getSearchNegator() {
            return searchNegator;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum SearchType {

        contains,
        equals,
        //        like,
        fuzzy,
//        regex
    }

    public enum SearchNegator {

        is, not
    }

    public enum CriterionJoinType {

        union("Union"), intersect("Interesection"); //, except("Difference");
        final private String displayName;

        private CriterionJoinType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public ArbilDatabase(PluginSessionStorage sessionStorage, PluginDialogHandler dialogHandler, PluginBugCatcher bugCatcher) {
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
        this.bugCatcher = bugCatcher;
        try {
            synchronized (databaseLock) {
                new Set("dbpath", new File(sessionStorage.getApplicationSettingsDirectory(), "BaseXData")).execute(context);
                new Open(databaseName).execute(context);
                new Close().execute(context);
            }
        } catch (BaseXException baseXException) {
            try {
                synchronized (databaseLock) {
                    new CreateDB(databaseName).execute(context);
                }
            } catch (BaseXException baseXException2) {
                bugCatcher.logException(new PluginException(baseXException2.getMessage()));
            }
        }
    }

    public void createDatabase() throws QueryException {
        String suffixFilter = "*.*mdi";
        try {
            synchronized (databaseLock) {
//    System.out.print(new InfoDB().execute(context));
//    new DropIndex("text").execute(context);
//    new DropIndex("attribute").execute(context);
//    new DropIndex("fulltext").execute(context);
                new DropDB(databaseName).execute(context);
                new Set("CREATEFILTER", suffixFilter).execute(context);
                final File cacheDirectory = sessionStorage.getProjectDirectory();
                System.out.println("cacheDirectory: " + cacheDirectory);
                new CreateDB(databaseName, cacheDirectory.toString()).execute(context);
//                System.out.println("Create full text index");
//                new CreateIndex("fulltext").execute(context); // note that the indexes appear to be created by default, so this step might be redundant
            }
        } catch (BaseXException exception) {
            throw new QueryException(exception.getMessage());
        }
    }

    private String getTypeConstraint(MetadataFileType fileType) {
        String typeConstraint = "";
        if (fileType != null) {
            final String imdiType = fileType.getImdiType();
            final String profileId = fileType.getProfileIdString();
            if (imdiType != null) {
                typeConstraint = "[/*:METATRANSCRIPT/count(" + imdiType + ") > 0]";
            } else if (profileId != null) {
                typeConstraint = "[*:CMD/@*:schemaLocation contains text '" + profileId + "']/*:CMD/*:Components/*";
            }
        }
        return typeConstraint;
    }

    private String getFieldConstraint(MetadataFileType fieldType) {
        String fieldConstraint = "";
        if (fieldType != null) {
            final String fieldNameString = fieldType.getFieldName();
            if (fieldNameString != null) {
                fieldConstraint = "[name() = '" + fieldNameString + "']";
            }
        }
        return fieldConstraint;
    }

    private String getSearchTextConstraint(SearchNegator searchNegator, SearchType searchType, String searchString) {
        final String escapedSearchString = escapeBadChars(searchString);
        String returnString = "";
        switch (searchType) {
            case contains:
                if (escapedSearchString.isEmpty()) {
                    // when the user has not entered any string then return all, but allow the negator to still be used
                    returnString = "1=1";
                } else {
                    returnString = "text() contains text '" + escapedSearchString + "'";
                }
                break;
            case equals:
                returnString = "text() = '" + escapedSearchString + "'";
                break;
            case fuzzy:
                returnString = "text() contains text '" + escapedSearchString + "' using fuzzy";
                break;
        }
        switch (searchNegator) {
            case is:
                returnString = "[" + returnString + "]";
                break;
            case not:
                returnString = "[not(" + returnString + ")]";
                break;
        }
        return returnString;
    }

    static String escapeBadChars(String inputString) {
        // our queries use double quotes so single quotes are allowed
        // todo: could ; cause issues?
        return inputString.replace("&", "&amp;").replace("\"", "&quot;").replace("'", "&apos;");
    }
    /*
     * let $elementSet0 := for $nameString0 in collection('ArbilDatabase')//*:Address[count(*) = 0] order by $nameString0 return $nameString0
     let $elementSet1 := for $nameString0 in collection('ArbilDatabase')//*:Region[count(*) = 0] order by $nameString0 return $nameString0
     return
     <TreeNode><DisplayString>All</DisplayString>
     {
     for $nameString0 in distinct-values($elementSet0/text())
     return
     <TreeNode><DisplayString>Address: {$nameString0}</DisplayString>
     {
     let $intersectionSet0 := $elementSet1[root()//*:Address = $nameString0]
     for $nameString1 in distinct-values($intersectionSet0/text())
     return
     <TreeNode><DisplayString>Region: {$nameString1}</DisplayString>
     </TreeNode>
     }
     </TreeNode>
     }
     </TreeNode>
     * */

    private String getTreeSubQuery(ArrayList<MetadataFileType> treeBranchTypeList, String whereClause, String selectClause, String trailingSelectClause, int levelCount) {
        final int maxMetadataFileCount = 100;
        if (!treeBranchTypeList.isEmpty()) {
            String separatorString = "";
//            if (whereClause.length() > 0) {
//                separatorString = ",\n";
//            }
            MetadataFileType treeBranchType = treeBranchTypeList.remove(0);
            String currentFieldName = treeBranchType.getFieldName();
            String nextWhereClause = whereClause + "[//*:" + currentFieldName + " = $nameString" + levelCount + "]";
            String nextSelectClause = selectClause + "[*:" + currentFieldName + " = $nameString" + levelCount + "]";
            String nextTrailingSelectClause = "[*:" + currentFieldName + " = $nameString" + levelCount + "]";
            return "{\n"
                    + "for $nameString" + levelCount + " in distinct-values(collection('" + databaseName + "')" + whereClause + "//*:" + currentFieldName + "[count(*) = 0]\n"
                    //                + "return concat(base-uri($entityNode), path($entityNode))\n"
                    + ")\n"
                    + "order by $nameString" + levelCount + "\n"
                    + "return\n"
                    + "<TreeNode><DisplayString>" + currentFieldName + ": {$nameString" + levelCount + "}</DisplayString>\n"
                    + getTreeSubQuery(treeBranchTypeList, nextWhereClause, nextSelectClause, nextTrailingSelectClause, levelCount + 1)
                    + "</TreeNode>\n}\n";
        } else {
            return "{"
                    //                    + " if (count(collection('" + databaseName + "')" + whereClause + "//.[count(*) = 0][text() != '']" + trailingSelectClause + ") < " + maxMetadataFileCount + ") then\n"
                    + "for $matchingNode in collection('" + databaseName + "')" + whereClause + "//." + trailingSelectClause + "\n"
                    + "return\n"
                    + "<MetadataTreeNode>\n"
                    + "<FileUri>{base-uri($matchingNode)}</FileUri>\n"
                    + "<FileUriPath>{path($matchingNode)}</FileUriPath>\n"
                    + "</MetadataTreeNode>\n"
                    //                    + "else \n"
                    //                    + "<DisplayString>&gt;more than " + maxMetadataFileCount + " results, please add more facets&lt;</DisplayString>"
                    + "\n}\n";
        }
    }

    private String getTreeQuery(ArrayList<MetadataFileType> treeBranchTypeList) {
//        String branchConstraint = "//treeBranchType.getFieldName()";

        return "<TreeNode><DisplayString>All</DisplayString>\n"
                + getTreeSubQuery(treeBranchTypeList, "", "", "", 0)
                + "</TreeNode>";


        /*
         for $d in distinct-values(doc("order.xml")//item/@dept)
         let $items := doc("order.xml")//item[@dept = $d]
         order by $d
         return <department code="{$d}">{
         for $i in $items
         order by $i/@num
         return $i
         }</department>

         */
    }

    private String getTreeFieldNames(MetadataFileType fileType, boolean fastQuery) {
        String countClause;
        if (fastQuery) {
            countClause = "";
        } else {
            countClause = "<RecordCount>{count(distinct-values(collection('ArbilDatabase')/descendant-or-self::*[name() = $nameString]/text()))}</RecordCount>";
        }
        String typeConstraint = getTypeConstraint(fileType);
        String noChildClause = "[count(*) = 0]";
        String hasTextClause = "[text() != '']";
        return "<MetadataFileType>\n"
                + "{\n"
                + "for $nameString in distinct-values(collection('" + databaseName + "')" + typeConstraint + "/descendant-or-self::*" + noChildClause + hasTextClause + "/name()\n"
                + ")\n"
                + "order by $nameString\n"
                + "return\n"
                + "<MetadataFileType>"
                + "<fieldName>{$nameString}</fieldName>"
                + countClause
                + "</MetadataFileType>\n"
                + "}</MetadataFileType>";
    }

    private String getSearchQuery(SearchParameters searchParameters) {
        String typeConstraint = getTypeConstraint(searchParameters.fileType);
        String fieldConstraint = getFieldConstraint(searchParameters.fieldType);
        // todo: add to query: boolean searchNot, SearchType searchType, String searchString
        String searchTextConstraint = getSearchTextConstraint(searchParameters.searchNegator, searchParameters.searchType, searchParameters.searchString);

        return "for $nameString in distinct-values(\n"
                + "for $entityNode in collection('" + databaseName + "')" + typeConstraint + "/descendant-or-self::*" + fieldConstraint + searchTextConstraint + "\n"
                + "return concat(base-uri($entityNode), path($entityNode))\n"
                + ")\n"
                //                + "order by $nameString\n"
                + "return\n"
                + "<MetadataFileType><arbilPathString>{$nameString}</arbilPathString></MetadataFileType>\n";
    }

    private String getPopulatedFieldNames(MetadataFileType fileType) {
        String typeConstraint = getTypeConstraint(fileType);
        return "<MetadataFileType>\n"
                + "<MetadataFileType><displayString>All Fields</displayString></MetadataFileType>\n"
                + "{\n"
                //                + "for $nameString in distinct-values(\n"
                //                + "for $entityNode in collection('" + databaseName + "')" + typeConstraint + "/descendant-or-self::*[count(*) = 0]\n"
                //                + "return $entityNode/name()\n"
                //                + ")\n"
                //                + "order by $nameString\n"
                //                + "return\n"
                //                + "<MetadataFileType>"
                //                + "<fieldName>{$nameString}</fieldName>"
                //                + "<RecordCount>{count(collection('ArbilDatabase')/descendant-or-self::*[name() = $nameString]/text())}</RecordCount>"
                //                + "</MetadataFileType>\n"
                /*
                 * optimised this query 2012-10-17
                 * the query above takes:
                 * 66932.06 ms
                 * the query below takes:
                 * 12.39 ms (varies per run)
                 */
                + "for $facetEntry in index:facets('ArbilDatabase', 'flat')//element[entry/text() != '']\n"
                + "return\n"
                + "<MetadataFileType>\n"
                + "<fieldName>{string($facetEntry/@name)}</fieldName>\n"
                //                + "<RecordCount>{string($facetEntry/@count)}</RecordCount>\n"
                //                + "<ValueCount>{count($facetEntry/entry)}</ValueCount>\n"
                + "<RecordCount>{count($facetEntry/entry)}</RecordCount>\n"
                + "</MetadataFileType>\n"
                + "}</MetadataFileType>";
    }

    private String getMetadataTypes() {
//        return "for $xpathString in distinct-values(\n"
//                + "for $entityNode in collection('" + databaseName + "')/*\n"
//                + "return path($entityNode)\n"
//                + ")\n"
//                + "return"
//                + "$xpathString";
        return "<MetadataFileType>\n"
                + "<MetadataFileType>\n"
                + "<displayString>All Types</displayString>\n"
                + "<RecordCount>{count(collection('ArbilDatabase'))}</RecordCount>\n"
                + "</MetadataFileType>\n"
                + "{\n"
                //                + "for $imdiType in distinct-values(collection('ArbilDatabase')/*:METATRANSCRIPT/*/name())\n"
                //                + "order by $imdiType\n"
                //                + "return\n"
                //                + "<MetadataFileType>\n"
                //                + "<ImdiType>{$imdiType}</ImdiType>\n"
                //                + "<RecordCount>{count(collection('ArbilDatabase')/*:METATRANSCRIPT/*[name()=$imdiType])}</RecordCount>\n"
                //                + "</MetadataFileType>\n"
                //                + "},{"
                //                + "for $profileString in distinct-values(collection('" + databaseName + "')/*:CMD/@*:schemaLocation)\n"
                //                //                + "order by $profileString\n"
                //                + "return\n"
                //                + "<MetadataFileType>\n"
                //                + "<profileString>{$profileString}</profileString>\n"
                //                + "<RecordCount>{count(collection('" + databaseName + "')/*:CMD[@*:schemaLocation = $profileString])}</RecordCount>"
                //                + "</MetadataFileType>\n"
                /*
                 * optimised this query 2012-10-17
                 * the query above takes:
                 * 5014.03 ms
                 * the query below takes:
                 * 11.8 ms (varies per run)
                 */
                + "for $profileInfo in index:facets('ArbilDatabase')/document-node/element[@name='METATRANSCRIPT']/element[@name!='History']\n"
                + "return\n"
                + "<MetadataFileType>\n"
                + "<fieldName>{string($profileInfo/@name)}</fieldName>\n"
                + "<RecordCount>{string($profileInfo/@count)}</RecordCount>\n"
                + "</MetadataFileType>"
                + "},{"
                + "for $profileInfo in index:facets('ArbilDatabase')/document-node/element[@name='CMD']/element[@name='Header']/element[@name='MdProfile']/text/entry\n"
                + "return\n"
                + "<MetadataFileType>\n"
                + "<fieldName>{string($profileInfo)}</fieldName>\n"
                + "<RecordCount>{string($profileInfo/@count)}</RecordCount>\n"
                //                + "<ValueCount>{count($profileInfo/entry)}</ValueCount>\n"
                + "</MetadataFileType>\n"
                //                + "},{"
                + "}</MetadataFileType>";
    }

    private String getPopulatedPaths() {
//        return "for $xpathString in distinct-values(\n"
//                + "for $entityNode in collection('" + databaseName + "')/*\n"
//                + "return path($entityNode)\n"
//                + ")\n"
//                + "return"
//                + "$xpathString";
        return "<MetadataFileType>\n"
                + "<MetadataFileType><displayString>All Types</displayString></MetadataFileType>\n"
                + "{\n"
                + "for $xpathString in distinct-values(\n"
                + "for $entityNode in collection('" + databaseName + "')/*\n"
                + "return path($entityNode)\n"
                + ")\n"
                + "order by $xpathString\n"
                + "return\n"
                + "<MetadataFileType><rootXpath>{$xpathString}</rootXpath></MetadataFileType>\n"
                + "}</MetadataFileType>";
    }

    public MetadataFileType[] getSearchResultMetadataTypes(CriterionJoinType criterionJoinType, ArrayList<SearchParameters> searchParametersList) {
        StringBuilder queryStringBuilder = new StringBuilder();
        StringBuilder joinStringBuilder = new StringBuilder();
        int parameterCounter = 0;
        for (SearchParameters searchParameters : searchParametersList) {
            if (queryStringBuilder.length() > 0) {
                joinStringBuilder.append(" ");
                joinStringBuilder.append(criterionJoinType.name());
                joinStringBuilder.append(" ");
            } else {
                joinStringBuilder.append("return <MetadataFileType>{");
            }
            joinStringBuilder.append("$set");
            joinStringBuilder.append(parameterCounter);
            queryStringBuilder.append("let $set");
            queryStringBuilder.append(parameterCounter);
            queryStringBuilder.append(" := ");
            parameterCounter++;
            queryStringBuilder.append(getSearchQuery(searchParameters));
        }
        joinStringBuilder.append("}</MetadataFileType>");
        queryStringBuilder.append(joinStringBuilder);
        final MetadataFileType[] metadataTypesString = getMetadataTypes(queryStringBuilder.toString());
        return metadataTypesString;
    }

    public MetadataFileType[] getPathMetadataTypes(MetadataFileType metadataFileType) {
        final String queryString = getPopulatedPaths();
        return getMetadataTypes(queryString);
    }

    public MetadataFileType[] getFieldMetadataTypes(MetadataFileType metadataFileType) {
        final String queryString = getPopulatedFieldNames(metadataFileType);
        return getMetadataTypes(queryString);
    }

    public MetadataFileType[] getMetadataTypes(MetadataFileType metadataFileType) {
        final String queryString = getMetadataTypes();
        return getMetadataTypes(queryString);
    }

    public MetadataFileType[] getTreeFieldTypes(MetadataFileType metadataFileType, boolean fastQuery) {
        final String queryString = getTreeFieldNames(metadataFileType, fastQuery);
        return getMetadataTypes(queryString);
    }

//    public DbTreeNode getSearchTreeData() {
//        final String queryString = getTreeQuery(treeBranchTypeList);
//        return getDbTreeNode(queryString);
//    }
    public DbTreeNode getTreeData(final ArrayList<MetadataFileType> treeBranchTypeList) {
        final String queryString = getTreeQuery(treeBranchTypeList);
        return getDbTreeNode(queryString);
    }

    private DbTreeNode getDbTreeNode(String queryString) {
        long startTime = System.currentTimeMillis();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DbTreeNode.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String queryResult;
            synchronized (databaseLock) {
                System.out.println("queryString: " + queryString);
                queryResult = new XQuery(queryString).execute(context);
            }
            System.out.println("queryResult: " + queryResult);
            DbTreeNode rootTreeNode = (DbTreeNode) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), DbTreeNode.class).getValue();
            long queryMils = System.currentTimeMillis() - startTime;
            int resultCount = 0;
            if (rootTreeNode != null) {
                resultCount = 1;
            }
            String queryTimeString = "Query time: " + queryMils + "ms for " + resultCount + " entities";
            System.out.println(queryTimeString);
            return rootTreeNode;
        } catch (JAXBException exception) {
            bugCatcher.logException(new PluginException(exception.getMessage()));
            dialogHandler.addMessageDialogToQueue("Error getting search options", "Search Options");
        } catch (BaseXException exception) {
            bugCatcher.logException(new PluginException(exception.getMessage()));
            dialogHandler.addMessageDialogToQueue("Error getting search options", "Search Options");
        }
        return new DbTreeNode();
    }

    private MetadataFileType[] getMetadataTypes(final String queryString) {
        long startTime = System.currentTimeMillis();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MetadataFileType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String queryResult;
            synchronized (databaseLock) {
                System.out.println("queryString: " + queryString);
                queryResult = new XQuery(queryString).execute(context);
            }
            System.out.println("queryResult: " + queryResult);
            MetadataFileType foundEntities = (MetadataFileType) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), MetadataFileType.class).getValue();
            long queryMils = System.currentTimeMillis() - startTime;
            final MetadataFileType[] entityDataArray = foundEntities.getChildMetadataTypes();
            int resultCount = 0;
            if (entityDataArray != null) {
                resultCount = entityDataArray.length;
            }
            String queryTimeString = "Query time: " + queryMils + "ms for " + resultCount + " entities";
            System.out.println(queryTimeString);
//            selectedEntity.appendTempLabel(queryTimeString);
            return foundEntities.getChildMetadataTypes();
        } catch (JAXBException exception) {
            bugCatcher.logException(new PluginException(exception.getMessage()));
            dialogHandler.addMessageDialogToQueue("Error getting search options", "Search Options");
        } catch (BaseXException exception) {
            bugCatcher.logException(new PluginException(exception.getMessage()));
            dialogHandler.addMessageDialogToQueue("Error getting search options", "Search Options");
        }
        return new MetadataFileType[]{};
    }
}
