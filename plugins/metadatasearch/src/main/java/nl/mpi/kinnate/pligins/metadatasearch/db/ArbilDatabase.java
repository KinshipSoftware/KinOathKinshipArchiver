package nl.mpi.kinnate.pligins.metadatasearch.db;

import java.io.File;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
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
 * Document : ArbilDatabase
 * Created on : Aug 6, 2012, 11:39:33 AM
 * Author : Peter Withers
 */
public class ArbilDatabase {

    static Context context = new Context();
    static final Object databaseLock = new Object();
    private final String databaseName = "ArbilDatabase";
    final private SessionStorage sessionStorage;
    final private MessageDialogHandler dialogHandler;

    public ArbilDatabase(SessionStorage sessionStorage, MessageDialogHandler dialogHandler) {
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
        try {
            synchronized (databaseLock) {
                new Open(databaseName).execute(context);
                new Close().execute(context);
            }
        } catch (BaseXException baseXException) {
            try {
                synchronized (databaseLock) {
                    new CreateDB(databaseName).execute(context);
                }
            } catch (BaseXException baseXException2) {
                BugCatcherManager.getBugCatcher().logError(baseXException2);
            }
        }
    }

    public void createDatabase() throws QueryException {
        String suffixFilter = "*.*mdi";
        try {
            synchronized (databaseLock) {
                new DropDB(databaseName).execute(context);
                new Set("CREATEFILTER", suffixFilter).execute(context);
                final File cacheDirectory = sessionStorage.getCacheDirectory();
                System.out.println("cacheDirectory: " + cacheDirectory);
                new CreateDB(databaseName, cacheDirectory.toString()).execute(context);
            }
        } catch (BaseXException exception) {
            throw new QueryException(exception.getMessage());
        }
    }

    private String getQuery() {
//        return "for $xpathString in distinct-values(\n"
//                + "for $entityNode in collection('" + databaseName + "')/*\n"
//                + "return path($entityNode)\n"
//                + ")\n"
//                + "return"
//                + "$xpathString";
        return "<MetadataFileType>\n"
                + "<childMetadataTypes>{\n"
                + "for $xpathString in distinct-values(\n"
                + "for $entityNode in collection('" + databaseName + "')/*\n"
                + "return path($entityNode)\n"
                + ")\n"
                + "return\n"
                + "<MetadataFileType><rootXpath>{$xpathString}</rootXpath></MetadataFileType>\n"
                + "}</childMetadataTypes></MetadataFileType>";
    }

    public MetadataFileType[] getMetadataTypes(MetadataFileType metadataFileType[]) {
        long startTime = System.currentTimeMillis();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MetadataFileType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String queryResult;
            synchronized (databaseLock) {
                final String queryString = getQuery();
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
            BugCatcherManager.getBugCatcher().logError(exception);
            dialogHandler.addMessageDialogToQueue("Error getting search options", "Search Options");
        } catch (BaseXException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            dialogHandler.addMessageDialogToQueue("Error getting search options", "Search Options");
        }
        return new MetadataFileType[]{};
    }
}
