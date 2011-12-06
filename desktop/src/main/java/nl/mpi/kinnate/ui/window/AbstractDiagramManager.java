package nl.mpi.kinnate.ui.window;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.ui.menu.DocumentNewMenu;
import nl.mpi.kinnate.ui.EntityUploadPanel;
import nl.mpi.kinnate.ui.GedcomImportPanel;
import nl.mpi.kinnate.ui.KinDiagramPanel;
import nl.mpi.kinnate.ui.menu.RecentFileMenu;

/**
 *  Document   : AbstractDiagramManager
 *  Created on : Dec 6, 2011, 12:28:56 PM
 *  Author     : Peter Withers
 */
public abstract class AbstractDiagramManager {

    private RecentFileMenu recentFileMenu;
    private EntityUploadPanel entityUploadPanel;
    private ApplicationVersionManager versionManager;

    public AbstractDiagramManager(ApplicationVersionManager versionManager, JFrame mainFrame) {
        this.versionManager = versionManager;
        mainFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                // check that all diagrams are saved and ask user if not
                if (offerUserToSaveAll()) {
                    System.exit(0);
                }
            }
        });
    }

    public void setWindowTitle(JFrame windowFrame, String titleString) {
        windowFrame.setTitle(versionManager.getApplicationVersion().applicationTitle + " " + versionManager.getApplicationVersion().currentMajor + "." + versionManager.getApplicationVersion().currentMinor + "." + versionManager.getApplicationVersion().currentRevision + " - " + titleString);
    }

    public void setWindowIcon(JFrame windowFrame) {
        // set the icon for the application (if this is still required for the various OSs). This is not required for Mac but might be needed for windows or linux.
        windowFrame.setIconImage(ArbilIcons.getSingleInstance().linorgIcon.getImage());
    }

    abstract public void createDiagramContainer(String diagramTitle, Component diagramComponent);

    public void newDiagram() {
        URI defaultDiagramUri = null;
        if (KinDiagramPanel.getDefaultDiagramFile().exists()) {
            defaultDiagramUri = KinDiagramPanel.getDefaultDiagramFile().toURI();
        }
        KinDiagramPanel egoSelectionTestPanel = new KinDiagramPanel(defaultDiagramUri, false);
        createDiagramContainer("Unsaved Default Diagram", egoSelectionTestPanel);
    }

    public void newDiagram(DocumentNewMenu.DocumentType documentType) {
        KinDiagramPanel egoSelectionTestPanel = new KinDiagramPanel(documentType);
        createDiagramContainer("Unsaved " + documentType.getDisplayName(), egoSelectionTestPanel);
    }

    public void openDiagram(String diagramTitle, URI selectedUri, boolean saveToRecentMenu) {
        if (saveToRecentMenu) {
            // prevent files from the samples menu being added to the recent files menu
            recentFileMenu.addRecentFile(new File(selectedUri));
        }
        KinDiagramPanel egoSelectionTestPanel = new KinDiagramPanel(selectedUri, saveToRecentMenu);
//        egoSelectionTestPanel.setTransferHandler(dragTransferHandler);
        createDiagramContainer(diagramTitle, egoSelectionTestPanel);
    }

    abstract Component getSelectedDiagram();

    public void loadAllTrees() {
        Component selectedComponent = getSelectedDiagram();
        if (selectedComponent instanceof KinDiagramPanel) {
            ((KinDiagramPanel) selectedComponent).loadAllTrees();
        }
    }

    public void openImportPanel(String importUrlString) {
        new GedcomImportPanel(this).startImport(importUrlString);
    }

    public void openJarImportPanel(String importUrlString) {
        new GedcomImportPanel(this).startImportJar(importUrlString);
    }

    public abstract void setSelectedDiagram(Component diagramComponent);

    public abstract void setSelectedDiagram(int diagramIndex);

    public void openEntityUploadPanel() {
        if (entityUploadPanel == null) {
            entityUploadPanel = new EntityUploadPanel();
            createDiagramContainer("Entity Upload", entityUploadPanel);
        }
        setSelectedDiagram(entityUploadPanel);
//        JDialog uploadDialog = new JDialog(this, "Entity Upload", true);
//        uploadDialog.setContentPane(new EntityUploadPanel());
//        uploadDialog.setLocationRelativeTo(this);
//        uploadDialog.setPreferredSize(new Dimension(100, 150));
//        uploadDialog.setVisible(true);
    }

    public abstract int getSavePanelIndex();

    public abstract String getSavePanelTitle(int selectedIndex);

    public SavePanel getCurrentSavePanel() {
        return getSavePanel(getSavePanelIndex());
    }

    abstract Component getDiagramAt(int diagramIndex);

    public SavePanel getSavePanel(int tabIndex) {
        Object selectedComponent = getDiagramAt(tabIndex);
        SavePanel savePanel = null;
        if (selectedComponent instanceof SavePanel) {
            savePanel = (SavePanel) selectedComponent;
        }
        return savePanel;
    }

    public abstract void closeSavePanel(int selectedIndex);

    public KinTermSavePanel getKinTermPanel() {
        SavePanel selectedComponent = getCurrentSavePanel();
        KinTermSavePanel kinTermSavePanel = null;
        if (selectedComponent instanceof KinTermSavePanel) {
            kinTermSavePanel = (KinTermSavePanel) selectedComponent;
        }
        return kinTermSavePanel;
    }

    public abstract void setDiagramTitle(int diagramIndex, String diagramTitle);

    abstract public Component[] getAllDiagrams();

    public boolean offerUserToSaveAll() {
        int diagramCount = getAllDiagrams().length;
        for (int diagramCounter = 0; diagramCounter < diagramCount; diagramCounter++) {
            Component selectedComponent = getDiagramAt(diagramCounter);
            if (selectedComponent instanceof SavePanel) {
                SavePanel savePanel = (SavePanel) selectedComponent;
                setSelectedDiagram(selectedComponent);
                String diagramName = getSavePanelTitle(diagramCounter);
                if (savePanel.requiresSave()) {
                    // warn user to save
                    switch (ArbilWindowManager.getSingleInstance().showDialogBox("There are unsaved changes in: \"" + diagramName + "\"\nDo you want to save before closing?", "Close Diagram", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                        case JOptionPane.YES_OPTION:
                            if (savePanel.hasSaveFileName()) {
                                savePanel.saveToFile();
                            } else {
                                saveDiagramAs(savePanel);
                            }
                        case JOptionPane.NO_OPTION:
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            return false;
                    }
                }
            }
        }
        return true;
    }

    private String saveDiagramAs(SavePanel savePanel) {
        // todo: update the file select to limit to svg and test that a file has been selected
        // todo: move this into the arbil window manager and get the last used directory
        // todo: make sure the file has the svg suffix
        JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new FileFilter() {

            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                return (file.getName().toLowerCase().endsWith(".svg"));
            }

            @Override
            public String getDescription() {
                return "Scalable Vector Graphics (SVG)";
            }
        });

        int returnVal = fc.showSaveDialog((Component) savePanel);
        // make sure the file path ends in .svg lowercase
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File svgFile = fc.getSelectedFile();
            if (!svgFile.getName().toLowerCase().endsWith(".svg")) {
                svgFile = new File(svgFile.getParentFile(), svgFile.getName() + ".svg");
            }
            savePanel.saveToFile(svgFile);
            recentFileMenu.addRecentFile(svgFile);
            return svgFile.getName();
        } else {
            // user canceled so there is no file selected and nothing to save
            return null;
        }
//        File selectedFile[] = LinorgWindowManager.getSingleInstance().showFileSelectBox("Save Kin Diagram", false, false, false);
//        if (selectedFile != null && selectedFile.length > 0) {
//            int tabIndex = Integer.valueOf(evt.getActionCommand());
//            SavePanel savePanel = getSavePanel(tabIndex);
//            savePanel.saveToFile(selectedFile[0]);
//            jTabbedPane1.setTitleAt(tabIndex, selectedFile[0].getName());
//        } else {
//            // todo: warn user that no file selected and so cannot save
//        }
    }
}
