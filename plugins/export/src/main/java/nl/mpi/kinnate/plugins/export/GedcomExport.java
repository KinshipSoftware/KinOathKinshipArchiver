package nl.mpi.kinnate.plugins.export;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.KinOathVersion;
import nl.mpi.kinnate.entityindexer.CollectionExport;
import nl.mpi.kinnate.entityindexer.QueryException;
import nl.mpi.kinnate.userstorage.KinSessionStorage;

/**
 * Document : GedcomExport
 * Created on : Jul 3, 2012, 1:16:57 PM
 * Author : Peter Withers
 */
public class GedcomExport {

    private CollectionExport entityCollection;

    public GedcomExport(CollectionExport entityCollection) {
        this.entityCollection = entityCollection;
    }

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
        return "let $colNames := distinct-values(collection('" + entityCollection.getDatabaseName() + "')/*:Kinnate/*:CustomData/*//local-name())\n" // todo: get the xpath not the node name
                // todo: need to handle unknown number of description fields etc
                // todo: this might now be handling sub nodes correctly 
                + "let $fileHeader := concat(string-join($colNames, \",\"), \"&#10;\")\n"
                + "let $fileLines :=\n"
                + "for $entityNode in collection('" + entityCollection.getDatabaseName() + "')/*:Kinnate\n"
                + "let $lineString := \n"
                + "for $column in $colNames\n"
                // todo: need to escape quotes in the data
                + "return concat(\'\"\', $entityNode/*:CustomData/*[local-name()=$column]/text(), '\"')\n"
                + "return string-join($lineString, \",\")\n"
                + "let $fileBody := string-join($fileLines, \"&#10;\")\n"
                + "return concat($fileHeader, $fileBody)\n";
    }

    public String getCsvDemoQuery() {
        return "let $colNames := collection('SimpleExportTemp')[1]\n"
                + "return  $colNames\n";
    }

    public String getHeaderQuery() {
        return "let $colNames := distinct-values(collection('" + entityCollection.getDatabaseName() + "')/*:Kinnate/*:CustomData/*/name())\n"
                + "return  $colNames\n";
    }

    public String getXPaths() {
        return "for $elementName in collection('" + entityCollection.getDatabaseName() + "')/*:Kinnate/*:CustomData\n"
                + "return fn:path($elementName)\n";
    }

    public String dropAndImportCsv(File importDirectory, String fileFilter) throws QueryException {
        return entityCollection.dropAndImportCsv(importDirectory, fileFilter);
    }

    public void dropAndCreate(File importDirectory, String fileFilter) throws QueryException {
        entityCollection.createExportDatabase(importDirectory, fileFilter);
    }

    public String generateExport(String exportQuery) throws QueryException {
        return entityCollection.performExportQuery(exportQuery);
    }

    public boolean databaseReady() {
//        throw new UnsupportedOperationException();
        return true;
    }

    static public void main(String[] args) {
        JFrame jFrame = new JFrame("CMDI/IMDI/KMDI Export Tool");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JTextArea queryText = new JTextArea();
        final JLabel queryTimeLabel = new JLabel();
        final ArbilWindowManager arbilWindowManager = new ArbilWindowManager();
        final KinSessionStorage kinSessionStorage = new KinSessionStorage(new ApplicationVersionManager(new KinOathVersion()));
        arbilWindowManager.setSessionStorage(kinSessionStorage);
        final CollectionExport entityCollection = new CollectionExport();
        final GedcomExport gedcomExport = new GedcomExport(entityCollection);
        final JProgressBar jProgressBar = new JProgressBar();
        //queryText.setText(new QueryBuilder().getEntityQuery("e4dfbd92d311088bf692211ced5179e5", new IndexerParameters()));
//        queryText.setText(new QueryBuilder().getRelationQuery("e4dfbd92d311088bf692211ced5179e5", new IndexerParameters()));
//        queryText.setText(new QueryBuilder().getEntityQuery("e4dfbd92d311088bf692211ced5179e5", new IndexerParameters()));
//        queryText.setText(new QueryBuilder().getEntityWithRelationsQuery("e4dfbd92d311088bf692211ced5179e5", new String[]{"e4dfbd92d311088bf692211ced5179e5"}, new IndexerParameters()));
        final String csvOption = "*.csv";
        final JComboBox formatSelect = new JComboBox(new String[]{"*.cmdi", "*.imdi", "*.kmdi", csvOption});
        final String browseOption = "<browse>";
        final JComboBox locationSelect = new JComboBox(new String[]{browseOption});

        File defaultArbilDirectory = new ArbilSessionStorage().getStorageDirectory();
        locationSelect.addItem(defaultArbilDirectory.toString());
        File defaultKinOathDirectory = new ArbilSessionStorage().getStorageDirectory();
        for (File currentFile : defaultKinOathDirectory.getParentFile().listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.getName().startsWith(".kinoath");
            }
        })) {
            locationSelect.addItem(currentFile.toString());
        }
        queryText.setText(gedcomExport.getHeaderQuery());
        final JTextArea resultsText = new JTextArea();
        resultsText.setVisible(false);
        final JButton runQueryButton = new JButton("run query");
        runQueryButton.setEnabled(gedcomExport.databaseReady());
        runQueryButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jProgressBar.setIndeterminate(true);
                resultsText.setVisible(true);
                resultsText.setText("");
                try {
                    long startTime = System.currentTimeMillis();
                    resultsText.append(gedcomExport.generateExport(queryText.getText()) + "\n");
                    long queryMils = System.currentTimeMillis() - startTime;
                    String queryTimeString = "Query time: " + queryMils + "ms";
                    queryTimeLabel.setText(queryTimeString);
                } catch (QueryException exception) {
                    resultsText.append("Error: " + exception.getMessage() + "\n");
                    arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), runQueryButton.getText());
                }
                jProgressBar.setIndeterminate(false);
                runQueryButton.setEnabled(gedcomExport.databaseReady());
            }
        });

        JButton recreateButton = new JButton("Create Database");
        recreateButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                runQueryButton.setEnabled(false);
                File importDirectory = null;
                if (!locationSelect.getSelectedItem().toString().equals(browseOption)) {
                    importDirectory = new File(locationSelect.getSelectedItem().toString());
                } else {
                    File[] importDirectoryArray = arbilWindowManager.showFileSelectBox("Select Import Directory", true, false, null, MessageDialogHandler.DialogueType.open, null/* formatSelect */);
                    if (importDirectoryArray != null && importDirectoryArray.length > 0) {
                        importDirectory = importDirectoryArray[0];
                    }
                }
                jProgressBar.setIndeterminate(true);
                resultsText.setVisible(true);
                if (importDirectory != null) {
                    resultsText.setText("recreating database for: " + importDirectory + "\n");
                    final File importDirectoryFinal = importDirectory;
                    new Thread() {

                        public void run() {
                            try {
                                final Object selectedItem = formatSelect.getSelectedItem();
                                if (csvOption.equals(selectedItem)) {
                                    queryText.setText(gedcomExport.getCsvDemoQuery());
                                    resultsText.append(gedcomExport.dropAndImportCsv(importDirectoryFinal, selectedItem.toString()));
                                } else {
                                    gedcomExport.dropAndCreate(importDirectoryFinal, selectedItem.toString());
                                }
                                resultsText.append("done\n");
                            } catch (QueryException exception) {
                                resultsText.append("Error: " + exception.getMessage() + "\n");
                                arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), runQueryButton.getText());
                            }
                            jProgressBar.setIndeterminate(false);
                            runQueryButton.setEnabled(gedcomExport.databaseReady());
                        }
                    }.start();
                } else {
                    resultsText.append("Invalid Import Directory" + "\n");
                    jProgressBar.setIndeterminate(false);
                    runQueryButton.setEnabled(false);
                }
            }
        });

        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.add(queryText, BorderLayout.PAGE_END);
        jPanel.add(new JScrollPane(resultsText), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(locationSelect);
        buttonPanel.add(formatSelect);
        buttonPanel.add(recreateButton);
        buttonPanel.add(runQueryButton);
        buttonPanel.add(queryTimeLabel);
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(buttonPanel, BorderLayout.PAGE_START);
        progressPanel.add(jProgressBar, BorderLayout.PAGE_END);
        jPanel.add(progressPanel, BorderLayout.PAGE_START);
        jFrame.setContentPane(jPanel);
        jFrame.pack();
        jFrame.setVisible(true);
    }
}