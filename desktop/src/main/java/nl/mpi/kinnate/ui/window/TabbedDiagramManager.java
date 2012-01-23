package nl.mpi.kinnate.ui.window;

import java.awt.Component;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.kinnate.entityindexer.EntityCollection;

/**
 *  Document   : DiagramWindowManager
 *  Created on : Dec 1, 2011, 4:03:01 PM
 *  Author     : Peter Withers
 */
public class TabbedDiagramManager extends AbstractDiagramManager {

    private javax.swing.JTabbedPane jTabbedPane1;

    public TabbedDiagramManager(ApplicationVersionManager versionManager, ArbilWindowManager dialogHandler, SessionStorage sessionStorage, BugCatcher bugCatcher, ArbilDataNodeLoader dataNodeLoader, ArbilTreeHelper treeHelper, EntityCollection entityCollection) {
        super(versionManager, dialogHandler, sessionStorage, bugCatcher, dataNodeLoader, treeHelper, entityCollection);
        jTabbedPane1 = new javax.swing.JTabbedPane();
    }

    @Override
    public void createApplicationWindow() {
        createDiagramWindow("", jTabbedPane1);
    }

    @Override
    public Component createDiagramContainer(Component diagramComponent) {
        String diagramTitle = diagramComponent.getName();
        jTabbedPane1.add(diagramTitle, diagramComponent);
        jTabbedPane1.setSelectedComponent(diagramComponent);
        return diagramComponent;
    }

    @Override
    public void createDiagramSubPanel(String diagramTitle, Component diagramComponent, Component parentPanel) {
        diagramComponent.setName(diagramTitle);
        createDiagramContainer(diagramComponent);
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
