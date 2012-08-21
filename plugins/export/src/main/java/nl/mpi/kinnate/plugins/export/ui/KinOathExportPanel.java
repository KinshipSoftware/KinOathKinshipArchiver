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
import nl.mpi.arbil.plugin.PluginException;
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

    public KinOathExportPanel(PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage, final PluginBugCatcher bugCatcher) {
        super(new BorderLayout());
        this.arbilWindowManager = dialogHandler;
        this.bugCatcher = bugCatcher;
        this.setName("CMDI/IMDI/KMDI Export Tool");
        final JLabel queryTimeLabel = new JLabel();
        final CollectionExport entityCollection = new CollectionExport(bugCatcher, sessionStorage);
        final GedcomExport gedcomExport = new GedcomExport(entityCollection);
        final JProgressBar jProgressBar = new JProgressBar();
        final String browseOption = "<browse>";
        final JComboBox locationSelect = new JComboBox(new String[]{browseOption});
        File defaultKinOathDirectory = sessionStorage.getProjectDirectory();
        for (File currentFile : defaultKinOathDirectory.getParentFile().listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(".kinoath"); // || pathname.getName().startsWith(".arbil");
            }
        })) {
            locationSelect.addItem(currentFile.toString());
        }
        final JTextArea resultsText = new JTextArea();
        final JButton saveAsButton = new JButton("Save KinOath Export File");
        final JButton recreateButton = new JButton("Create Temporary Database");
        saveAsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveAsButton.setEnabled(false);
                recreateButton.setEnabled(false);
                locationSelect.setEnabled(false);
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
                    jProgressBar.setIndeterminate(true);
                    try {
                        // add the file suffix if not already there
                        if (!saveFile[0].getName().toLowerCase().endsWith(".kinoath")) {
                            saveFile[0] = new File(saveFile[0].getParentFile(), saveFile[0].getName() + ".kinoath");
                        }
                        resultsText.setText("Generating export contents.\n");
                        final String generateExportResult = gedcomExport.generateExport(gedcomExport.getGedcomQuery());
                        resultsText.setText("Creating export file: " + saveFile.toString() + "\n");
                        FileWriter fileWriter = new FileWriter(saveFile[0]);
                        fileWriter.write(generateExportResult);
                        fileWriter.close();
                        resultsText.setText("Export file complete.\n");
                        arbilWindowManager.addMessageDialogToQueue("Save Complete", "Save File");
                    } catch (IOException exception) {
                        resultsText.setText("Error Saving File.\n");
                        arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Error Saving File");
                        bugCatcher.logException(new PluginException(exception.getMessage()));
                    } catch (QueryException exception) {
                        resultsText.setText("Error Creating Export.\n");
                        arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Error Creating Export");
                        bugCatcher.logException(new PluginException(exception.getMessage()));
                    }
                } else {
                    resultsText.setText("Export file not valid, no export created.\n");
                }
                jProgressBar.setIndeterminate(false);
                saveAsButton.setEnabled(true);
                recreateButton.setEnabled(true);
                locationSelect.setEnabled(true);
            }
        });
        recreateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveAsButton.setEnabled(false);
                recreateButton.setEnabled(false);
                locationSelect.setEnabled(false);
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
                if (importDirectory != null) {
                    resultsText.setText("Dropping old temporary database\n");
                    resultsText.setText("Creating new temporary database for: " + importDirectory + "\n");
                    final File importDirectoryFinal = importDirectory;
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                gedcomExport.dropAndCreate(importDirectoryFinal, "*.kmdi");
                                resultsText.append("Completed cteating temporary database\n");
                            } catch (QueryException exception) {
                                resultsText.append("Error creating temporary database: " + exception.getMessage() + "\n");
                                arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Create Temporary Database");
                                bugCatcher.logException(new PluginException(exception.getMessage()));
                            }
                            jProgressBar.setIndeterminate(false);
                            saveAsButton.setEnabled(true);
                            recreateButton.setEnabled(true);
                            locationSelect.setEnabled(true);
                        }
                    }.start();
                } else {
                    resultsText.append("Invalid Import Directory" + "\n");
                    jProgressBar.setIndeterminate(false);
                    saveAsButton.setEnabled(true);
                    recreateButton.setEnabled(true);
                    locationSelect.setEnabled(true);
                }
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(locationSelect);
        buttonPanel.add(recreateButton);
        buttonPanel.add(saveAsButton);
        buttonPanel.add(queryTimeLabel);
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(buttonPanel, BorderLayout.PAGE_START);
        progressPanel.add(jProgressBar, BorderLayout.PAGE_END);
        this.add(new JScrollPane(resultsText), BorderLayout.CENTER);
        this.add(progressPanel, BorderLayout.PAGE_START);
    }
}
