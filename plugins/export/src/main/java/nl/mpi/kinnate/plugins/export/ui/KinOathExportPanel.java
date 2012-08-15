package nl.mpi.kinnate.plugins.export.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import nl.mpi.arbil.plugin.PluginBugCatcher;
import nl.mpi.arbil.plugin.PluginDialogHandler;
import nl.mpi.arbil.plugin.PluginSessionStorage;
import nl.mpi.kinnate.entityindexer.CollectionExport;
import nl.mpi.kinnate.entityindexer.QueryException;
import nl.mpi.kinnate.plugins.export.GedcomExport;

/**
 * Document : KinOathExportPanel <br> Created on Aug 15, 2012, 11:52:00 AM <br>
 *
 * @author Peter Withers <br>
 */
public class KinOathExportPanel extends JPanel {

    final PluginDialogHandler arbilWindowManager;
    final PluginBugCatcher bugCatcher;
//final KinSessionStorage kinSessionStorage;

    public KinOathExportPanel(PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage, PluginBugCatcher bugCatcher) {
        super(new BorderLayout());
        this.arbilWindowManager = dialogHandler;
        this.bugCatcher = bugCatcher;
//        this.kinSessionStorage=kinSessionStorage;
        this.setName("CMDI/IMDI/KMDI Export Tool");
        final JTextArea queryText = new JTextArea();
        final JLabel queryTimeLabel = new JLabel();
        final CollectionExport entityCollection = new CollectionExport(bugCatcher);
        final GedcomExport gedcomExport = new GedcomExport(entityCollection);
        final JProgressBar jProgressBar = new JProgressBar();
        final String csvOption = "*.csv";
        final JComboBox formatSelect = new JComboBox(new String[]{"*.cmdi", "*.imdi", "*.kmdi", csvOption});
        final String browseOption = "<browse>";
        final JComboBox locationSelect = new JComboBox(new String[]{browseOption});

        // get the default Arbil storage directory
//        File defaultArbilDirectory = new ArbilSessionStorage().getStorageDirectory();
//        locationSelect.addItem(defaultArbilDirectory.toString());
        File defaultKinOathDirectory = sessionStorage.getStorageDirectory();
        for (File currentFile : defaultKinOathDirectory.getParentFile().listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(".kinoath");
            }
        })) {
            locationSelect.addItem(currentFile.toString());
        }
        queryText.setText(gedcomExport.getGedcomQuery());
        final JTextArea resultsText = new JTextArea();
        resultsText.setVisible(false);
        final JButton runQueryButton = new JButton("run query");
        final JButton saveAsButton = new JButton("Save KinOath Export File");
//        saveAsButton.setEnabled(false);
        saveAsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                HashMap<String, javax.swing.filechooser.FileFilter> fileFilterMap = new HashMap<String, javax.swing.filechooser.FileFilter>(2);
                fileFilterMap.put("KinOath Export", new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File selectedFile) {
                        if (selectedFile.isDirectory()) {
                            return true;
                        }
                        final String currentFileName = selectedFile.getName().toLowerCase();
                        if (currentFileName.endsWith(".kinoath")) {
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public String getDescription() {
                        return "KinOath Kinship Data";
                    }
                });
                File[] saveFile = arbilWindowManager.showFileSelectBox("Save KinOath Export File", false, false, fileFilterMap, PluginDialogHandler.DialogueType.save, null);
                if (saveFile != null) {
                    try {
                        final String generateExportResult = gedcomExport.generateExport(gedcomExport.getGedcomQuery());
                        FileWriter fileWriter = new FileWriter(saveFile[0]);
                        fileWriter.write(generateExportResult);
                        fileWriter.close();
                        arbilWindowManager.addMessageDialogToQueue("Save Complete", "Save File");
                    } catch (IOException exception) {
                        arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Error Saving File");
                    } catch (QueryException exception) {
                        arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Error Creating Export");
                    }
                }
            }
        });

        runQueryButton.setEnabled(gedcomExport.databaseReady());
        runQueryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jProgressBar.setIndeterminate(true);
                resultsText.setVisible(true);
                resultsText.setText("");
                try {
                    long startTime = System.currentTimeMillis();
                    final String generateExportResult = gedcomExport.generateExport(queryText.getText());
                    resultsText.append(generateExportResult + "\n");
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
                    File[] importDirectoryArray = arbilWindowManager.showFileSelectBox("Select Import Directory", true, false, null, PluginDialogHandler.DialogueType.open, null/* formatSelect */);
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
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(locationSelect);
        buttonPanel.add(formatSelect);
        buttonPanel.add(recreateButton);
        buttonPanel.add(runQueryButton);
        buttonPanel.add(saveAsButton);
        buttonPanel.add(queryTimeLabel);
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(buttonPanel, BorderLayout.PAGE_START);
        progressPanel.add(jProgressBar, BorderLayout.PAGE_END);
        this.add(queryText, BorderLayout.PAGE_END);
        this.add(new JScrollPane(resultsText), BorderLayout.CENTER);
        this.add(progressPanel, BorderLayout.PAGE_START);
    }
}
