package nl.mpi.kinnate.entityindexer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.KinOathVersion;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityArray;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kintypestrings.KinTypeElement;
import nl.mpi.kinnate.projects.ProjectManager;
import nl.mpi.kinnate.projects.ProjectRecord;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifierArray;
import nl.mpi.kinnate.userstorage.KinSessionStorage;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Optimize;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.iter.Iter;
import org.basex.query.value.item.Item;

/**
 * Created on : Feb 15, 2011, 5:37:06 PM
 *
 * @author Peter Withers
 */
public class EntityCollection extends DatabaseUpdateHandler {
    
    final private SessionStorage sessionStorage;
    final private MessageDialogHandler dialogHandler;
    final private String databaseName; // = "nl-mpi-kinnate";
    final private ProjectRecord projectRecord;
    final static Context context = new Context();
    final static Object databaseLock = new Object();
    final private String dbErrorMessage = "Could not perform the required query, not all data might be shown at this point.\nSee the log file via the help menu for more details.";
    
    public class SearchResults {
        
        public String[] resultsPathArray;
        public String statusMessage;
        public int resultCount = 0;
    }
    
    public EntityCollection(SessionStorage sessionStorage, MessageDialogHandler dialogHandler, ProjectRecord projectRecord) {
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
        this.projectRecord = projectRecord;
        databaseName = projectRecord.getProjectUUID();
        // make sure the database exists
        try {
            synchronized (databaseLock) {
                new Set("dbpath", new File(sessionStorage.getApplicationSettingsDirectory(), "BaseXData")).execute(context);
                new Open(databaseName).execute(context);
                //context.close();
                new Close().execute(context);
            }
        } catch (BaseXException baseXException) {
            try {
                synchronized (databaseLock) {
                    new CreateDB(databaseName).execute(context);
//                new Open(databaseName).execute(context);
                }
            } catch (BaseXException baseXException2) {
                BugCatcherManager.getBugCatcher().logError(baseXException2);
            }
        }
        // todo: should we explicitly close the DB? putting it in the distructor would not be reliable
        // todo: however now that we close via the Close() method it seems fine and the DB is not explicitly opened 
    }

//    public void closeDataBase() {
//        try {
//            new Close().execute(context);
//        } catch (BaseXException baseXException2) {
//            new ArbilBugCatcher().logError(baseXException2);
//        }
//    }
    // see comments below
    @Deprecated
    protected void createDatabase() {
        // this continues to cause the inserted data to be non updateable without creating duplicates
        // todo: if this is required then we will need to walk the working directory and add each file via addFileToDB
        try {
//            System.out.println("List: " + new List().execute(context));
            synchronized (databaseLock) {
                new DropDB(databaseName).execute(context);
                new Set("CREATEFILTER", "*.kmdi").execute(context);
                new CreateDB(databaseName, sessionStorage.getProjectWorkingDirectory().toString()).execute(context);
//            System.out.println("List: " + new List().execute(context));
//            System.out.println("Find: " + new Find(databaseName).title());
//            System.out.println("Info: " + new Info().execute(context));
//            new Open(databaseName).execute(context);
//            new CreateIndex("text").execute(context); // TEXT|ATTRIBUTE|FULLTEXT|PATH
//            new CreateIndex("fulltext").execute(context);
//            new CreateIndex("attribute").execute(context);
//            new CreateIndex("path").execute(context);
//            new Close().execute(context);
//            context.close();
            }
        } catch (BaseXException baseXException) {
            BugCatcherManager.getBugCatcher().logError(baseXException);
        }
    }

    /////////////////// Export Queries ///////////////////
    public Context openExistingExportDatabase(String exportDatabaseName) throws BaseXException {
        Context tempDbContext = new Context();
        synchronized (databaseLock) {
            new Open(exportDatabaseName).execute(tempDbContext);
        }
        return tempDbContext;
    }
    
    public void dropExportDatabase(Context tempDbContext, String exportDatabaseName) throws BaseXException {
        synchronized (databaseLock) {
            new DropDB(exportDatabaseName).execute(tempDbContext);
        }
    }
    
    public Context createExportDatabase(File directoryOfInputFiles, String suffixFilter, String exportDatabaseName) throws BaseXException {
        if (suffixFilter == null) {
            suffixFilter = "*.kmdi";
        }
        Context tempDbContext = new Context();
        synchronized (databaseLock) {
            new DropDB(exportDatabaseName).execute(tempDbContext);
            new Set("CREATEFILTER", suffixFilter).execute(tempDbContext);
            new CreateDB(exportDatabaseName, directoryOfInputFiles.toString()).execute(tempDbContext);
        }
        return tempDbContext;
    }
    
    public String performExportQuery(Context tempDbContext, String exportDatabaseName, String exportQueryString) throws BaseXException {
        if (tempDbContext == null) {
            tempDbContext = context;
        }
        String returnString = null;
        synchronized (databaseLock) {
            if (exportDatabaseName != null) {
                new Close().execute(context);
                // todo: verify that opeing two database at the same time will not cause issues
                new Open(exportDatabaseName).execute(tempDbContext);
            }
            returnString = new XQuery(exportQueryString).execute(tempDbContext);
            if (exportDatabaseName != null) {
                new Close().execute(tempDbContext);
                new Open(databaseName).execute(context);
            }
        }
        return returnString;
    }
    /////////////////// End Export Queries ///////////////////

    public void dropDatabase() {
        try {
            synchronized (databaseLock) {
                new DropDB(databaseName).execute(context);
                System.out.println("List: " + new List().execute(context));
            }
        } catch (BaseXException baseXException) {
            BugCatcherManager.getBugCatcher().logError(baseXException);
        }
    }
    
    private void addFileToDB(URI updatedDataUrl, UniqueIdentifier updatedFileIdentifier) {
        // the document might be in any location, so the url must be used to add to the DB, but the ID must be used to remove the old DB entries, so that old records will removed including duplicates
        String urlString = updatedDataUrl.toASCIIString();
        try {
            synchronized (databaseLock) {
                // delete appears to be fine with a uri string, providing that the document was added as individually and not added as a collection
                // the use of DELETE has been replaced by deleting via the ID in a query
//                new Delete(urlString).execute(context);
                runDeleteQuery(updatedFileIdentifier);
                // add requires a url other wise it appends the working path when using base-uri in a query
                // add requires the parent directory otherwise it adds the file name to the root and appends the working path when using base-uri in a query
                // add appears not to have been tested by anybody, I am not sure if I like basex now, but the following works
                new Add(urlString.replaceFirst("[^/]*$", ""), urlString).execute(context);
            }
        } catch (BaseXException baseXException) {
            // todo: if this throws here then the db might be corrupt and the user needs a way to drop and repopulate the db
            BugCatcherManager.getBugCatcher().logError(baseXException);
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* baseXException.getMessage() */, "Add File To DB");
        }
    }
    
    public void deleteFromDatabase(UniqueIdentifier updatedFileIdentifier) {
        try {
            synchronized (databaseLock) {
                new Open(databaseName).execute(context);
                // the use of DELETE has been replaced by deleting via the ID in a query
//                new Delete(urlString).execute(context);
                runDeleteQuery(updatedFileIdentifier);
                new Optimize().execute(context);
                new Close().execute(context);
            }
            updateOccured();
        } catch (BaseXException baseXException) {
            // todo: if this throws here then the db might be corrupt and the user needs a way to drop and repopulate the db
            BugCatcherManager.getBugCatcher().logError(baseXException);
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* baseXException.getMessage() */, "Add File To DB");
        }
    }
    
    public void updateDatabase(final UniqueIdentifier[] updatedFileArray, final JProgressBar progressBar) {
        try {
            if (progressBar != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        progressBar.setMinimum(0);
                        progressBar.setMaximum(updatedFileArray.length);
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(0);
                    }
                });
            }
            synchronized (databaseLock) {
                new Open(databaseName).execute(context);
                for (UniqueIdentifier updatedUniqueIdentifier : updatedFileArray) {
                    addFileToDB(updatedUniqueIdentifier.getFileInProject(sessionStorage).toURI(), updatedUniqueIdentifier);
                    if (progressBar != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                progressBar.setValue(progressBar.getValue() + 1);
                            }
                        });
                    }
                }
                if (progressBar != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            progressBar.setIndeterminate(true);
                        }
                    });
                }
                new Optimize().execute(context);
                if (progressBar != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            progressBar.setIndeterminate(false);
                        }
                    });
                }
                new Close().execute(context);
            }
            updateOccured();
        } catch (BaseXException baseXException) {
            BugCatcherManager.getBugCatcher().logError(baseXException);
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* baseXException.getMessage() */, "Update Database");
        }
    }
    
    public void updateDatabase(URI updatedFile, UniqueIdentifier updatedFileIdentifier) {
        // it would appear that a re adding a file does not remove the old entries so for now we will dump and recreate the entire database
        // update, this has been updated and adding directories as a collection breaks the update and delete methods in basex so we now do each document individualy
//        createDatabase();
        try {
            synchronized (databaseLock) {
                new Open(databaseName).execute(context);
                addFileToDB(updatedFile, updatedFileIdentifier);
                new Optimize().execute(context);
                new Close().execute(context);
            }
            updateOccured();
        } catch (BaseXException baseXException) {
            BugCatcherManager.getBugCatcher().logError(baseXException);
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* baseXException.getMessage() */, "Update Database");
        }
    }

//    public SearchResults listGedcomFamIds() {
//        // todo: probably needs to be updated.
//        String queryString = "distinct-values(collection('nl-mpi-kinnate')/*:Kinnate/*:Gedcom[*:Entity/*:GedcomType='FAM']/*:UniqueIdentifier//text())";
//        return performQuery(queryString);
//    }
//    public SearchResults listAllRelationTypes() {
//        // todo: probably needs to be updated.
//        // todo: use this to populate the InderParametersFormUI
//        String queryString = "distinct-values(collection('nl-mpi-kinnate')/Kinnate/Relation/Type/text())";
//        return performQuery(queryString);
//    }
    public SearchResults searchByName(String namePartString) {
        String queryString = "for $doc in collection('nl-mpi-kinnate') where contains(string-join($doc//text()), \"" + namePartString + "\") return base-uri($doc)";
        return performQuery(queryString);
    }
    
    public SearchResults searchForLocalEntites() {
        String queryString = "for $doc in collection('nl-mpi-kinnate') where exists(/*:Kinnate/*:Entity/*:Identifier/@*:type=\"lid\") return base-uri($doc)";
        return performQuery(queryString);
    }
    
    private SearchResults performQuery(String queryString) {
        SearchResults searchResults = new SearchResults();
        ArrayList<String> resultPaths = new ArrayList<String>();
        try {
            synchronized (databaseLock) {
                //for $doc in collection('nl-mpi-kinnate')  where $doc//NAME="Bob /Cox/" return base-uri($doc)
//            String query = "for $doc in collection('nl-mpi-kinnate') where $doc//NAME = \"" + namePartString + "\" return base-uri($doc)";
                QueryProcessor proc = new QueryProcessor(queryString, context);//Emp[contains(Ename,"AR")]
                Iter iter = proc.iter();
                Item item;
                while ((item = iter.next()) != null) {
//                System.out.println(item.toJava());
                    resultPaths.add(item.toJava().toString());
                    searchResults.resultCount++;
                }
                proc.close();
            }
            searchResults.statusMessage = "found " + searchResults.resultCount + " records";
        } catch (QueryException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            searchResults.statusMessage = exception.getMessage();
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* exception.getMessage() */, "Perform Query");
        }
        searchResults.resultsPathArray = resultPaths.toArray(new String[]{});
//        searchResults.statusMessage = searchResults.statusMessage + "\n query: " + queryString;
        return searchResults;
    }
    
    public UniqueIdentifier[] getEntityIdByTerm(KinTypeElement queryTerms) {
        // todo: add a query cache or determine that the xml database does the job of caching adequately (p.s. basex appears to cache the queries adequately)
        UniqueIdentifier[] returnArray = new UniqueIdentifier[]{};
        QueryBuilder queryBuilder = new QueryBuilder();
        String queryString = queryBuilder.getTermQuery(queryTerms);
        System.out.println("queryString: " + queryString);
        long startTime = System.currentTimeMillis();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(UniqueIdentifierArray.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String queryResult;
            synchronized (databaseLock) {
                queryResult = new XQuery(queryString).execute(context);
            }
//            System.out.println("queryResult: " + queryResult);
            UniqueIdentifierArray identifierArray;
            if (queryResult.length() > 0) {
                // filter out the name spaces from the xml
//                XMLReader reader = XMLReaderFactory.createXMLReader();
//                KinXmlFilter kinXmlFilter = new KinXmlFilter();
//                kinXmlFilter.setParent(reader);
//                SAXSource saxSource = new SAXSource(kinXmlFilter, new InputSource(new BufferedReader(new StringReader(queryResult))));
//                selectedUniqueIdentifiers = (UniqueIdentifier[]) unmarshaller.unmarshal(saxSource, UniqueIdentifier[].class).getValue();
                // or leave the name space as is
                identifierArray = unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), UniqueIdentifierArray.class).getValue();
                if (identifierArray != null && identifierArray.testIdentifiers != null) {
                    returnArray = identifierArray.testIdentifiers;
                }
                long queryMils = System.currentTimeMillis() - startTime;
                String queryTimeString = "Query time: " + queryMils + "ms for " + returnArray.length + " UniqueIdentifiers";
                System.out.println(queryTimeString);
            }
        } catch (JAXBException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* exception.getMessage() */, "Get Entity Id ByTerm");
        } catch (BaseXException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* exception.getMessage() */, "Get Entity Id By Term");
        }
        return returnArray;
    }
    
    public EntityData[] getEntityByEndPoint(DataTypes.RelationType relationType, IndexerParameters indexParameters) {
        QueryBuilder queryBuilder = new QueryBuilder();
        String query1String = queryBuilder.getEntityByEndPointQuery(relationType, indexParameters);
        System.out.println("getEntityByEndPoint:" + query1String);
        return getEntityByQuery(query1String, indexParameters);
    }
    
    public EntityData[] getEntityByKeyWord(String keyWords, IndexerParameters indexParameters) {
        QueryBuilder queryBuilder = new QueryBuilder();
        String query1String = queryBuilder.getEntityByKeyWordQuery(keyWords, indexParameters);
        return getEntityByQuery(query1String, indexParameters);
    }
    
    private EntityData[] getEntityByQuery(String query1String, IndexerParameters indexParameters) {
        long startTime = System.currentTimeMillis();
        System.out.println("query1String: " + query1String);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EntityArray.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String queryResult;
            synchronized (databaseLock) {
                queryResult = new XQuery(query1String).execute(context);
            }
//            System.out.println("queryResult: " + queryResult);
            EntityArray foundEntities = (EntityArray) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), EntityArray.class).getValue();
            long queryMils = System.currentTimeMillis() - startTime;
            final EntityData[] entityDataArray = foundEntities.getEntityDataArray();
            int resultCount = 0;
            if (entityDataArray != null) {
                resultCount = entityDataArray.length;
            }
            String queryTimeString = "Query time: " + queryMils + "ms for " + resultCount + " entities";
            System.out.println(queryTimeString);
//            selectedEntity.appendTempLabel(queryTimeString);
            return foundEntities.getEntityDataArray();
        } catch (JAXBException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* exception.getMessage() */, "Get Entity By Key Word");
        } catch (BaseXException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* exception.getMessage() */, "Get Entity By Key Word");
        }
        return new EntityData[]{};
    }
    
    public EntityData[] getEntityWithRelations(UniqueIdentifier uniqueIdentifier, String[] excludeUniqueIdentifiers, IndexerParameters indexParameters) {
        // todo: probably needs to be updated.
        long startTime = System.currentTimeMillis();
        QueryBuilder queryBuilder = new QueryBuilder();
        String query1String = queryBuilder.getEntityWithRelationsQuery(uniqueIdentifier, excludeUniqueIdentifiers, indexParameters);
//        System.out.println("query1String: " + query1String);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EntityData.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String queryResult;
            synchronized (databaseLock) {
                queryResult = new XQuery(query1String).execute(context);
            }
//            System.out.println("queryResult: " + queryResult);
            EntityData[] selectedEntity = (EntityData[]) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), EntityData[].class).getValue();
            long queryMils = System.currentTimeMillis() - startTime;
            String queryTimeString = "Query time: " + queryMils + "ms for " + selectedEntity.length + " entities";
            System.out.println(queryTimeString);
//            selectedEntity.appendTempLabel(queryTimeString);
            return selectedEntity;
        } catch (JAXBException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* exception.getMessage() */, "Get Entity With Relations");
        } catch (BaseXException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* exception.getMessage() */, "Get Entity With Relations");
        }
        return new EntityData[]{}; //(uniqueIdentifier, null, "", EntityData.SymbolType.none, new String[]{"Error loading data", "view log for details"}, false);
    }
    
    public void runDeleteQuery(UniqueIdentifier uniqueIdentifier) {
        QueryBuilder queryBuilder = new QueryBuilder();
        String query1String = queryBuilder.getDeleteQuery(uniqueIdentifier);
//        System.out.println("query1String: " + query1String);
        try {
            long startQueryTime = System.currentTimeMillis();
            String queryResult;
            synchronized (databaseLock) {
                queryResult = new XQuery(query1String).execute(context);
            }
            long queryMils = System.currentTimeMillis() - startQueryTime;
            System.out.println("Query time: " + queryMils + "ms");
        } catch (BaseXException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* exception.getMessage() */, "Run Delete Query");
        }
    }
    
    public String getEntityPath(UniqueIdentifier uniqueIdentifier) {
        QueryBuilder queryBuilder = new QueryBuilder();
        String query1String = queryBuilder.getEntityPath(uniqueIdentifier);
//        System.out.println("query1String: " + query1String);
        try {
            long startQueryTime = System.currentTimeMillis();
            String queryResult;
            synchronized (databaseLock) {
                queryResult = new XQuery(query1String).execute(context);
            }
            long queryMils = System.currentTimeMillis() - startQueryTime;
            System.out.println("Query time: " + queryMils + "ms");
            return queryResult;
        } catch (BaseXException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* exception.getMessage() */, "Get Entity Path");
        }
        return null;
    }
    
    public EntityData getEntity(UniqueIdentifier uniqueIdentifier, IndexerParameters indexParameters) {
//        long startTime = System.currentTimeMillis();
        QueryBuilder queryBuilder = new QueryBuilder();
        String query1String = queryBuilder.getEntityQuery(uniqueIdentifier, indexParameters);
        String queryResult = "";
//        System.out.println("query1String: " + query1String);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EntityData.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//            long startQueryTime = System.currentTimeMillis();
            synchronized (databaseLock) {
                queryResult = new XQuery(query1String).execute(context);
            }
//            long queryMils = System.currentTimeMillis() - startQueryTime;
//            System.out.println("Query time: " + queryMils + "ms");
//            long startJaxbTime = System.currentTimeMillis();
//            System.out.println("queryResult: " + queryResult);
            EntityData selectedEntity = (EntityData) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), EntityData.class).getValue();
//            long queryJaxBMils = System.currentTimeMillis() - startJaxbTime;
//            System.out.println("JaxB time: " + queryJaxBMils + "ms");
//            long queryTotalMils = System.currentTimeMillis() - startTime;
            // todo: this should not be called if the entire database has been loaded into the tree, check why it occurs
//            final String queryTimeString = "Total Query time: " + queryTotalMils + "ms";
//            System.out.println(queryTimeString);
//            selectedEntity.appendTempLabel(queryTimeString);
//            System.out.println("Query Result: " + queryResult);
            return selectedEntity;
        } catch (JAXBException exception) {
            // this is where the symptom of duplicate ids has been seen, but it should have been resolved by the delete query replacing the DELETE command
            BugCatcherManager.getBugCatcher().logError(query1String + "\n" + queryResult, exception);
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* exception.getMessage() */, "Get Entity");
        } catch (BaseXException exception) {
            BugCatcherManager.getBugCatcher().logError(query1String + "\n" + queryResult, exception);
            dialogHandler.addMessageDialogToQueue(dbErrorMessage /* exception.getMessage() */, "Get Entity");
        }
        return new EntityData(uniqueIdentifier, new String[]{"Error loading data", "view log for details"});
    }
    
    static public void main(String[] args) {
        JFrame jFrame = new JFrame("Test Query Window");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JTextArea queryText = new JTextArea();
        final JLabel queryTimeLabel = new JLabel();
        final ArbilWindowManager arbilWindowManager = new ArbilWindowManager();
        final KinSessionStorage kinSessionStorage = new KinSessionStorage(new ApplicationVersionManager(new KinOathVersion()));
        final EntityCollection entityCollection = new EntityCollection(kinSessionStorage, arbilWindowManager, new ProjectManager().getDefaultProject(kinSessionStorage));
        //queryText.setText(new QueryBuilder().getEntityQuery("e4dfbd92d311088bf692211ced5179e5", new IndexerParameters()));
//        queryText.setText(new QueryBuilder().getRelationQuery("e4dfbd92d311088bf692211ced5179e5", new IndexerParameters()));
//        queryText.setText(new QueryBuilder().getEntityQuery("e4dfbd92d311088bf692211ced5179e5", new IndexerParameters()));
//        queryText.setText(new QueryBuilder().getEntityWithRelationsQuery("e4dfbd92d311088bf692211ced5179e5", new String[]{"e4dfbd92d311088bf692211ced5179e5"}, new IndexerParameters()));
        queryText.setText("for $entityNode in collection('nl-mpi-kinnate')/*:Kinnate[(*:Entity|*:Gedcom)/*:UniqueIdentifier/. = \"e4dfbd92d311088bf692211ced5179e5\"]\n"
                + "return<Entity>{\n"
                + "<Identifier>{$entityNode/(*:Entity|*:Gedcom)/*:UniqueIdentifier//text()}</Identifier>,\n"
                + "<DateOfBirth>{$entityNode/(*:Entity|*:Gedcom)/DOB}</DateOfBirth>,\n"
                + "<Path>{base-uri($entityNode)}</Path>\n"
                + "}</Entity>\n");
        final JTextArea resultsText = new JTextArea();
        resultsText.setVisible(false);
        JButton jButton = new JButton("run query");
        jButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resultsText.setText("");
                try {
                    long startTime = System.currentTimeMillis();
                    resultsText.append(new XQuery(queryText.getText()).execute(context));
                    long queryMils = System.currentTimeMillis() - startTime;
                    String queryTimeString = "Query time: " + queryMils + "ms";
                    queryTimeLabel.setText(queryTimeString);
                } catch (BaseXException exception) {
                    resultsText.append(exception.getMessage());
                    arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Action Performed");
                }
//                SearchResults results = entityCollection.performQuery(queryText.getText());
//                for (String resultLine : results.resultsPathArray) {
//                    resultsText.append(resultLine + "\n");
//                }
                resultsText.setVisible(true);
            }
        });
//        JButton updateButton = new JButton("update file");
//        updateButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                resultsText.setText("");
//                try {
//                    entityCollection.updateDatabase(new URI("file:/Users/petwit/.arbil/ArbilWorkingFiles/ca1641fc8828f9edb295d1e7b3d37405/_PARENTS_.kmdi"));
//                } catch (URISyntaxException exception) {
//                    resultsText.append(exception.getMessage());
//                    arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Action Performed");
//                }
//                resultsText.setVisible(true);
//            }
//        });
        JButton dropButton = new JButton("drop database");
        dropButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resultsText.setText("");
                entityCollection.dropDatabase();
//                try {
//                new EntityCollection().createDatabase();
//                } catch (URISyntaxException exception) {
//                    resultsText.append(exception.getMessage());
//                }
                resultsText.setVisible(true);
            }
        });
        JButton recreateButton = new JButton("drop and recreate database");
        recreateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resultsText.setText("recreating database");
//                new EntityCollection().dropDatabase();
//                try {
                entityCollection.createDatabase();
                resultsText.setText("done\n");
//                } catch (URISyntaxException exception) {
//                    resultsText.append(exception.getMessage());
//                }
                resultsText.setVisible(true);
            }
        });
        
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.add(queryText, BorderLayout.CENTER);
        jPanel.add(resultsText, BorderLayout.PAGE_END);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(jButton);
//        buttonPanel.add(updateButton);
        buttonPanel.add(dropButton);
        buttonPanel.add(recreateButton);
        buttonPanel.add(queryTimeLabel);
        jPanel.add(buttonPanel, BorderLayout.PAGE_START);
        jFrame.setContentPane(new JScrollPane(jPanel));
        jFrame.pack();
        jFrame.setVisible(true);
//        try {
//            String xmlString = "<results>"
//                    + "<relations>"
//                    + "<entity>"
//                    + "<type>ancestor</type>"
//                    + "<path>file:/Users/petwit/.arbil/ArbilWorkingFiles/a0d39c01f0e75d5364bfe643635aa48d/_F1_.cmdi</path>"
//                    + "</entity>"
//                    + "<entity>"
//                    + "<type>another ancestor</type>"
//                    + "<path>another path</path>"
//                    + "</entity>"
//                    + "</relations>"
//                    + "</results>";
//
//            StringReader xmlReader = new StringReader(xmlString);
//            StreamSource xmlSource = new StreamSource(xmlReader);
//            JAXBContext jaxbContext1 = JAXBContext.newInstance(RelationResults.class);
//            Unmarshaller unmarshaller1 = jaxbContext1.createUnmarshaller();
//            RelationResults data = unmarshaller1.unmarshal(xmlSource, RelationResults.class).getValue();
//            System.out.println(data.relationArray[0].path + " : " + data.relationArray[0].type);
//        } catch (JAXBException exception) {
//            System.out.println(exception.getMessage());
//            System.out.println(exception.getLocalizedMessage());
//            System.out.println(exception.getErrorCode());
//            System.out.println(exception.toString());
//            System.out.println(exception.getMessage());
//        }
    }
}
