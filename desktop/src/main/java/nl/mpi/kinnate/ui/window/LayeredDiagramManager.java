package nl.mpi.kinnate.ui.window;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import nl.mpi.arbil.util.ApplicationVersionManager;

/**
 *  Document   : DiagramWindowManager
 *  Created on : Dec 1, 2011, 4:03:01 PM
 *  Author     : Peter Withers
 */
public class LayeredDiagramManager extends AbstractDiagramManager {

    private javax.swing.JPanel mainPanel;
    private HashMap<Component, String> titleMap = new HashMap<Component, String>();
    private ArrayList<Component> diagramArray = new ArrayList<Component>();
    private JFrame mainFrame;

    public LayeredDiagramManager(ApplicationVersionManager versionManager) {
        super(versionManager);
        mainPanel = new javax.swing.JPanel(new BorderLayout());
        this.mainFrame = createDiagramWindow(versionManager.getApplicationVersion().compileDate, mainPanel);
    }

    @Override
    public void createDiagramContainer(String diagramTitle, Component diagramComponent) {
        titleMap.put(diagramComponent, diagramTitle);
        diagramArray.add(diagramComponent);
        setSelectedDiagram(diagramComponent);
    }

    @Override
    public void createDiagramSubPanel(String diagramTitle, Component diagramComponent) {
        int currentDiagramIndex = getSavePanelIndex();
        Component currentComponent = getDiagramAt(currentDiagramIndex);
        JTabbedPane tabbedPane;
        if (!(currentComponent instanceof JTabbedPane)) {
            tabbedPane = new JTabbedPane();
            final String savePanelTitle = getSavePanelTitle(currentDiagramIndex);
            tabbedPane.addTab(savePanelTitle, currentComponent);
            titleMap.remove(currentComponent);
            titleMap.put(tabbedPane, savePanelTitle);
            diagramArray.set(currentDiagramIndex, tabbedPane);
            setSelectedDiagram(currentDiagramIndex);
        } else {
            tabbedPane = (JTabbedPane) currentComponent;
        }
        tabbedPane.addTab(diagramTitle, diagramComponent);
    }

    @Override
    Component getSelectedDiagram() {
        if (mainPanel.getComponents().length > 0) {
            return mainPanel.getComponent(0);
        } else {
            return null;
        }
    }

    @Override
    public void setSelectedDiagram(Component diagramComponent) {
        mainPanel.removeAll();
        String diagramTitle = null;
        if (diagramComponent != null) {
            mainPanel.add(diagramComponent, BorderLayout.CENTER);
            diagramTitle = titleMap.get(diagramComponent);
        }
        setWindowTitle(mainFrame, diagramTitle);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    @Override
    public void setSelectedDiagram(int diagramIndex) {
        if (diagramArray.size() > diagramIndex) {
            setSelectedDiagram(diagramArray.get(diagramIndex));
        } else {
            setSelectedDiagram(null);
        }
    }

    public int getSavePanelIndex() {
        return diagramArray.indexOf(getSelectedDiagram());
    }

    public String getSavePanelTitle(int selectedIndex) {
        return titleMap.get(diagramArray.get(selectedIndex));
    }

    @Override
    Component getDiagramAt(int diagramIndex) {
        return diagramArray.get(diagramIndex);
    }

    public void closeSavePanel(int selectedIndex) {
        titleMap.remove(diagramArray.get(selectedIndex));
        diagramArray.remove(selectedIndex);
        while (diagramArray.size() <= selectedIndex && selectedIndex > 0) {
            selectedIndex--;
        }
        setSelectedDiagram(selectedIndex);
    }

    public void setDiagramTitle(int diagramIndex, String diagramTitle) {
        titleMap.put(getSelectedDiagram(), diagramTitle);
        setWindowTitle(mainFrame, diagramTitle);
    }

    @Override
    public Component[] getAllDiagrams() {
        return diagramArray.toArray(new Component[]{});
    }
}
