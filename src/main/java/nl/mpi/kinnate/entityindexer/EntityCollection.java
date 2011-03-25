package nl.mpi.kinnate.entityindexer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.LinorgBugCatcher;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter.KinType;
import nl.mpi.kinnate.kindata.EntityData;
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
public class EntityCollection implements EntityService {

    private String databaseName = "nl-mpi-kinnate";
    static Context context = new Context();

    public EntityCollection() {
    }

    public class SearchResults {

        public String[] resultsPathArray;
        public String statusMessage;
        public int resultCount = 0;
    }

    public void createDatabase() {
        try {
            new DropDB(databaseName).execute(context);
            new Set("CREATEFILTER", "*.cmdi").execute(context);
            new CreateDB(databaseName, LinorgSessionStorage.getSingleInstance().getCacheDirectory().toString()).execute(context);
//            context.close();
        } catch (BaseXException baseXException) {
            GuiHelper.linorgBugCatcher.logError(baseXException);
        }
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
            new LinorgBugCatcher().logError(exception);
            searchResults.statusMessage = exception.getMessage();
        } catch (IOException exception) {
            new LinorgBugCatcher().logError(exception);
            searchResults.statusMessage = exception.getMessage();
        }
        searchResults.resultsPathArray = resultPaths.toArray(new String[]{});
        searchResults.statusMessage = searchResults.statusMessage + "\n query: " + queryString;
        return searchResults;
    }

    public EntityData getEntity(String uniqueIdentifier, IndexerParameters indexParameters) {
        QueryBuilder queryBuilder = new QueryBuilder();
        String query1String = queryBuilder.getEntityQuery(uniqueIdentifier, indexParameters);
        System.out.println("query1String: " + query1String);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EntityData.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String queryResult = new XQuery(query1String).execute(context);
            System.out.println("queryResult: " + queryResult);
            EntityData selectedEntity = (EntityData) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), EntityData.class).getValue();
            return selectedEntity;
        } catch (JAXBException exception) {
            new LinorgBugCatcher().logError(exception);
        } catch (BaseXException exception) {
            new LinorgBugCatcher().logError(exception);
        }
        return null;
    }

    private void getNextRelations(HashMap<String, EntityData> createdGraphNodes, EntityData egoNode, ArrayList<KinType> remainingKinTypes, IndexerParameters indexParameters) {
        KinType currentKinType = remainingKinTypes.remove(0);
        for (EntityData.EntityRelation entityRelation : egoNode.getAllRelateNodes()) {
            EntityData alterNode;
            if (createdGraphNodes.containsKey(entityRelation.alterUniqueIdentifier)) {
                alterNode = createdGraphNodes.get(entityRelation.alterUniqueIdentifier);
            } else {
                alterNode = getEntity(entityRelation.alterUniqueIdentifier, indexParameters);
                createdGraphNodes.put(entityRelation.alterUniqueIdentifier, alterNode);
            }
            alterNode.isVisible = true;

            if (egoNode.relationMatchesType(entityRelation, currentKinType)) {
                // only traverse if the type matches
                if (remainingKinTypes.size() > 0) {
                    getNextRelations(createdGraphNodes, alterNode, remainingKinTypes, indexParameters);
                }
            }
        }
    }

    public EntityData[] getRelationsOfEgo(URI[] egoNodes, String[] uniqueIdentifiers, String[] kinTypeStrings, IndexerParameters indexParameters) throws EntityServiceException {
        KinTypeStringConverter kinTypeStringConverter = new KinTypeStringConverter();
        HashMap<String, EntityData> createdGraphNodes = new HashMap<String, EntityData>();
        for (String currentEgoId : uniqueIdentifiers) {
            EntityData egoNode;
            if (createdGraphNodes.containsKey(currentEgoId)) {
                egoNode = createdGraphNodes.get(currentEgoId);
            } else {
                egoNode = getEntity(currentEgoId, indexParameters);
                createdGraphNodes.put(currentEgoId, egoNode);
            }
            egoNode.isEgo = true;
            egoNode.isVisible = true;
            if (kinTypeStrings != null) {
                for (String currentKinString : kinTypeStrings) {
                    ArrayList<KinType> kinTypes = kinTypeStringConverter.getKinTypes(currentKinString);
                    getNextRelations(createdGraphNodes, egoNode, kinTypes, indexParameters);
                }
            }
            // set the alter node object from the unique identifier
            for (EntityData graphDataNode : createdGraphNodes.values()) {
                for (EntityData.EntityRelation nodeRelation : graphDataNode.getAllRelateNodes()) {
                    nodeRelation.setAlterNode(createdGraphNodes.get(nodeRelation.alterUniqueIdentifier));
                }
            }
        }
        return createdGraphNodes.values().toArray(new EntityData[]{});



//        ArrayList<GraphDataNode> graphDataNodes = new ArrayList<GraphDataNode>();
//        for (String entityIdentifier : uniqueIdentifiers) {
//            graphDataNodes.add(getEntity(entityIdentifier, indexParameters));
//        }
//        // todo: process the kin type strings
//        return graphDataNodes.toArray(new GraphDataNode[]{});
    }

    static public void main(String[] args) {
        JFrame jFrame = new JFrame("Test Query Window");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JTextArea queryText = new JTextArea();
        final JTextArea resultsText = new JTextArea();
        resultsText.setVisible(false);
        JButton jButton = new JButton("run query");
        jButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                resultsText.setText("");
                try {
                    resultsText.append(new XQuery(queryText.getText()).execute(context));
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
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.add(queryText, BorderLayout.CENTER);
        jPanel.add(resultsText, BorderLayout.PAGE_END);
        jPanel.add(jButton, BorderLayout.PAGE_START);
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
