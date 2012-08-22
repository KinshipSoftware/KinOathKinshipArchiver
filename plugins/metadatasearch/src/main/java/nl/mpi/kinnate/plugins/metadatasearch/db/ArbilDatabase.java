package nl.mpi.kinnate.plugins.metadatasearch.db;

import java.io.File;
import java.io.StringReader;
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
            final String rootXpath = fileType.getRootXpath();
            final String profileId = fileType.getProfileIdString();
            if (rootXpath != null) {
                typeConstraint = "[count(" + rootXpath + ") > 0]";
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
                    returnString = "text() = '' or text() != ''";
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
                + "for $entityNode in collection('" + databaseName + "')" + typeConstraint + "/descendant-or-self::*\n"
                + "return $entityNode/name()\n"
                + ")\n"
                + "order by $nameString\n"
                + "return\n"
                + "<MetadataFileType><fieldName>{$nameString}</fieldName></MetadataFileType>\n"
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
                + "<MetadataFileType><displayString>All Types</displayString></MetadataFileType>\n"
                + "{\n"
                + "for $xpathString in distinct-values(\n"
                + "for $entityNode in collection('" + databaseName + "')/*:METATRANSCRIPT/*\n"
                + "return path($entityNode)\n"
                + ")\n"
                + "order by $xpathString\n"
                + "return\n"
                + "<MetadataFileType><rootXpath>{$xpathString}</rootXpath></MetadataFileType>\n"
                + "},{"
                + "for $xpathString in distinct-values(\n"
                + "for $entityNode in collection('" + databaseName + "')/*:CMD/@*:schemaLocation\n"
                + "return $entityNode\n"
                + ")\n"
                + "order by $xpathString\n"
                + "return\n"
                + "<MetadataFileType><profileString>{$xpathString}</profileString></MetadataFileType>\n"
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
