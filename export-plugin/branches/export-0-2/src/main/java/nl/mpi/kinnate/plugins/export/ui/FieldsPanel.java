package nl.mpi.kinnate.plugins.export.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import nl.mpi.kinnate.entityindexer.CollectionExport;
import nl.mpi.kinnate.entityindexer.QueryException;
import nl.mpi.kinnate.plugins.export.GedcomExport;

/**
 * Document : FieldsPanel
 * Created on : Jul 18, 2012, 4:09:37 PM
 * Author : Peter Withers
 */
public class FieldsPanel extends JPanel {

    GedcomExport gedcomExport;
    final JLabel queryTimeLabel = new JLabel();
    private String[] selectedFieldNames;
    private String[] selectedFieldPaths;
    private JTextArea[] fieldTextAreas;
    private Color defaultTextBackground;

    public FieldsPanel(GedcomExport gedcomExport) {
        this.gedcomExport = gedcomExport;
        this.setLayout(new BorderLayout());
        this.add(queryTimeLabel, BorderLayout.PAGE_END);
    }

    public String[] getSelectedFieldNames() {
        return selectedFieldNames;
    }

    public String[] getSelectedFieldPaths() {
        return selectedFieldPaths;
    }

    public void populateFields() {
        queryTimeLabel.setText("getting fields list...");
        String queryString = gedcomExport.getHeaderQuery();
        try {
            long startTime = System.currentTimeMillis();
            String queryResult = gedcomExport.generateExport(queryString);
            long queryMils = System.currentTimeMillis() - startTime;
            String queryTimeString = "Query time: " + queryMils + "ms";
            queryTimeLabel.setText(queryTimeString);
            String queryResultTidy = queryResult.replaceAll("\"[^\"]*\":", "*:");
            final String[] splitResults = queryResultTidy.split(" ");
            JPanel fieldsPanel = new JPanel(new GridLayout(splitResults.length, 2, 2, 2));
            selectedFieldNames = new String[splitResults.length];
            selectedFieldPaths = new String[splitResults.length];
            fieldTextAreas = new JTextArea[splitResults.length];
            for (int currentIndex = 0; currentIndex < splitResults.length; currentIndex++) {
                String currentField = splitResults[currentIndex];
                selectedFieldNames[currentIndex] = currentField.replaceAll("^.*:", "").replaceFirst("\\[\\d*\\]$", "");
                selectedFieldPaths[currentIndex] = currentField;
                fieldsPanel.add(new JLabel(selectedFieldPaths[currentIndex]));
                fieldTextAreas[currentIndex] = new JTextArea(selectedFieldNames[currentIndex]);
                fieldTextAreas[currentIndex].getDocument().addDocumentListener(new DocumentListener() {

                    public void changedUpdate(DocumentEvent e) {
                        namesAreUnique();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        namesAreUnique();
                    }

                    public void insertUpdate(DocumentEvent e) {
                        namesAreUnique();
                    }
                });
                fieldsPanel.add(fieldTextAreas[currentIndex]);
            }
            if (fieldTextAreas.length > 0) {
                defaultTextBackground = fieldTextAreas[0].getBackground();
            }
            FieldsPanel.this.add(fieldsPanel, BorderLayout.CENTER);
        } catch (QueryException exception) {
            queryTimeLabel.setText("Error: " + exception.getMessage() + "\n");
        }
    }

    public boolean namesAreUnique() {
        boolean namesAreUnique = true;
        ArrayList<String> fieldNames = new ArrayList<String>();
        final boolean validFields[] = new boolean[fieldTextAreas.length];
        for (int fieldCounter = 0; fieldCounter < fieldTextAreas.length; fieldCounter++) {
            JTextArea currentTextArea = fieldTextAreas[fieldCounter];
            final String currentText = currentTextArea.getText();
            selectedFieldNames[fieldCounter] = currentText;
            if (fieldNames.contains(currentText)) {
                validFields[fieldCounter] = false;
                namesAreUnique = false;
            } else {
                validFields[fieldCounter] = true;
            }
            fieldNames.add(currentText);
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                for (int fieldCounter = 0; fieldCounter < fieldTextAreas.length; fieldCounter++) {
                    if (validFields[fieldCounter]) {
                        fieldTextAreas[fieldCounter].setBackground(defaultTextBackground);
                    } else {
                        fieldTextAreas[fieldCounter].setBackground(Color.red);
                    }
                }
            }
        });
        return namesAreUnique;
    }

//    static public void main(String[] args) {
//        JFrame jFrame = new JFrame("Fields Panel Test");
//        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
////        final ArbilWindowManager arbilWindowManager = new ArbilWindowManager();
////        final KinSessionStorage kinSessionStorage = new KinSessionStorage(new ApplicationVersionManager(new KinOathVersion()));
////        arbilWindowManager.setSessionStorage(kinSessionStorage);
//        final CollectionExport entityCollection = new CollectionExport();
//        final GedcomExport gedcomExport = new GedcomExport(entityCollection);
//        FieldsPanel fieldsPanel = new FieldsPanel(gedcomExport);
//        fieldsPanel.populateFields();
//        jFrame.setContentPane(fieldsPanel);
//        jFrame.pack();
//        jFrame.setVisible(true);
//    }
}
