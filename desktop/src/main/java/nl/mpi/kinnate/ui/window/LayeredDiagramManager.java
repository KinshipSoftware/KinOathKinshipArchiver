package nl.mpi.kinnate.ui.window;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;

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

    public LayeredDiagramManager(JFrame mainFrame) {
        super(mainFrame);
        this.mainFrame = mainFrame;
        mainPanel = new javax.swing.JPanel(new BorderLayout());
        mainFrame.add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public void createDiagramContainer(String diagramTitle, Component diagramComponent) {
        titleMap.put(diagramComponent, diagramTitle);
        diagramArray.add(diagramComponent);
        setSelectedDiagram(diagramComponent);
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
        mainFrame.setTitle(diagramTitle);
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
    }

    @Override
    public Component[] getAllDiagrams() {
        return diagramArray.toArray(new Component[]{});
    }
}
