package nl.mpi.kinnate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import nl.mpi.arbil.ImdiTable;
import nl.mpi.arbil.ImdiTableModel;
import nl.mpi.kinnate.EntityIndexer.EntityIndex;

/**
 *  Document   : KinTypeStringTestPanel
 *  Created on : Sep 29, 2010, 12:52:01 PM
 *  Author     : Peter Withers
 */
public class KinTypeEgoSelectionTestPanel extends JPanel {

    private FieldSelectionList symbolFieldsList;
    private FieldSelectionList relationFieldsList;
    private JTextArea kinTypeStringInput;
    private GraphPanel graphPanel;
    private GraphData graphData;
    private EgoSelectionPanel egoSelectionPanel;
    private EntityIndex entityIndex;
    private String defaultString = "This test panel should provide a kin diagram based on selected egos and the the kintype strings entered here.\nEnter one string per line.";
    private String kinTypeStrings[] = new String[]{};

    public KinTypeEgoSelectionTestPanel(File existingFile) {
        this.setLayout(new BorderLayout());
        graphPanel = new GraphPanel();
        egoSelectionPanel = new EgoSelectionPanel();
        kinTypeStringInput = new JTextArea(defaultString);
        kinTypeStringInput.setBorder(javax.swing.BorderFactory.createTitledBorder("Kin Type Strings"));
        JPanel kinGraphPanel = new JPanel(new BorderLayout());
        kinGraphPanel.add(kinTypeStringInput, BorderLayout.PAGE_START);
        kinGraphPanel.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, egoSelectionPanel, graphPanel), BorderLayout.CENTER);

        // todo: add drag drop of field to these lists and initially populate them from the SVG data
        symbolFieldsList = new FieldSelectionList();
        relationFieldsList = new FieldSelectionList();
        JTabbedPane fieldListTabs = new JTabbedPane();
        fieldListTabs.add("Symbol Fields", new JScrollPane(symbolFieldsList));
        fieldListTabs.add("Relation Fields", new JScrollPane(relationFieldsList));

        ImdiTableModel imdiTableModel = new ImdiTableModel();
        ImdiTable imdiTable = new ImdiTable(imdiTableModel, "Selected Nodes");
        graphPanel.setImdiTableModel(imdiTableModel);

        JScrollPane tableScrollPane = new JScrollPane(imdiTable);
//        Dimension minimumSize = new Dimension(0, 0);
//        fieldListTabs.setMinimumSize(minimumSize);
//        tableScrollPane.setMinimumSize(minimumSize);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, kinGraphPanel,
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScrollPane, fieldListTabs));
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
                kinTypeStrings = kinTypeStringInput.getText().split("\n");
                graphPanel.setKinTypeStrigs(kinTypeStrings);
                graphData.setEgoNodes(entityIndex.getRelationsOfEgo(graphPanel.getEgoList(), kinTypeStrings));
                graphPanel.drawNodes(graphData);
            }
        });
        entityIndex = new EntityIndex(graphPanel.getIndexParameters());
        entityIndex.indexEntities();
        graphData = new GraphData();
        URI[] egoSelection = graphPanel.getEgoList();
        graphData.setEgoNodes(entityIndex.getRelationsOfEgo(egoSelection, kinTypeStrings));
        if (existingFile.exists()) {
            graphPanel.readSvg(existingFile);
        } else {
            graphPanel.drawNodes(graphData);
        }
        egoSelectionPanel.setEgoNodes(graphPanel.getEgoList());
        symbolFieldsList.setFieldList(graphPanel.getIndexParameters().symbolFieldsFields);
        relationFieldsList.setFieldList(graphPanel.getIndexParameters().relevantLinkData);
        kinTypeStrings = graphPanel.getKinTypeStrigs();
        boolean firstString = true;
        for (String currentKinTypeString : kinTypeStrings) {
            if (currentKinTypeString.trim().length() > 0) {
                if (firstString) {
                    kinTypeStringInput.setText("");
                    firstString = false;
                }
                kinTypeStringInput.append(currentKinTypeString.trim() + "\n");
            }
        }
    }

    public void addEgoNodes(URI[] egoSelection) {
        graphPanel.setEgoList(egoSelection);
        graphData.setEgoNodes(entityIndex.getRelationsOfEgo(egoSelection, kinTypeStrings));
        graphPanel.drawNodes(graphData);
        egoSelectionPanel.setEgoNodes(graphPanel.getEgoList());
    }
}
