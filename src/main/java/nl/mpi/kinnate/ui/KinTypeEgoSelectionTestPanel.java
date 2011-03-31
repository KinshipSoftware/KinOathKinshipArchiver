package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.ImdiTable;
import nl.mpi.arbil.ImdiTableModel;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.EntityIndex;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityService;
import nl.mpi.kinnate.entityindexer.EntityServiceException;

/**
 *  Document   : KinTypeStringTestPanel
 *  Created on : Sep 29, 2010, 12:52:01 PM
 *  Author     : Peter Withers
 */
public class KinTypeEgoSelectionTestPanel extends JPanel implements SavePanel {

    private JTextArea kinTypeStringInput;
    private GraphPanel graphPanel;
    private GraphSorter graphSorter;
    private EgoSelectionPanel egoSelectionPanel;
    private KinTermPanel kinTermPanel;
    private EntityService entityIndex;
    private String defaultString = "# This test panel should provide a kin diagram based on selected egos and the the kintype strings entered here.\n# Enter one string per line.\n# By default all relations of the selected entity will be shown.\n";
    private String kinTypeStrings[] = new String[]{};

    public KinTypeEgoSelectionTestPanel(File existingFile) {
        this.setLayout(new BorderLayout());
        graphPanel = new GraphPanel(this);
        egoSelectionPanel = new EgoSelectionPanel();
        kinTermPanel = new KinTermPanel(this, graphPanel.getkinTerms());
        kinTypeStringInput = new JTextArea(defaultString);
        kinTypeStringInput.setBorder(javax.swing.BorderFactory.createTitledBorder("Kin Type Strings"));
        JPanel kinGraphPanel = new JPanel(new BorderLayout());
        kinGraphPanel.add(kinTypeStringInput, BorderLayout.PAGE_START);

//        JSplitPane egoSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JPanel kintermSplitPane = new JPanel(new BorderLayout());
//        kinGraphPanel.add(egoSplitPane, BorderLayout.CENTER);
//        outerSplitPane.setDividerLocation(0.5); // todo: add this to its parent so that the divider position sticks
        kintermSplitPane.add(new HidePane(egoSelectionPanel, "Ego Selection", BorderLayout.LINE_END), BorderLayout.LINE_START);
        kintermSplitPane.add(graphPanel, BorderLayout.CENTER);
        kintermSplitPane.add(new HidePane(kinTermPanel, "Kin Terms", BorderLayout.LINE_START), BorderLayout.LINE_END);
        kinGraphPanel.add(kintermSplitPane);

        ImdiTableModel imdiTableModel = new ImdiTableModel();
        ImdiTable imdiTable = new ImdiTable(imdiTableModel, "Selected Nodes");
        TableCellDragHandler tableCellDragHandler = new TableCellDragHandler();
        imdiTable.setTransferHandler(tableCellDragHandler);
        imdiTable.setDragEnabled(true);
        graphPanel.setImdiTableModel(imdiTableModel);

        JScrollPane tableScrollPane = new JScrollPane(imdiTable);
//        Dimension minimumSize = new Dimension(0, 0);
//        fieldListTabs.setMinimumSize(minimumSize);
//        tableScrollPane.setMinimumSize(minimumSize);

        // EntityIndex loads the xml files and reads the document for entity data
//        entityIndex = new EntityIndex(graphPanel.getIndexParameters());
        // EntityCollection queries the xml collection to get the entity data
        entityIndex = new EntityCollection();

        graphSorter = new GraphSorter();
        if (existingFile != null && existingFile.exists()) {
            graphPanel.readSvg(existingFile);
        } else {
            graphPanel.drawNodes(graphSorter);
            // todo: filter out the noise and only save or use the actual kin type strings
//            graphPanel.setKinTypeStrigs(kinTypeStringInput.getText().split("\n"));
//            kinTypeStrings = graphPanel.getKinTypeStrigs();
        }

        IndexerParametersPanel indexerParametersPanel = new IndexerParametersPanel(this, graphPanel, tableCellDragHandler);
        JPanel advancedPanel = new JPanel(new BorderLayout());
        advancedPanel.add(tableScrollPane, BorderLayout.CENTER);
        advancedPanel.add(new HidePane(indexerParametersPanel, "Indexer Parameters", BorderLayout.LINE_START), BorderLayout.LINE_END);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, kinGraphPanel, advancedPanel);
        this.add(splitPane);


        kinTypeStringInput.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                if (kinTypeStringInput.getText().equals(defaultString)) {
                    kinTypeStringInput.setText("");
                    kinTypeStringInput.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (kinTypeStringInput.getText().length() == 0) {
                    kinTypeStringInput.setText(defaultString);
                    kinTypeStringInput.setForeground(Color.lightGray);
                }
            }
        });
        kinTypeStringInput.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                graphPanel.setKinTypeStrigs(kinTypeStringInput.getText().split("\n"));
                kinTypeStrings = graphPanel.getKinTypeStrigs();
                drawGraph();
            }
        });
        boolean firstString = true;
        for (String currentKinTypeString : kinTypeStrings) {
            if (currentKinTypeString.trim().length() > 0) {
                if (firstString) {
                    kinTypeStringInput.setText("");
                    firstString = false;
                } else {
                    kinTypeStringInput.append("\n");
                }
                kinTypeStringInput.append(currentKinTypeString.trim());
            }
        }
    }

    public void drawGraph() {
        try {
            graphSorter.setEntitys(entityIndex.getRelationsOfEgo(null, graphPanel.getEgoUniquiIdentifiersList(), kinTypeStrings, graphPanel.getIndexParameters()));
        } catch (EntityServiceException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Failed to load an entity", "Kinnate");
        }
        egoSelectionPanel.setEgoNodes(graphPanel.getEgoPaths());
        kinTypeStrings = graphPanel.getKinTypeStrigs();
        graphPanel.drawNodes(graphSorter);
    }

    public void setEgoNodes(URI[] egoPathArray, String[] egoIdentifierArray) {
        graphPanel.setEgoList(egoPathArray, egoIdentifierArray);
        drawGraph();
    }

    public void addEgoNodes(URI[] egoPathArray, String[] egoIdentifierArray) {
        graphPanel.addEgo(egoPathArray, egoIdentifierArray);
        drawGraph();
    }

    public void removeEgoNodes(String[] egoIdentifierArray) {
        graphPanel.removeEgo(egoIdentifierArray);
        drawGraph();
    }

    public boolean hasSaveFileName() {
        return graphPanel.hasSaveFileName();
    }

    public boolean requiresSave() {
        return graphPanel.requiresSave();
    }

    public void saveToFile() {
        graphPanel.saveToFile();
    }

    public void saveToFile(File saveFile) {
        graphPanel.saveToFile(saveFile);
    }

    public void updateGraph() {
        this.drawGraph();
    }
}
