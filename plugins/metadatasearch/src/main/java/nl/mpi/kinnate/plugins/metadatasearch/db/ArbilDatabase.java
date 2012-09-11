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
                new DropDB(databaseName).execute(context);
                new Set("CREATEFILTER", suffixFilter).execute(context);
                final File cacheDirectory = sessionStorage.getProjectDirectory();
                System.out.println("cacheDirectory: " + cacheDirectory);
                new CreateDB(databaseName, cacheDirectory.toString()).execute(context);
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

    private String getTreeSubQuery(ArrayList<MetadataFileType> treeBranchTypeList, String whereClause, String selectClause, String trailingSelectClause, int levelCount) {
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
            return "{for $matchingNode in collection('" + databaseName + "')" + whereClause + "//." + trailingSelectClause + "\n"
                    + "return\n"
                    + "<MetadataTreeNode>\n"
                    + "<FileUri>{base-uri($matchingNode)}</FileUri>\n"
                    + "<FileUriPath>{path($matchingNode)}</FileUriPath>\n"
                    + "</MetadataTreeNode>\n}\n";
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

    private String getTreeFieldNames(MetadataFileType fileType) {
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
                + "<RecordCount>{count(distinct-values(collection('ArbilDatabase')/descendant-or-self::*[name() = $nameString]/text()))}</RecordCount>"
                + "</MetadataFileType>\n"
                + "}</MetadataFileType>";
    }

    private String getSearchQuery(MetadataFileType fileType, MetadataFileType fieldType, SearchNegator searchNegator, SearchType searchType, String searchString) {
        String typeConstraint = getTypeConstraint(fileType);
        String fieldConstraint = getFieldConstraint(fieldType);
        // todo: add to query: boolean searchNot, SearchType searchType, String searchString
        String searchTextConstraint = getSearchTextConstraint(searchNegator, searchType, searchString);

        return "<MetadataFileType>\n"
                + "{\n"
                + "for $nameString in distinct-values(\n"
                + "for $entityNode in collection('" + databaseName + "')" + typeConstraint + "/descendant-or-self::*" + fieldConstraint + searchTextConstraint + "\n"
                + "return concat(base-uri($entityNode), path($entityNode))\n"
                + ")\n"
                //                + "order by $nameString\n"
                + "return\n"
                + "<MetadataFileType><arbilPathString>{$nameString}</arbilPathString></MetadataFileType>\n"
                + "}</MetadataFileType>";
    }

    private String getPopulatedFieldNames(MetadataFileType fileType) {
        String typeConstraint = getTypeConstraint(fileType);
        return "<MetadataFileType>\n"
                + "<MetadataFileType><displayString>All Fields</displayString></MetadataFileType>\n"
                + "{\n"
                + "for $nameString in distinct-values(\n"
                + "for $entityNode in collection('" + databaseName + "')" + typeConstraint + "/descendant-or-self::*[count(*) = 0]\n"
                + "return $entityNode/name()\n"
                + ")\n"
                + "order by $nameString\n"
                + "return\n"
                + "<MetadataFileType>"
                + "<fieldName>{$nameString}</fieldName>"
                + "<RecordCount>{count(collection('ArbilDatabase')/descendant-or-self::*[name() = $nameString]/text())}</RecordCount>"
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
                + "for $imdiType in distinct-values(collection('ArbilDatabase')/*:METATRANSCRIPT/*/name())\n"
                + "order by $imdiType\n"
                + "return\n"
                + "<MetadataFileType>\n"
                + "<ImdiType>{$imdiType}</ImdiType>\n"
                + "<RecordCount>{count(collection('ArbilDatabase')/*:METATRANSCRIPT/*[name()=$imdiType])}</RecordCount>\n"
                + "</MetadataFileType>\n"
                + "},{"
                + "for $profileString in distinct-values(collection('" + databaseName + "')/*:CMD/@*:schemaLocation)\n"
                //                + "order by $profileString\n"
                + "return\n"
                + "<MetadataFileType>\n"
                + "<profileString>{$profileString}</profileString>\n"
                + "<RecordCount>{count(collection('" + databaseName + "')/*:CMD[@*:schemaLocation = $profileString])}</RecordCount>"
                + "</MetadataFileType>\n"
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

    public MetadataFileType[] getSearchResultMetadataTypes(MetadataFileType fileType, MetadataFileType fieldType, SearchNegator searchNegator, SearchType searchType, String searchString) {
        final String queryString = getSearchQuery(fileType, fieldType, searchNegator, searchType, searchString);
        return getMetadataTypes(queryString);
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

    public MetadataFileType[] getTreeFieldTypes(MetadataFileType metadataFileType) {
        final String queryString = getTreeFieldNames(metadataFileType);
        return getMetadataTypes(queryString);
    }

    public DbTreeNode getTreeData(final ArrayList<MetadataFileType> treeBranchTypeList) {
        final String queryString = getTreeQuery(treeBranchTypeList);
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
