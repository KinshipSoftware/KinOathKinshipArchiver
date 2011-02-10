package nl.mpi.kinnate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import nl.mpi.kinnate.EntityIndexer.EntityIndex;

/**
 *  Document   : KinTypeStringTestPanel
 *  Created on : Sep 29, 2010, 12:52:01 PM
 *  Author     : Peter Withers
 */
public class KinTypeEgoSelectionTestPanel extends JPanel {

    private JTextArea kinTypeStringInput;
    private GraphPanel graphPanel;
    private GraphData graphData;
    private EgoSelectionPanel egoSelectionPanel;
    private EntityIndex entityIndex;
    private String defaultString = "This test panel should provide a kin diagram based on selected egos and the the kintype strings entered here.\nEnter one string per line.";
    private String kinTypeStrings[] = new String[]{};

    public KinTypeEgoSelectionTestPanel(EntityIndex entityIndexLocal, File existingFile) {
        entityIndex = entityIndexLocal;
        this.setLayout(new BorderLayout());
        graphPanel = new GraphPanel();
        egoSelectionPanel = new EgoSelectionPanel();
        kinTypeStringInput = new JTextArea(defaultString);
        kinTypeStringInput.setBorder(javax.swing.BorderFactory.createTitledBorder("Kin Type Strings"));
        this.add(kinTypeStringInput, BorderLayout.PAGE_START);
        this.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, egoSelectionPanel, graphPanel), BorderLayout.CENTER);
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
        graphData = new GraphData();
        URI[] egoSelection = graphPanel.getEgoList();
        graphData.setEgoNodes(entityIndex.getRelationsOfEgo(egoSelection, kinTypeStrings));
        if (existingFile.exists()) {
            graphPanel.readSvg(existingFile);
        } else {
            graphPanel.drawNodes(graphData);
        }
        egoSelectionPanel.setEgoNodes(graphPanel.getEgoList());
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
