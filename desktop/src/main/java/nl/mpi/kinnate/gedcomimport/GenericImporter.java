package nl.mpi.kinnate.gedcomimport;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : GenericImporter
 *  Created on : May 30, 2011, 10:31:15 AM
 *  Author     : Peter Withers
 */
public interface GenericImporter {

    public boolean canImport(String inputFileString);

    public HashMap<String, HashSet<UniqueIdentifier>> getCreatedNodeIds();

    public URI[] importFile(File inputFile, String profileId) throws IOException, ImportException;

    public URI[] importFile(String inputFileString, String profileId) throws IOException, ImportException;
}
