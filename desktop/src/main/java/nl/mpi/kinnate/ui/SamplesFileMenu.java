package nl.mpi.kinnate.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;

/**
 *  Document   : RecentFileMenu
 *  Created on : Apr 15, 2011, 11:30:39 AM
 *  Author     : Peter Withers
 */
public class SamplesFileMenu extends JMenu implements ActionListener {

    MainFrame mainFrame;

    public SamplesFileMenu(MainFrame mainFrameLocal) {
        mainFrame = mainFrameLocal;
        addSampleToMenu("Freeform Diagram Syntax", "FreeformDiagramSyntax.svg");
        addSampleToMenu("Application Overview", "ApplicationOverview.svg");
        addSampleToMenu("Hawaiian Kin Terms", "HawaiianKinTerms.svg");
        addSampleToMenu("Japanese Kin Terms", "JapaneseKinTerms.svg");
        addSampleToMenu("Swedish Kin Terms", "SwedishKinTerms.svg");
//        addSampleToMenu("Custom Symbols", "CustomSymbols.svg");
        addSampleToMenu("Named Transient Entities", "NamedTransientEntities.svg");
//        addSampleToMenu("Cha'palaa Kin Terms", "ChapalaaKinTerms.svg");
//        addSampleToMenu("Gendered Ego", "GenderedEgo.svg");
        addSampleToMenu("Olivier Kyburz Examples", "N40.svg");
//        addSampleToMenu("Archive Link Example", "ArchiveLinks.svg");
        addSampleToMenu("Charles II of Spain", "Charles_II_of_Spain.svg");
        addSampleToMenu("Imported Data Query Example (ANTONIO DE PAULA PESSOA DE /FIGUEIREDO/)", "QueryExample.svg");
//        addSampleToMenu("Imported Entities (600)", "600ImportedEntities.svg");
//        addSampleToMenu("R Usage of the Entity Server", "R-ServerUsage.svg");
    }

    private void addSampleToMenu(String menuText, String sampleFileString) {
        String currentFilePath = SamplesFileMenu.class.getResource("/svgsamples/" + sampleFileString).toString();
        JMenuItem currentMenuItem = new JMenuItem(menuText);
        currentMenuItem.setActionCommand(currentFilePath);
        currentMenuItem.addActionListener(this);
        this.add(currentMenuItem);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            final URI sampleFile = new URI(e.getActionCommand());
            if (e.getSource() instanceof JMenuItem) {
                String sampleName = ((JMenuItem) e.getSource()).getText();
                mainFrame.openDiagram(sampleName, sampleFile, false);
            }
        } catch (URISyntaxException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Failed to load sample", "Sample Diagram");
        }
    }
//    private void addLaunchSampleToMenu(String menuText, String sampleFileString) {
//        String currentFilePath = SamplesFileMenu.class.getResource("../../../../svgsamples/" + sampleFileString).getPath();
//        JMenuItem currentMenuItem = new JMenuItem(menuText);
//        currentMenuItem.setActionCommand(currentFilePath);
//        currentMenuItem.addActionListener(new LaunchExternal());
//        this.add(currentMenuItem);
//    }
//
//    class LaunchExternal implements ActionListener {
//
//        public void actionPerformed(ActionEvent e) {
//            try {
//                GuiHelper.getSingleInstance().openFileInExternalApplication(new URI(e.getActionCommand()));
//            } catch (URISyntaxException exception) {
//                System.err.println(exception.getMessage());
//            }
//        }
//    }
}
