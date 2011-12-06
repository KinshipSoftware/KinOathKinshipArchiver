package nl.mpi.kinnate.ui.window;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JFrame;
import nl.mpi.arbil.util.ApplicationVersionManager;

/**
 *  Document   : DiagramWindowManager
 *  Created on : Dec 1, 2011, 4:03:01 PM
 *  Author     : Peter Withers
 */
public class TabDiagramManager extends AbstractDiagramManager {

    private javax.swing.JTabbedPane jTabbedPane1;

    public TabDiagramManager(ApplicationVersionManager versionManager, JFrame mainFrame) {
        super(versionManager, mainFrame);
        jTabbedPane1 = new javax.swing.JTabbedPane();
        mainFrame.add(jTabbedPane1, BorderLayout.CENTER);
    }

    @Override
    public void createDiagramContainer(String diagramTitle, Component diagramComponent) {
        jTabbedPane1.add(diagramTitle, diagramComponent);
        jTabbedPane1.setSelectedComponent(diagramComponent);
    }

    @Override
    Component getSelectedDiagram() {
        return jTabbedPane1.getSelectedComponent();
    }

    @Override
    public void setSelectedDiagram(Component diagramComponent) {
        jTabbedPane1.setSelectedComponent(diagramComponent);
    }

    @Override
    public void setSelectedDiagram(int diagramIndex) {
        jTabbedPane1.setSelectedIndex(diagramIndex);
    }

    public int getSavePanelIndex() {
        return jTabbedPane1.getSelectedIndex();
    }

    public String getSavePanelTitle(int selectedIndex) {
        return jTabbedPane1.getTitleAt(selectedIndex);
    }

    @Override
    Component getDiagramAt(int diagramIndex) {
        return jTabbedPane1.getComponentAt(diagramIndex);
    }

    public void closeSavePanel(int selectedIndex) {
        jTabbedPane1.remove(selectedIndex);
    }

    public void setDiagramTitle(int diagramIndex, String diagramTitle) {
        jTabbedPane1.setTitleAt(diagramIndex, diagramTitle);
    }

    @Override
    public Component[] getAllDiagrams() {
        return jTabbedPane1.getComponents();
    }
}
