package nl.mpi.kinnate.plugins.export.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import nl.mpi.flap.kinnate.entityindexer.QueryException;
import nl.mpi.flap.plugin.PluginBugCatcher;
import nl.mpi.flap.plugin.PluginDialogHandler;
import nl.mpi.flap.plugin.PluginDialogHandler.DialogueType;
import nl.mpi.flap.plugin.PluginException;
import nl.mpi.flap.plugin.PluginSessionStorage;
import nl.mpi.kinnate.entityindexer.CollectionExport;
import nl.mpi.kinnate.plugins.export.GedcomExport;

/**
 * Document : ExportPanel Created on : Jul 18, 2012, 5:37:51 PM
 *
 * @author Peter Withers
 */
public class ExportPanel extends JPanel implements ActionListener {

    PluginDialogHandler arbilWindowManager;
    GedcomExport gedcomExport;
    FieldsPanel fieldsPanel;
    JTabbedPane outerTabbedPane;

    public ExportPanel(PluginDialogHandler arbilWindowManager, GedcomExport gedcomExport, JTabbedPane jTabbedPane) {
        this.arbilWindowManager = arbilWindowManager;
        this.gedcomExport = gedcomExport;
        this.outerTabbedPane = jTabbedPane;
        fieldsPanel = new FieldsPanel(gedcomExport);
        fieldsPanel.populateFields();
        this.setLayout(new BorderLayout());
        this.add(fieldsPanel, BorderLayout.CENTER);
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(this);
        this.add(exportButton, BorderLayout.PAGE_END);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (fieldsPanel.namesAreUnique()) {
                final String queryString = gedcomExport.generateExport(fieldsPanel.getSelectedFieldNames(), fieldsPanel.getSelectedFieldPaths());
                outerTabbedPane.add("Query Text", new JScrollPane(new JTextArea(queryString)));
                SwingUtilities.invokeLater(new Thread() {
                    @Override
                    public void run() {


//                        jProgressBar.setIndeterminate(true);
//                resultsText.setVisible(true);
//                resultsText.setText("");
                        try {
                            long startTime = System.currentTimeMillis();
                            String resultsString = gedcomExport.generateExport(queryString);
                            long queryMils = System.currentTimeMillis() - startTime;
                            String queryTimeString = "Query time: " + queryMils + "ms";
//                    queryTimeLabel.setText(queryTimeString);
//                            ExportPanel.this.removeAll();
                            final ResultsPanel resultsPanel = new ResultsPanel();
//                        ExportPanel.this.add(new JScrollPane(new JTextArea(csvText)), BorderLayout.CENTER);
//                            ExportPanel.this.add(resultsPanel, BorderLayout.CENTER);
                            outerTabbedPane.add("Results Text", new JScrollPane(new JTextArea(resultsString)));
                            outerTabbedPane.add("Results Table", resultsPanel);
                            resultsPanel.updateTable(resultsString); // "\"a\",\"b\",\"c\"\n\",\"1\",\"2\",\"3\"");
                            ExportPanel.this.revalidate();
                            ExportPanel.this.repaint();

                        } catch (QueryException exception) {
//                    resultsText.append("Error: " + exception.getMessage() + "\n");
                            arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Generate Table");
                        }
//                jProgressBar.setIndeterminate(false);
//                runQueryButton.setEnabled(gedcomExport.databaseReady());
                    }
                });
            } else {
                arbilWindowManager.addMessageDialogToQueue("The chosen field names are not unique", "Export Error");
            }
        } catch (QueryException exception) {
            arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Export Error");
        }
    }

    static public void main(String[] args) {
        JFrame jFrame = new JFrame("Fields Panel Test");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        final PluginDialogHandler arbilWindowManager = new ArbilWindowManager();
//        final KinSessionStorage kinSessionStorage = new KinSessionStorage(new ApplicationVersionManager(new KinOathVersion()));
//        arbilWindowManager.setSessionStorage(kinSessionStorage);
        final CollectionExport entityCollection = new CollectionExport(new PluginBugCatcher() {
            public void logException(PluginException exception) {
                System.err.println(exception.getMessage());;
            }
        }, new PluginSessionStorage() {
            public File getApplicationSettingsDirectory() {
                return new File("/Users/petwit2/.arbil/");
            }

            public File getProjectDirectory() {
                return new File("/Users/petwit2/.arbil/");
            }

            public File getProjectWorkingDirectory() {
                return new File("/Users/petwit2/.arbil/ArbilWorkingFiles/");
            }
        });
        final GedcomExport gedcomExport = new GedcomExport(entityCollection);
        JTabbedPane jTabbedPane = new JTabbedPane();
        ExportPanel exportPanel = new ExportPanel(new PluginDialogHandler() {
            public void addMessageDialogToQueue(String messageString, String messageTitle) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean showConfirmDialogBox(String messageString, String messageTitle) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public int showDialogBox(String message, String title, int optionType, int messageType) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public int showDialogBox(String message, String title, int optionType, int messageType, Object[] options, Object initialValue) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public File[] showFileSelectBox(String titleText, boolean directorySelectOnly, boolean multipleSelect, Map<String, FileFilter> fileFilterMap, DialogueType dialogueType, JComponent customAccessory) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, gedcomExport, jTabbedPane);
        jTabbedPane.add("Import", exportPanel);
        jFrame.setContentPane(jTabbedPane);
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
