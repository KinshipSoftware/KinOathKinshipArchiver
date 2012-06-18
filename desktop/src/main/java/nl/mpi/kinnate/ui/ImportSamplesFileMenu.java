package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 * Document : RecentFileMenu
 * Created on : Apr 15, 2011, 11:30:39 AM
 * Author : Peter Withers
 */
public class ImportSamplesFileMenu extends JMenu implements ActionListener {

    private AbstractDiagramManager diagramWindowManager;
    private MessageDialogHandler dialogHandler;
    private Component parentComponent;

    public ImportSamplesFileMenu(AbstractDiagramManager diagramWindowManager, MessageDialogHandler dialogHandler, Component parentComponent) {
        this.diagramWindowManager = diagramWindowManager;
        this.parentComponent = parentComponent;
        this.dialogHandler = dialogHandler;
        addSampleToMenu("Gedcom Simple File (small sample of three individuals)", "/gedcomsamples/wiki-test-ged.ged");
        addSampleToMenu("Gedcom Torture File (only for testing GEDOM compliance)", "/TestGED/TGC55C.ged");
//        addSampleToMenu("Descententes de Jose Antonio de Figueiredo", "/gedcomsamples/descententes_de_jose_antonio_de_figueiredo.ged");
//        addSampleToMenu("Wadeye-Joe-Blythe-20110525", "/AllianceSamples/Wadeye-Joe-Blythe-20110525.csv");
        addSampleToMenu("European Royalty (royal92.ged)", "/gedcomsamples/royal92.ged");
//        addSampleToMenu("Doerte Sample", "/DoerteSamples/sample.csv");
//        addSampleToMenu("AltNetspace Sample", "/AltNetspace/people.csv");
    }

    private void addSampleToMenu(String menuText, String sampleFileString) {
//        String currentFilePath = ImportSamplesFileMenu.class.getResource("../../../../svgsamples/" + sampleFileString).getPath();
        JMenuItem currentMenuItem = new JMenuItem(menuText);
        currentMenuItem.setActionCommand(sampleFileString);
        currentMenuItem.addActionListener(this);
        this.add(currentMenuItem);
    }

    public void actionPerformed(ActionEvent e) {
        SavePanel originatingSavePanel = diagramWindowManager.getCurrentSavePanel(parentComponent);
        try {
            diagramWindowManager.openJarImportPanel(e.getActionCommand(), originatingSavePanel);
        } catch (ImportException exception1) {
            dialogHandler.addMessageDialogToQueue(exception1.getMessage() + "\n" + e.getActionCommand(), "Import Sample Data");
        }
    }
}
