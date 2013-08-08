/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.ui.window;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.projects.ProjectManager;

/**
 * Document : DiagramWindowManager Created on : Dec 1, 2011, 4:03:01 PM
 *
 * @author Peter Withers
 */
public class LayeredDiagramManager extends AbstractDiagramManager {

    private javax.swing.JPanel mainPanel;
    private HashMap<Component, String> titleMap = new HashMap<Component, String>();
    private ArrayList<Component> diagramArray = new ArrayList<Component>();
    private JFrame mainFrame;

    public LayeredDiagramManager(ApplicationVersionManager versionManager, ArbilWindowManager dialogHandler, SessionStorage sessionStorage, ArbilDataNodeLoader dataNodeLoader, ArbilTreeHelper treeHelper, ProjectManager projectManager) {
        super(versionManager, dialogHandler, sessionStorage, dataNodeLoader, treeHelper, projectManager);
        mainPanel = new javax.swing.JPanel(new BorderLayout());
    }

    @Override
    public void createApplicationWindow() {
        this.mainFrame = createDiagramWindow(getSavePanelTitle(getSavePanelIndex(null)), mainPanel, null);
    }

    @Override
    public Component createDiagramContainer(Component diagramComponent, Rectangle preferredSizeLocation) {
        String diagramTitle = diagramComponent.getName();
        titleMap.put(diagramComponent, diagramTitle);
        diagramArray.add(diagramComponent);
        setSelectedDiagram(diagramComponent);
        return diagramComponent;
    }

    @Override
    public void createDiagramSubPanel(String diagramTitle, Component diagramComponent, Component parentPanel) {
        // todo: this should use the parentPanel not getSavePanelIndex.
        int currentDiagramIndex = getSavePanelIndex(null);
        Component currentComponent = getDiagramAt(currentDiagramIndex);
        JTabbedPane tabbedPane;
        if (!(currentComponent instanceof JTabbedPane)) {
            tabbedPane = new JTabbedPane();
            final String savePanelTitle = getSavePanelTitle(currentDiagramIndex);
            tabbedPane.addTab(savePanelTitle, currentComponent);
            titleMap.remove(currentComponent);
            titleMap.put(tabbedPane, savePanelTitle);
            diagramArray.set(currentDiagramIndex, tabbedPane);// todo: this could be incorrect, but is not used at the moment
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
        if (mainFrame != null) {
            setWindowTitle(mainFrame, diagramTitle);
        }
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

    public int getSavePanelIndex(Component eventTarget) {
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
        if (mainFrame != null) {
            setWindowTitle(mainFrame, diagramTitle);
        }
    }

    @Override
    public Component[] getAllDiagrams() {
        return diagramArray.toArray(new Component[]{});
    }
}
