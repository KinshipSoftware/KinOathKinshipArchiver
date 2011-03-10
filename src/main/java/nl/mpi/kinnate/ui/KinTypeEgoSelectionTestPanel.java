package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.ImdiTable;
import nl.mpi.arbil.ImdiTableModel;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.kinnate.entityindexer.EntityIndex;
import nl.mpi.kinnate.svg.GraphData;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.SavePanel;

/**
 *  Document   : KinTypeStringTestPanel
 *  Created on : Sep 29, 2010, 12:52:01 PM
 *  Author     : Peter Withers
 */
public class KinTypeEgoSelectionTestPanel extends JPanel implements SavePanel {

    private JTextArea kinTypeStringInput;
    private GraphPanel graphPanel;
    private GraphData graphData;
    private EgoSelectionPanel egoSelectionPanel;
    private KinTermPanel kinTermPanel;
    private EntityIndex entityIndex;
    private String defaultString = "This test panel should provide a kin diagram based on selected egos and the the kintype strings entered here.\nEnter one string per line.";
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

        JSplitPane egoSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane kintermSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        kinGraphPanel.add(egoSplitPane, BorderLayout.CENTER);
//        outerSplitPane.setDividerLocation(0.5); // todo: add this to its parent so that the divider position sticks
        egoSplitPane.setLeftComponent(egoSelectionPanel);
        kintermSplitPane.setLeftComponent(graphPanel);
        kintermSplitPane.setRightComponent(kinTermPanel);
        egoSplitPane.setRightComponent(kintermSplitPane);

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

        entityIndex = new EntityIndex(graphPanel.getIndexParameters());
//        entityIndex.indexEntities();

        graphData = new GraphData();
        if (existingFile != null && existingFile.exists()) {
            graphPanel.readSvg(existingFile);
        } else {
            graphPanel.drawNodes(graphData);
        }
        URI[] egoSelection = graphPanel.getEgoList();
        try {
            graphData.setEgoNodes(entityIndex.getRelationsOfEgo(egoSelection, kinTypeStrings));
        } catch (URISyntaxException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Failed to load an entity", "Kinnate");
        }
        egoSelectionPanel.setEgoNodes(graphPanel.getEgoList());
        kinTypeStrings = graphPanel.getKinTypeStrigs();

        IndexerParametersPanel indexerParametersPanel = new IndexerParametersPanel(this, graphPanel, entityIndex, tableCellDragHandler);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, kinGraphPanel,
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScrollPane, indexerParametersPanel));
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
            graphData.setEgoNodes(entityIndex.getRelationsOfEgo(graphPanel.getEgoList(), kinTypeStrings));
        } catch (URISyntaxException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Failed to load an entity", "Kinnate");
        }
        graphPanel.drawNodes(graphData);
    }

    public void addEgoNodes(URI[] egoSelection) {
        graphPanel.setEgoList(egoSelection);
        drawGraph();
        egoSelectionPanel.setEgoNodes(graphPanel.getEgoList());
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
