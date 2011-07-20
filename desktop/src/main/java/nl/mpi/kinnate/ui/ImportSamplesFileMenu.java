package nl.mpi.kinnate.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *  Document   : RecentFileMenu
 *  Created on : Apr 15, 2011, 11:30:39 AM
 *  Author     : Peter Withers
 */
public class ImportSamplesFileMenu extends JMenu implements ActionListener {

    MainFrame mainFrame;

    public ImportSamplesFileMenu(MainFrame mainFrameLocal) {
        mainFrame = mainFrameLocal;
        addSampleToMenu("Gedcom Simple File", "/gedcomsamples/wiki-test-ged.ged");
        addSampleToMenu("Gedcom Torture File", "/TestGED/TGC55C.ged");
        addSampleToMenu("Descententes de Jose Antonio de Figueiredo", "/gedcomsamples/descententes_de_jose_antonio_de_figueiredo.ged");
        addSampleToMenu("Wadeye-Joe-Blythe-20110525", "/AllianceSamples/Wadeye-Joe-Blythe-20110525.csv");
        addSampleToMenu("European Royalty (royal92.ged)", "/gedcomsamples/royal92.ged");
    }
    
    private void addSampleToMenu(String menuText, String sampleFileString) {
//        String currentFilePath = ImportSamplesFileMenu.class.getResource("../../../../svgsamples/" + sampleFileString).getPath();
        JMenuItem currentMenuItem = new JMenuItem(menuText);
        currentMenuItem.setActionCommand(sampleFileString);
        currentMenuItem.addActionListener(this);
        this.add(currentMenuItem);
    }

    public void actionPerformed(ActionEvent e) {
        mainFrame.importEntities(e.getActionCommand());
    }
}
