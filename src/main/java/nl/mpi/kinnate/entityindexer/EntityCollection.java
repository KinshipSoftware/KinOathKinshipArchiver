package nl.mpi.kinnate.entityindexer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.LinorgBugCatcher;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.kinnate.svg.GraphDataNode;
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

    @XmlRootElement(name = "results")
    static public class RelationResults {

        @XmlElementWrapper(name = "relations")
        @XmlElement(name = "entity")
        RelationData[] relationArray;
    }

    static public class RelationData {

        @XmlElement
        GraphDataNode.RelationType type;
        @XmlElement
        String path;
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

    public RelationData[] getRelatedNodes(String uniqueIdentifier, IndexerParameters indexerParameters) {
        // todo: it would seem that entityPath is not going to be adequate because of resolved vs unresolved paths, it would seem best at this point to implement an ID or even persistent identifier if posible
        // there are two parts required to get all relations of an ego: check the ego entity for relations to others and then check the relations of all other entities for references to the ego entity
        // for now maybe use an md5 sum the full path url or something and put it into both the entity and linking entities
        String ancestorSequence = indexerParameters.ancestorFields.asSequenceString();
        String decendantSequence = indexerParameters.decendantFields.asSequenceString();

        //        uniqueIdentifier = "742243abdb2468b8df65f16ee562ac10";
        String query1String = "<results><relations>{"
                + "for $relationNode in collection('nl-mpi-kinnate')/Kinnate/Relation[UniqueIdentifier/. = \"" + uniqueIdentifier + "\"]\n"
                + "let $isAncestor := $relationNode/Type/text() = " + decendantSequence + "\n" // note that the ancestor and decentant are switched for alter compared to ego
                + "let $isDecendant := $relationNode/Type/text() = " + ancestorSequence + "\n"
                + "where $isAncestor or $isDecendant \n"
                + "return \n"
                + "<entity>{\n"
                + "if ($isAncestor)\n"
                + "then <type>ancestor</type>\n"
                + "else if ($isDecendant)\n"
                + "then <type>descendant</type>\n"
                + "else <type>none</type>,\n"
                // with the type value we are looking for one of GraphDataNode.RelationType: sibling, ancestor, descendant, union, none
                + "<path>{base-uri($relationNode)}</path>\n"
                + "}</entity>"
                + "}, {"
                // for $relationNode in collection('nl-mpi-kinnate')/Kinnate/(Gedcom|Relation|Entity)[UniqueIdentifier/. = "742243abdb2468b8df65f16ee562ac10"]
                + "for $relationNode in collection('nl-mpi-kinnate')/Kinnate/(Gedcom|Entity)[UniqueIdentifier/. = \"" + uniqueIdentifier + "\"]\n"
                + "let $isAncestor := $relationNode/Type/text() = " + ancestorSequence + "\n" // note that the ancestor and decentant are switched for alter compared to ego
                + "let $isDecendant := $relationNode/Type/text() = " + decendantSequence + "\n"
                + "where $isAncestor or $isDecendant \n"
                + "return \n"
                + "<entity>{\n"
                + "if ($isAncestor)\n"
                + "then <type>ancestor</type>\n"
                + "else if ($isDecendant)\n"
                + "then <type>descendant</type>\n"
                + "else <type>none</type>,\n"
                // with the type value we are looking for one of GraphDataNode.RelationType: sibling, ancestor, descendant, union, none
                + "<path>{base-uri($relationNode)}</path>\n"
                + "}</entity>"
                + "}</relations></results>\n";

        System.out.println("query1String: " + query1String);
        ArrayList<RelationData> resultsArray = new ArrayList<RelationData>();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(RelationResults.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String queryResult = new XQuery(query1String).execute(context);
            RelationResults relationResults = (RelationResults) unmarshaller.unmarshal(new StreamSource(new StringReader(queryResult)), RelationResults.class).getValue();
            resultsArray.addAll(Arrays.asList(relationResults.relationArray));
        } catch (JAXBException exception) {
            new LinorgBugCatcher().logError(exception);
        } catch (BaseXException exception) {
            new LinorgBugCatcher().logError(exception);
        }
        return resultsArray.toArray(new RelationData[]{});
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

    static public void main(String[] args) {
//        final EntityCollection entityCollection = new EntityCollection();
        JFrame jFrame = new JFrame("Test Query Window");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JTextArea queryText = new JTextArea();
        final JTextArea resultsText = new JTextArea();
        resultsText.setVisible(false);
        JButton jButton = new JButton("run query");
        jButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                resultsText.setText(queryText.getText() + "\n");
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
