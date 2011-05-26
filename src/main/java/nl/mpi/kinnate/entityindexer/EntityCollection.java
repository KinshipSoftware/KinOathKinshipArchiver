package nl.mpi.kinnate.entityindexer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.XQuery;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 *  Document   : EntityCollection
 *  Created on : Feb 15, 2011, 5:37:06 PM
 *  Author     : Peter Withers
 */
public class EntityCollection {

    private String databaseName = "nl-mpi-kinnate";
    static Context context = new Context();

    public class SearchResults {

        public String[] resultsPathArray;
        public String statusMessage;
        public int resultCount = 0;
    }

    public void createDatabase() {
        try {
            new DropDB(databaseName).execute(context);
            new Set("CREATEFILTER", "*.cmdi").execute(context);
            new CreateDB(databaseName, ArbilSessionStorage.getSingleInstance().getCacheDirectory().toString()).execute(context);
//            new Open(databaseName).execute(context);
//            new CreateIndex("text").execute(context); // TEXT|ATTRIBUTE|FULLTEXT|PATH
//            new CreateIndex("fulltext").execute(context);
//            new CreateIndex("attribute").execute(context);
//            new CreateIndex("path").execute(context);
//            new Close().execute(context);
//            context.close();
        } catch (BaseXException baseXException) {
            new ArbilBugCatcher().logError(baseXException);
        }
    }

    public void updateDatabase(URI updatedFile) {
        // todo: it would appear that a re adding a file does not remove the old entries so for now we will dump and recreate the entire database
        createDatabase();
//        try {
//            new Open(databaseName).execute(context);
////            new Delete(updatedFile.toString()).execute(context);
////            new Add(new File(updatedFile).toString()).execute(context);
//            new Add(updatedFile.toString()).execute(context);
//            new Optimize().execute(context);
//            new Close().execute(context);
//        } catch (BaseXException baseXException) {
//            new ArbilBugCatcher().logError(baseXException);
//        }
    }

    public SearchResults listGedcomFamIds() {
        String queryString = "distinct-values(collection('nl-mpi-kinnate')/*:Kinnate/*:Gedcom[*:Entity/*:GedcomType='FAM']/*:UniqueIdentifier//text())";
        return performQuery(queryString);
    }

    public SearchResults listAllRelationTypes() {
        // todo: use this to populate the InderParametersFormUI
        String queryString = "distinct-values(collection('nl-mpi-kinnate')/Kinnate/Relation/Type/text())";
        return performQuery(queryString);
    }

    public SearchResults searchByName(String namePartString) {
        String queryString = "for $doc in collection('nl-mpi-kinnate') where contains(string-join($doc//text()), \"" + namePartString + "\") return base-uri($doc)";
        return performQuery(queryString);
    }

    private SearchResults performQuery(String queryString) {
        SearchResults searchResults = new SearchResults();
        ArrayList<String> resultPaths = new ArrayList<String>();
        try {
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

            searchResults.statusMessage = "found " + searchResults.resultCount + " records";
        } catch (QueryException exception) {
            new ArbilBugCatcher().logError(exception);
            searchResults.statusMessage = exception.getMessage();
        } catch (IOException exception) {
            new ArbilBugCatcher().logError(exception);
            searchResults.statusMessage = exception.getMessage();
        }
        searchResults.resultsPathArray = resultPaths.toArray(new String[]{});
        searchResults.statusMessage = searchResults.statusMessage + "\n query: " + queryString;
        return searchResults;
    }

    public String[] getEntityIdByTerm(KinTypeStringConverter.KinTypeElement queryTerms) {
        // todo: add a query cache or determine that the xml database does the job of caching adequately
        QueryBuilder queryBuilder = new QueryBuilder();
        String queryString = queryBuilder.getTermQuery(queryTerms);
        System.out.println("query1String: " + queryString);
        String[] searchResults = new String[]{};
        try {
            searchResults = new XQuery(queryString).execute(context).split("\\|");
        } catch (BaseXException exception) {
            new ArbilBugCatcher().logError(exception);
        }
        for (String resultLine : searchResults) {
            System.out.println("resultLine: " + resultLine);
        }
        return searchResults;
    }

    public EntityData[] getEntityWithRelations(String uniqueIdentifier, String[] excludeUniqueIdentifiers, IndexerParameters indexParameters) {
        long startTime = System.currentTimeMillis();
        QueryBuilder queryBuilder = new QueryBuilder();
        String query1String = queryBuilder.getEntityWithRelationsQuery(uniqueIdentifier, excludeUniqueIdentifiers, indexParameters);
        System.out.println("query1String: " + query1String);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EntityData.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String queryResult = new XQuery(query1String).execute(context);
            System.out.println("queryResult: " + queryResult);
            EntityData[] selectedEntity = (EntityData[]) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), EntityData[].class).getValue();
            long queryMils = System.currentTimeMillis() - startTime;
            String queryTimeString = "Query time: " + queryMils + "ms for " + selectedEntity.length + " entities";
            System.out.println(queryTimeString);
//            selectedEntity.appendTempLabel(queryTimeString);
            return selectedEntity;
        } catch (JAXBException exception) {
            new ArbilBugCatcher().logError(exception);
        } catch (BaseXException exception) {
            new ArbilBugCatcher().logError(exception);
        }
        return new EntityData[]{}; //(uniqueIdentifier, null, "", EntityData.SymbolType.none, new String[]{"Error loading data", "view log for details"}, false);
    }

    public EntityData getEntity(String uniqueIdentifier, IndexerParameters indexParameters) {
        long startTime = System.currentTimeMillis();
        QueryBuilder queryBuilder = new QueryBuilder();
        String query1String = queryBuilder.getEntityQuery(uniqueIdentifier, indexParameters);
        System.out.println("query1String: " + query1String);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EntityData.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String queryResult = new XQuery(query1String).execute(context);
            System.out.println("queryResult: " + queryResult);
            EntityData selectedEntity = (EntityData) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), EntityData.class).getValue();
            long queryMils = System.currentTimeMillis() - startTime;
            String queryTimeString = "Query time: " + queryMils + "ms";
            System.out.println(queryTimeString);
//            selectedEntity.appendTempLabel(queryTimeString);
            return selectedEntity;
        } catch (JAXBException exception) {
            new ArbilBugCatcher().logError(exception);
        } catch (BaseXException exception) {
            new ArbilBugCatcher().logError(exception);
        }
        return new EntityData(uniqueIdentifier, null, "", EntityData.SymbolType.none, new String[]{"Error loading data", "view log for details"}, false);
    }

    static public void main(String[] args) {
        JFrame jFrame = new JFrame("Test Query Window");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JTextArea queryText = new JTextArea();
        final JLabel queryTimeLabel = new JLabel();
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
                }
//                SearchResults results = entityCollection.performQuery(queryText.getText());
//                for (String resultLine : results.resultsPathArray) {
//                    resultsText.append(resultLine + "\n");
//                }
                resultsText.setVisible(true);
            }
        });
        JButton updateButton = new JButton("update file");
        updateButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                resultsText.setText("");
                try {
                    new EntityCollection().updateDatabase(new URI("file:/Users/petwit/.arbil/ArbilWorkingFiles/ca1641fc8828f9edb295d1e7b3d37405/_PARENTS_.cmdi"));
                } catch (URISyntaxException exception) {
                    resultsText.append(exception.getMessage());
                }
                resultsText.setVisible(true);
            }
        });
        JButton recreateButton = new JButton("recreate database");
        recreateButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                resultsText.setText("");
//                try {
                new EntityCollection().createDatabase();
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
        buttonPanel.add(updateButton);
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
