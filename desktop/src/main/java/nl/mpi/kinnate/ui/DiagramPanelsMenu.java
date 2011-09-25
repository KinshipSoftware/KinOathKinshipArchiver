package nl.mpi.kinnate.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;

/**
 *  Document   : VisiblePanelSetting
 *  Created on : Sept 25, 2011, 12:50:44 PM
 *  Author     : Peter Withers
 */
public class DiagramPanelsMenu extends JMenu implements ActionListener {

    MainFrame mainFrame;

    public DiagramPanelsMenu(MainFrame mainFrameLocal) {
        mainFrame = mainFrameLocal;
        this.setText("Panels");
        this.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
