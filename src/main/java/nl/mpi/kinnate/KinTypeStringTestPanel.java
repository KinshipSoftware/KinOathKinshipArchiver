package nl.mpi.kinnate;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 *  Document   : KinTypeStringTestPanel
 *  Created on : Sep 29, 2010, 12:52:01 PM
 *  Author     : Peter Withers
 */
public class KinTypeStringTestPanel extends JPanel {

    JTextArea kinTypeStringInput;

    public KinTypeStringTestPanel() {
        this.setLayout(new BorderLayout());
        kinTypeStringInput = new JTextArea("This test panel should provide a kin diagram of the kintype strings entered here.\nEnter one string per line.\nEach new line (enter/return key) will update the graph.");
        this.add(kinTypeStringInput, BorderLayout.PAGE_START);
        this.add(new GraphPanel1(), BorderLayout.CENTER);
    }
}
