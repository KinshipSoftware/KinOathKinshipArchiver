package nl.mpi.kinnate.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.svg.GraphPanel;

/**
 *  Document   : KinTermPanel
 *  Created on : Mar 8, 2011, 12:21:12 PM
 *  Author     : Peter Withers
 */
public class KinTermPanel extends JPanel {

    public KinTermPanel(SavePanel savePanel, GraphPanel graphPanel) {
        this.add(new JLabel("KinTerms"));
    }
}
