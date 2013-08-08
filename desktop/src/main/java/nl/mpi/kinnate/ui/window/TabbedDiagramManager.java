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

import java.awt.Component;
import java.awt.Rectangle;
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
public class TabbedDiagramManager extends AbstractDiagramManager {

    private javax.swing.JTabbedPane jTabbedPane1;

    public TabbedDiagramManager(ApplicationVersionManager versionManager, ArbilWindowManager dialogHandler, SessionStorage sessionStorage, ArbilDataNodeLoader dataNodeLoader, ArbilTreeHelper treeHelper, ProjectManager projectManager) {
        super(versionManager, dialogHandler, sessionStorage, dataNodeLoader, treeHelper, projectManager);
        jTabbedPane1 = new javax.swing.JTabbedPane();
    }

    @Override
    public void createApplicationWindow() {
        createDiagramWindow("", jTabbedPane1, null);
    }

    @Override
    public Component createDiagramContainer(Component diagramComponent, Rectangle preferredSizeLocation) {
        String diagramTitle = diagramComponent.getName();
        jTabbedPane1.add(diagramTitle, diagramComponent);
        jTabbedPane1.setSelectedComponent(diagramComponent);
        return diagramComponent;
    }

    @Override
    public void createDiagramSubPanel(String diagramTitle, Component diagramComponent, Component parentPanel) {
        diagramComponent.setName(diagramTitle);
        createDiagramContainer(diagramComponent, null);
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

    public int getSavePanelIndex(Component eventTarget) {
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
