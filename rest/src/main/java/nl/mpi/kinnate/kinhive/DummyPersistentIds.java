package nl.mpi.kinnate.kinhive;

/**
 *  Document   : DummyPersistentIds
 *  Created on : Jul 1, 2011, 11:04:28 AM
 *  Author     : Peter Withers
 */
public class DummyPersistentIds {

    static public String getPID() {
        // todo: do not do this in production, the time on the server could be wrong and there might be two servers or cpus and there might be two requests in the space of one ms...
        return "dummyPID:" + System.currentTimeMillis();
    }
}
