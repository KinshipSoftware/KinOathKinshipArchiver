package nl.mpi.kinnate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 *  Document   : KinTypeStringTestPanel
 *  Created on : Sep 29, 2010, 12:52:01 PM
 *  Author     : Peter Withers
 */
public class KinTypeEgoSelectionTestPanel extends JPanel {

    JTextArea kinTypeStringInput;
    GraphPanel graphPanel;
    GraphData graphData;
    EgoSelectionPanel egoSelectionPanel;
    String defaultString = "This test panel should provide a kin diagram of the kintype strings entered here.\nEnter one string per line.\nEach new line (enter/return key) will update the graph.";

    public KinTypeEgoSelectionTestPanel() {
        this.setLayout(new BorderLayout());
        graphPanel = new GraphPanel();
        egoSelectionPanel = new EgoSelectionPanel();
        kinTypeStringInput = new JTextArea(defaultString);
        kinTypeStringInput.setBorder(javax.swing.BorderFactory.createTitledBorder("Kin Type Strings"));
        this.add(kinTypeStringInput, BorderLayout.PAGE_START);
        this.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, egoSelectionPanel, new JScrollPane(graphPanel)), BorderLayout.CENTER);
//        this.add(new EgoSelectionPanel(), BorderLayout.LINE_START);
//        this.add(new JScrollPane(graphPanel), BorderLayout.CENTER);
//        kinTypeStringInput.setForeground(Color.lightGray);
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
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void keyPressed(KeyEvent e) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void keyReleased(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                KinTypeStringConverter graphData = new KinTypeStringConverter();
                graphData.readKinTypes(kinTypeStringInput.getText().split("\n"));
                graphPanel.drawNodes(graphData);
                KinTypeEgoSelectionTestPanel.this.doLayout();
//                }
            }
        });
        graphPanel.readSvg(new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/src/main/resources/EgoSelection.svg"));
        graphData = new GraphData();
        ImdiTreeObject[] egoSelection = graphPanel.getEgoList();
        graphData.setEgoNodes(egoSelection);
        egoSelectionPanel.setEgoNodes(egoSelection);
        graphPanel.drawNodes(graphData);
    }

    public void addEgoNodes(ImdiTreeObject[] egoSelection) {
        graphData.setEgoNodes(egoSelection);
        egoSelectionPanel.setEgoNodes(egoSelection);
        graphPanel.drawNodes(graphData);
    }
}
