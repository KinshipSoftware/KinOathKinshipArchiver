package nl.mpi.kinnate;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 *  Document   : KinTypeStringTestPanel
 *  Created on : Sep 29, 2010, 12:52:01 PM
 *  Author     : Peter Withers
 */
public class KinTypeStringTestPanel extends JPanel {

    JTextArea kinTypeStringInput;
    GraphPanel graphPanel;

    public KinTypeStringTestPanel() {
        this.setLayout(new BorderLayout());
        graphPanel = new GraphPanel();
        kinTypeStringInput = new JTextArea("This test panel should provide a kin diagram of the kintype strings entered here.\nEnter one string per line.\nEach new line (enter/return key) will update the graph.");
        this.add(kinTypeStringInput, BorderLayout.PAGE_START);
        this.add(graphPanel, BorderLayout.CENTER);
        kinTypeStringInput.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void keyPressed(KeyEvent e) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    KinTypeStringConverter graphData = new KinTypeStringConverter();
                    graphData.readKinTypes(kinTypeStringInput.getText().split("\n"));
                    graphPanel.drawNodes(graphData);
                    KinTypeStringTestPanel.this.doLayout();
                }
            }
        });
    }
}
