package nl.mpi.kinnate.gedcomimport;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Created on : May 30, 2011, 10:31:15 AM
 *
 * @author Peter Withers
 */
public interface GenericImporter {

    public boolean canImport(String inputFileString);

    public HashMap<String, HashSet<UniqueIdentifier>> getCreatedNodeIds();

    public UniqueIdentifier[] importFile(File inputFile, String profileId) throws IOException, ImportException;

    public UniqueIdentifier[] importFile(String inputFileString, String profileId) throws IOException, ImportException;
}
