package nl.mpi.kinnate.plugins.export.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.kinnate.entityindexer.CollectionExport;
import nl.mpi.kinnate.entityindexer.QueryException;
import nl.mpi.kinnate.plugins.export.GedcomExport;

/**
 * Document : ExportPanel
 * Created on : Jul 18, 2012, 5:37:51 PM
 * Author : Peter Withers
 */
public class ExportPanel extends JPanel implements ActionListener {

    ArbilWindowManager arbilWindowManager;
    GedcomExport gedcomExport;
    FieldsPanel fieldsPanel;
    JTabbedPane outerTabbedPane;

    public ExportPanel(ArbilWindowManager arbilWindowManager, GedcomExport gedcomExport, JTabbedPane jTabbedPane) {
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
        final ArbilWindowManager arbilWindowManager = new ArbilWindowManager();
//        final KinSessionStorage kinSessionStorage = new KinSessionStorage(new ApplicationVersionManager(new KinOathVersion()));
//        arbilWindowManager.setSessionStorage(kinSessionStorage);
        final CollectionExport entityCollection = new CollectionExport();
        final GedcomExport gedcomExport = new GedcomExport(entityCollection);
        JTabbedPane jTabbedPane = new JTabbedPane();
        ExportPanel exportPanel = new ExportPanel(arbilWindowManager, gedcomExport, jTabbedPane);
        jTabbedPane.add("Import", exportPanel);
        jFrame.setContentPane(jTabbedPane);
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
