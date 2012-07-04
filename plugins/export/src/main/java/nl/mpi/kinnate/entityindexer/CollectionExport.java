package nl.mpi.kinnate.entityindexer;

import java.io.File;
import nl.mpi.arbil.util.BugCatcherManager;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;

/**
 * Document : CollectionExport
 * Created on : Jul 4, 2012, 4:10:42 PM
 * Author : Peter Withers
 */
public class CollectionExport implements CollectionExporter {

    static Context context = new Context();
    static final Object databaseLock = new Object();
    private final String databaseName = "SimpleExportTemp";

    public CollectionExport() {
        // make sure the database exists
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

    public void dropExportDatabase() throws BaseXException {
        synchronized (databaseLock) {
            new DropDB(databaseName).execute(context);
        }
    }

    public void createExportDatabase(File directoryOfInputFiles, String suffixFilter) throws QueryException {
        if (suffixFilter == null) {
            suffixFilter = "*.kmdi";
        }
        try {
            synchronized (databaseLock) {
                new DropDB(databaseName).execute(context);
                new Set("CREATEFILTER", suffixFilter).execute(context);
                new CreateDB(databaseName, directoryOfInputFiles.toString()).execute(context);
            }
        } catch (BaseXException exception) {
            throw new QueryException(exception.getMessage());
        }
    }

    public String performExportQuery(String exportQueryString) throws QueryException {
        String returnString = null;
        try {
            synchronized (databaseLock) {
                returnString = new XQuery(exportQueryString).execute(context);
            }
        } catch (BaseXException exception) {
            throw new QueryException(exception.getMessage());
        }
        return returnString;
    }
}
