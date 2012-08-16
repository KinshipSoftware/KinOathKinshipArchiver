package nl.mpi.kinnate.entityindexer;

import java.io.File;
import nl.mpi.arbil.plugin.PluginBugCatcher;
import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.arbil.plugin.PluginSessionStorage;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;

/**
 * Document : CollectionExport Created on : Jul 4, 2012, 4:10:42 PM Author :
 * Peter Withers
 */
public class CollectionExport implements CollectionExporter {

    static Context context = new Context();
    static final Object databaseLock = new Object();
    private final String databaseName = "SimpleExportTemp";
    final PluginBugCatcher bugCatcher;

    public CollectionExport(PluginBugCatcher bugCatcher, PluginSessionStorage sessionStorage) {
        this.bugCatcher = bugCatcher;
        // make sure the database exists
        try {
            synchronized (databaseLock) {
                new Set("dbpath", new File(sessionStorage.getStorageDirectory(), "BaseXData")).execute(context);
                new Open(databaseName).execute(context);
                new Close().execute(context);
            }
        } catch (BaseXException baseXException) {
            try {
                synchronized (databaseLock) {
                    new CreateDB(databaseName).execute(context);
                }
            } catch (BaseXException baseXException2) {
                bugCatcher.logException(new PluginException("failed to create database: " + databaseName + " : " + baseXException2.getMessage()));
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

    public String dropAndImportCsv(File directoryOfInputFiles, String suffixFilter) throws QueryException {
        if (suffixFilter == null) {
            suffixFilter = "*.csv";
        }
        try {
            synchronized (databaseLock) {
                new DropDB(databaseName).execute(context);
                new Set("CREATEFILTER", suffixFilter).execute(context);
                new CreateDB(databaseName).execute(context);

                String importQuery = "declare option db:parser \"csv\";\n"
                        + "declare option db:parseropt \"header=yes\";\n"
                        + "for $file in file:list(\"" + directoryOfInputFiles.toString() + "\", false(), \"" + suffixFilter + "\")\n"
                        + "return db:add('" + databaseName + "', $file)\n";
                return performExportQuery(importQuery);
            }
        } catch (BaseXException exception) {
            throw new QueryException(exception.getMessage());
        }
    }

    public String getDatabaseName() {
        return databaseName;
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
