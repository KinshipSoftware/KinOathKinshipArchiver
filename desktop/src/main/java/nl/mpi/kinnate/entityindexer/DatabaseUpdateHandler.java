package nl.mpi.kinnate.entityindexer;

import java.util.ArrayList;

/**
 * Document : DatabaseUpdateHandler
 * Created on : Apr 25, 2012, 5:09:44 PM
 * Author : Peter Withers
 */
public class DatabaseUpdateHandler {

    ArrayList<DatabaseUpdateListener> registeredListeners = new ArrayList<DatabaseUpdateListener>();

    public void addDatabaseUpdateListener(DatabaseUpdateListener databaseUpdateListener) {
        registeredListeners.add(databaseUpdateListener);
    }

    protected void updateOccured() {
        for (DatabaseUpdateListener databaseUpdateListener : registeredListeners) {
            databaseUpdateListener.updateOccured();
        }
    }
}
