package nl.mpi.kinnate.entityindexer;

/**
 * Document : CollectionExporter
 * Created on : Jul 4, 2012, 4:21:07 PM
 * Author : Peter Withers
 */
public interface CollectionExporter {

    public String performExportQuery(String exportQueryString) throws QueryException;

    public String getDatabaseName();
}
