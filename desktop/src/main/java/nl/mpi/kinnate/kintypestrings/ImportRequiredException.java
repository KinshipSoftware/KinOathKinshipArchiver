package nl.mpi.kinnate.kintypestrings;

import java.net.URI;

/**
 * Document : ImportRequiredException
 * Created on : Apr 20, 2012, 11:29:39 AM
 * Author : Peter Withers
 */
public class ImportRequiredException extends Exception {

    String messageString;
    URI importURI;

    public ImportRequiredException(String messageString, URI importURI) {
        this.messageString = messageString;
        this.importURI = importURI;
    }
}
