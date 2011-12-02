package nl.mpi.kinnate.ui;

import java.io.File;
import java.net.URI;
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
}
