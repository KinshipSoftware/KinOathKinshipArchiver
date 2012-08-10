package nl.mpi.kinnate.plugins.export;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Date;
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
        final KinOathVersion kinOathVersion = new KinOathVersion();
        return "let $headerString := \"0 HEAD\n"
                + "1 KinOathVersion " + kinOathVersion.currentMajor + "." + kinOathVersion.currentMinor + "." + kinOathVersion.currentRevision + "\n"
                + "1 ExportDate " + new Date().toString() + "\n\"\n";
    }

    private String getQueryFunctions() {
        return "declare function local:getSubFields($levelCounter, $currentNode) {\n"
                //                + "if (string-join($currentNode/descendant-or-self::*/text()) then "
                + "let $result := concat($levelCounter, \" \", $currentNode/name(), \" \",$currentNode/text(),\"\n\",\n"
                + "string-join("
                + "for $subNode in $currentNode/*\n"
                + " return local:getSubFields($levelCounter + 1, $subNode)))\nreturn $result};\n";
    }

    private String getRelationFields() {
        return "string-join(for $relationNode in $kinnateNode/*:Entity/*:Relations/*"
                + "return concat("
                + "\"1 \",\n"
                + "$relationNode/@*:Type/string(),\n"
                + "\":\","
                + "$relationNode/@*:CustomType/string(),"
                + "\":\","
                + "$relationNode/@*:dcr/string(),\n"
                + "\" @\","
                + "$relationNode/*:Identifier/@*:type/string(),\n"
                + "\"_\","
                + "$relationNode/*:Identifier/text(),"
                + "\"@\n\"\n"
                + "))\n";
    }

    private String getEntityFields() {
        return "string-join(local:getSubFields(1, $kinnateNode/*:CustomData))\n";
    }

    private String getIndividual() {
        return "return concat(\"0 @\","
                + "$kinnateNode/*:Entity/*:Identifier//text(),"
                + "\"@ INDI\n\",\n"
                + getEntityFields()
                + ",\n"
                + getRelationFields()
                + ")\n";
    }

    private String getGedcomQuery() {
        return getQueryFunctions() + getHeader()
                + "let $entitySection := for $kinnateNode in collection('nl-mpi-kinnate')/*:Kinnate\n"
                + getIndividual()
                + "return concat($headerString, string-join($entitySection))\n";
    }

    private String getCvsQuery() {
        // todo: add quotes to the header line
        // for $entityNode in collection('nl-mpi-kinnate')/*:Kinnate/*:Entity[*:Identifier/text() = "8719d4e8c7f5f343aa17007ee4ad4c59"]/* return <a>{path($entityNode)}</a>

        // for $entityNode in collection('nl-mpi-kinnate')/*:Kinnate[*:Entity/*:Identifier/text() = "8719d4e8c7f5f343aa17007ee4ad4c59"]/* return <a>{distinct-values($entityNode/descendant-or-self::*/local-name(.))}</a>

        /*
         * for $xpathString in
         * for $entityNode in collection('nl-mpi-kinnate')/*:Kinnate/*:Entity[*:Identifier/text() = "8719d4e8c7f5f343aa17007ee4ad4c59"]/descendant-or-self::*
         * return path($entityNode)
         * return
         * <a>{$xpathString}</a>
         */


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
//        return "let $colNames := distinct-values(collection('" + entityCollection.getDatabaseName() + "')/*:Kinnate/*:CustomData/*/name())\n"
//                + "return  $colNames\n";
        return "for $xpathString in distinct-values(\n"
                // + "for $entityNode in collection('nl-mpi-kinnate')/*:Kinnate/*:Entity[*:Identifier/text() != '']/descendant-or-self::*\n"
                + "for $entityNode in collection('nl-mpi-kinnate')/*:Kinnate/*:CustomData/descendant-or-self::*[text() != '']\n"
                + "return path($entityNode)\n"
                + ")\n"
                + "return\n"
                + "$xpathString";
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

    public String generateExport(String[] selectedFieldNames, String[] selectedFieldPath) throws QueryException {
        // todo: need to add kin id and relations including custom types
        // todo: for imdi and cmdi the attributes need to also be included
        StringBuilder stringBuilder = new StringBuilder();
        for (String currentHeader : selectedFieldNames) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("\",\"");
            } else {
                stringBuilder.append("let $headerString := '\"");
            }
            stringBuilder.append(currentHeader);
        }
        stringBuilder.append("\"\n'\n");
        stringBuilder.append("let $bodyString := for $documentNode in collection('");
        stringBuilder.append(entityCollection.getDatabaseName());
//        stringBuilder.append("')/*:Kinnate/*:CustomData\nreturn\n");
        stringBuilder.append("')\nreturn\nconcat(\n'\"',\n");
        boolean firstColumn = true;
        for (String currentField : selectedFieldPath) {
            if (firstColumn) {
                firstColumn = false;
            } else {
                stringBuilder.append(",'\",\"',\n");
            }
            stringBuilder.append("$documentNode");
            stringBuilder.append(currentField);
            stringBuilder.append("/text()");
        }
        stringBuilder.append(",'\"')\n");
        stringBuilder.append("return concat($headerString, string-join($bodyString,\"\n\"))\n");
//        return entityCollection.performExportQuery(stringBuilder.toString());
        return stringBuilder.toString();
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
//        queryText.setText(gedcomExport.getHeaderQuery());
        queryText.setText(gedcomExport.getGedcomQuery());
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
