package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.SavePanel;

/**
 *  Document   : DiagramWindowManager
 *  Created on : Dec 1, 2011, 4:03:01 PM
 *  Author     : Peter Withers
 */
public class DiagramWindowManager {

    private RecentFileMenu recentFileMenu;
    private javax.swing.JTabbedPane jTabbedPane1;
    private EntityUploadPanel entityUploadPanel;

    public DiagramWindowManager(JFrame mainFrame) {
        jTabbedPane1 = new javax.swing.JTabbedPane();
        mainFrame.add(jTabbedPane1, BorderLayout.CENTER);
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

    public void newDiagram() {
        URI defaultDiagramUri = null;
        if (KinDiagramPanel.getDefaultDiagramFile().exists()) {
            defaultDiagramUri = KinDiagramPanel.getDefaultDiagramFile().toURI();
        }
        KinDiagramPanel egoSelectionTestPanel = new KinDiagramPanel(defaultDiagramUri, false);
        jTabbedPane1.add("Unsaved Default Diagram", egoSelectionTestPanel);
        jTabbedPane1.setSelectedComponent(egoSelectionTestPanel);
//        egoSelectionTestPanel.drawGraph();
    }

    public void newDiagram(DocumentNewMenu.DocumentType documentType) {
        KinDiagramPanel egoSelectionTestPanel = new KinDiagramPanel(documentType);
        jTabbedPane1.add("Unsaved " + documentType.getDisplayName(), egoSelectionTestPanel);
        jTabbedPane1.setSelectedComponent(egoSelectionTestPanel);
    }

    public void openDiagram(String diagramTitle, URI selectedUri, boolean saveToRecentMenu) {
        if (saveToRecentMenu) {
            // prevent files from the samples menu being added to the recent files menu
            recentFileMenu.addRecentFile(new File(selectedUri));
        }
        KinDiagramPanel egoSelectionTestPanel = new KinDiagramPanel(selectedUri, saveToRecentMenu);
//        egoSelectionTestPanel.setTransferHandler(dragTransferHandler);
        jTabbedPane1.add(diagramTitle, egoSelectionTestPanel);
        jTabbedPane1.setSelectedComponent(egoSelectionTestPanel);
//        egoSelectionTestPanel.drawGraph();
    }

    public void loadAllTrees() {
        Object selectedComponent = jTabbedPane1.getSelectedComponent();
        if (selectedComponent instanceof KinDiagramPanel) {
            ((KinDiagramPanel) selectedComponent).loadAllTrees();
        }
    }

    public void openImportPanel(String importUrlString) {
        new GedcomImportPanel(jTabbedPane1).startImport(importUrlString);
    }

    public void openEntityUploadPanel() {
        if (entityUploadPanel == null) {
            entityUploadPanel = new EntityUploadPanel();
            jTabbedPane1.add("Entity Upload", entityUploadPanel);
        }
        jTabbedPane1.setSelectedComponent(entityUploadPanel);
//        JDialog uploadDialog = new JDialog(this, "Entity Upload", true);
//        uploadDialog.setContentPane(new EntityUploadPanel());
//        uploadDialog.setLocationRelativeTo(this);
//        uploadDialog.setPreferredSize(new Dimension(100, 150));
//        uploadDialog.setVisible(true);
    }

    public int getSavePanelIndex() {
        return jTabbedPane1.getSelectedIndex();
    }

    public String getSavePanelTitle(int selectedIndex) {
        return jTabbedPane1.getTitleAt(selectedIndex);
    }

    public SavePanel getCurrentSavePanel() {
        return getSavePanel(getSavePanelIndex());
    }

    public SavePanel getSavePanel(int tabIndex) {
        Object selectedComponent = jTabbedPane1.getComponentAt(tabIndex);
        SavePanel savePanel = null;
        if (selectedComponent instanceof SavePanel) {
            savePanel = (SavePanel) selectedComponent;
        }
        return savePanel;
    }

    public void closeSavePanel(int selectedIndex) {
        jTabbedPane1.remove(selectedIndex);
    }

    public KinTermSavePanel getKinTermPanel() {
        SavePanel selectedComponent = getCurrentSavePanel();
        KinTermSavePanel kinTermSavePanel = null;
        if (selectedComponent instanceof KinTermSavePanel) {
            kinTermSavePanel = (KinTermSavePanel) selectedComponent;
        }
        return kinTermSavePanel;
    }

    public void setDiagramTitle(int diagramIndex, String diagramTitle) {
        jTabbedPane1.setTitleAt(diagramIndex, diagramTitle);
    }

    public boolean offerUserToSaveAll() {
        for (Component selectedComponent : jTabbedPane1.getComponents()) {
            if (selectedComponent instanceof SavePanel) {
                SavePanel savePanel = (SavePanel) selectedComponent;
                if (savePanel.requiresSave()) {
                    // warn user to save
                    switch (ArbilWindowManager.getSingleInstance().showDialogBox("There are unsaved changes in:\n" + selectedComponent.getName() + "\nDo you want to save before closing?", "Close Diagram", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
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
