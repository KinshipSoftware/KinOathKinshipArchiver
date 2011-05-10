package nl.mpi.kinnate.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *  Document   : RecentFileMenu
 *  Created on : Apr 15, 2011, 11:30:39 AM
 *  Author     : Peter Withers
 */
public class SamplesFileMenu extends JMenu implements ActionListener {

    MainFrame mainFrame;

    public SamplesFileMenu(MainFrame mainFrameLocal) {
        mainFrame = mainFrameLocal;
        addSampleToMenu("Hawaiian Kin Terms", "HawaiianKinTerms.svg");
        addSampleToMenu("Japanese Kin Terms", "JapaneseKinTerms.svg");
        addSampleToMenu("Cha'palaa Kin Terms", "ChapalaaKinTerms.svg");
        addSampleToMenu("Gendered Ego", "GenderedEgo.svg");
    }

    private void addSampleToMenu(String menuText, String sampleFileString) {
        String currentFilePath = SamplesFileMenu.class.getResource("../../../../svgsamples/" + sampleFileString).getPath();
        JMenuItem currentMenuItem = new JMenuItem(menuText);
        currentMenuItem.setActionCommand(currentFilePath);
        currentMenuItem.addActionListener(this);
        this.add(currentMenuItem);
    }

    public void actionPerformed(ActionEvent e) {
        mainFrame.openDiagram(new File(e.getActionCommand()), false);
    }
}
