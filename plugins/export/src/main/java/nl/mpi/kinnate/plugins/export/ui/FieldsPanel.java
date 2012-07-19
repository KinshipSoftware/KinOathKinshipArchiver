package nl.mpi.kinnate.plugins.export.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
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
                final int currentIndexFinal = currentIndex;
                fieldTextAreas[currentIndex].addKeyListener(new KeyListener() {

                    public void keyTyped(KeyEvent e) {
                        // todo: this does not respond to the first text change and hence it is the wrong listner to use
                        selectedFieldNames[currentIndexFinal] = ((JTextArea) e.getSource()).getText();
                        namesAreUnique();
                    }

                    public void keyPressed(KeyEvent e) {
                    }

                    public void keyReleased(KeyEvent e) {
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
        // todo: sort out this swing edt issue and test the latency when typing text
//        SwingUtilities.invokeLater(new Runnable() {
//
//            public void run() {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//        });
        for (JTextArea currentTextArea : fieldTextAreas) {
            final String currentText = currentTextArea.getText();
            if (fieldNames.contains(currentText)) {
                namesAreUnique = false;
                currentTextArea.setBackground(Color.red);
            } else {
                currentTextArea.setBackground(defaultTextBackground);
            }
            fieldNames.add(currentText);
        }
        return namesAreUnique;
    }

    static public void main(String[] args) {
        JFrame jFrame = new JFrame("Fields Panel Test");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        final ArbilWindowManager arbilWindowManager = new ArbilWindowManager();
//        final KinSessionStorage kinSessionStorage = new KinSessionStorage(new ApplicationVersionManager(new KinOathVersion()));
//        arbilWindowManager.setSessionStorage(kinSessionStorage);
        final CollectionExport entityCollection = new CollectionExport();
        final GedcomExport gedcomExport = new GedcomExport(entityCollection);
        FieldsPanel fieldsPanel = new FieldsPanel(gedcomExport);
        fieldsPanel.populateFields();
        jFrame.setContentPane(fieldsPanel);
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
