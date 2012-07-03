package nl.mpi.kinnate.export;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.KinOathVersion;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.userstorage.KinSessionStorage;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.XQuery;

/**
 * Document : GedcomExport
 * Created on : Jul 3, 2012, 1:16:57 PM
 * Author : Peter Withers
 */
public class GedcomExport {

    private String getHeader() {
        return "let $headerString := \"0 HEAD\n"
                + "1 SOUR Reunion\n"
                + "2 VERS V8.0\n"
                + "2 CORP Leister Productions\n"
                + "1 DEST Reunion\n"
                + "1 DATE 11 FEB 2006\n"
                + "1 FILE test\n"
                + "1 GEDC\n"
                + "2 VERS 5.5\n"
                + "1 CHAR MACINTOSH\n\"";
    }

    private String getIndividual() {
        return "string(\"0 @\"),"
                + "{$entityNode/*:Entity/*:UniqueIdentifier//text()},"
                + "string(\"@ INDI\n\")"
                + "1 NAME Bobby Jo /Cox/\n"
                + "1 SEX M\n"
                + "1 FAMC @F1@\n"
                + "1 CHAN\n"
                + "2 DATE 11 FEB 2006\n";
    }

    private String getCvsQuery() {
        // todo: add quotes to the header line
        return "let $colNames := distinct-values(collection('nl-mpi-kinnate')/*:Kinnate/*:CustomData/*//local-name())\n"
                // todo: need to handle unknown number of description fields etc
                // todo: this might now be handling sub nodes correctly 
                + "let $fileHeader := concat(string-join($colNames, \",\"), \"&#10;\")\n"
                + "let $fileLines :=\n"
                + "for $entityNode in collection('nl-mpi-kinnate')/*:Kinnate\n"
                + "let $lineString := \n"
                + "for $column in $colNames\n"
                // todo: need to escape quotes in the data
                + "return concat(\'\"\', $entityNode/*:CustomData/*[local-name()=$column]/text(), '\"')\n"
                + "return string-join($lineString, \",\")\n"
                + "let $fileBody := string-join($fileLines, \"&#10;\")\n"
                + "return concat($fileHeader, $fileBody)\n";
    }
    // todo: this context probalby should be static and only declaired in "EntityCollection"
    static Context context = new Context();

    static public void main(String[] args) {
        JFrame jFrame = new JFrame("Test Query Window");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JTextArea queryText = new JTextArea();
        final JLabel queryTimeLabel = new JLabel();
        final ArbilWindowManager arbilWindowManager = new ArbilWindowManager();
        final KinSessionStorage kinSessionStorage = new KinSessionStorage(new ApplicationVersionManager(new KinOathVersion()));
        final EntityCollection entityCollection = new EntityCollection(kinSessionStorage, arbilWindowManager);
        final GedcomExport gedcomExport = new GedcomExport();
        //queryText.setText(new QueryBuilder().getEntityQuery("e4dfbd92d311088bf692211ced5179e5", new IndexerParameters()));
//        queryText.setText(new QueryBuilder().getRelationQuery("e4dfbd92d311088bf692211ced5179e5", new IndexerParameters()));
//        queryText.setText(new QueryBuilder().getEntityQuery("e4dfbd92d311088bf692211ced5179e5", new IndexerParameters()));
//        queryText.setText(new QueryBuilder().getEntityWithRelationsQuery("e4dfbd92d311088bf692211ced5179e5", new String[]{"e4dfbd92d311088bf692211ced5179e5"}, new IndexerParameters()));
        queryText.setText(gedcomExport.getCvsQuery());
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

        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.add(queryText, BorderLayout.CENTER);
        jPanel.add(resultsText, BorderLayout.PAGE_END);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(jButton);
        buttonPanel.add(queryTimeLabel);
        jPanel.add(buttonPanel, BorderLayout.PAGE_START);
        jFrame.setContentPane(new JScrollPane(jPanel));
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
