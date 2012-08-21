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

    public String getPopulatedFieldNames() {
        return "<MetadataFileType>\n"
                + "<MetadataFileType><displayString>All</displayString></MetadataFileType>\n"
                + "{\n"
                + "for $nameString in distinct-values(\n"
                + "for $entityNode in collection('" + databaseName + "')/descendant-or-self::*\n"
                + "return $entityNode/name()\n"
                + ")\n"
                + "order by $nameString\n"
                + "return\n"
                + "<MetadataFileType><pathPart>{$nameString}</pathPart></MetadataFileType>\n"
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
                + "<MetadataFileType><displayString>All</displayString></MetadataFileType>\n"
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

    public MetadataFileType[] getMetadataTypes(MetadataFileType metadataFileType) {
        long startTime = System.currentTimeMillis();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MetadataFileType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String queryResult;
            synchronized (databaseLock) {
                final String queryString = getPopulatedFieldNames();
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
