package nl.mpi.kinnate.plugins.export.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

    public ExportPanel(ArbilWindowManager arbilWindowManager, GedcomExport gedcomExport) {
        this.arbilWindowManager = arbilWindowManager;
        this.gedcomExport = gedcomExport;
        fieldsPanel = new FieldsPanel(gedcomExport);
        fieldsPanel.populateFields();
        this.add(fieldsPanel, BorderLayout.CENTER);
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(this);
        this.add(exportButton, BorderLayout.PAGE_END);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (fieldsPanel.namesAreUnique()) {
                final String csvText = gedcomExport.generateExport(fieldsPanel.getSelectedFieldNames(), fieldsPanel.getSelectedFieldPaths());
                SwingUtilities.invokeLater(new Thread() {

                    @Override
                    public void run() {
                        ExportPanel.this.removeAll();
                        ExportPanel.this.add(new JScrollPane(new JTextArea(csvText)), BorderLayout.CENTER);
                        ExportPanel.this.revalidate();
                        ExportPanel.this.repaint();
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
        ExportPanel exportPanel = new ExportPanel(arbilWindowManager, gedcomExport);
        jFrame.setContentPane(exportPanel);
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
