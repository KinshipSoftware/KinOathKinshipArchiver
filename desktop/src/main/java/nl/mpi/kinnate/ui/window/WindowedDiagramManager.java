/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.ui.window;

import java.awt.Component;
import java.awt.Container;
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
import nl.mpi.arbil.util.ArbilLogConfigurer;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.projects.ProjectManager;

/**
 * Document : DiagramWindowManager Created on : Dec 1, 2011, 4:03:01 PM
 *
 * @author Peter Withers
 */
public class WindowedDiagramManager extends AbstractDiagramManager {

    private HashMap<JFrame, String> titleMap = new HashMap<JFrame, String>();
    private ArrayList<JFrame> diagramArray = new ArrayList<JFrame>();

    public WindowedDiagramManager(ApplicationVersionManager versionManager, ArbilWindowManager dialogHandler, SessionStorage sessionStorage, ArbilDataNodeLoader dataNodeLoader, ArbilTreeHelper treeHelper, ProjectManager projectManager, ArbilLogConfigurer arbilLogConfigurer) {
        super(versionManager, dialogHandler, sessionStorage, dataNodeLoader, treeHelper, projectManager, arbilLogConfigurer);
    }

    @Override
    public void createApplicationWindow() {
        // nothing to do for this diagram manager
    }

    @Override
    protected void closeWindowAction(JFrame windowFrame) {
        if (diagramArray.size() == 1) {
            super.closeWindowAction(windowFrame);
        } else {
            final int selectedIndex = diagramArray.indexOf(windowFrame);
            if (windowFrame instanceof SavePanel) {
                SavePanel savePanel = (SavePanel) windowFrame;
                String diagramTitle = getSavePanelTitle(selectedIndex);
                boolean userCanceled = offerUserToSave(savePanel, diagramTitle);
                if (!userCanceled) {
                    closeSavePanel(selectedIndex);
                }
            } else {
                closeSavePanel(selectedIndex);
            }
        }
    }

    @Override
    public Component createDiagramContainer(Component diagramComponent, Rectangle preferredSizeLocation) {
        String diagramTitle = diagramComponent.getName();
        JFrame diagramWindow = super.createDiagramWindow(diagramTitle, diagramComponent, preferredSizeLocation);
        titleMap.put(diagramWindow, diagramTitle);
        diagramArray.add(diagramWindow);
        return diagramWindow;
    }

    @Override
    public void createDiagramSubPanel(String diagramTitle, Component diagramComponent, Component parentPanel) {
//        int currentDiagramIndex = getSavePanelIndex();
        JFrame diagramFame = (JFrame) parentPanel; // getDiagramAt(currentDiagramIndex);
        Component currentComponent = diagramFame.getContentPane();
        JTabbedPane tabbedPane;
        if (!(currentComponent instanceof JTabbedPane)) {
            tabbedPane = new JTabbedPane();
            final String savePanelTitle = currentComponent.getName(); //getSavePanelTitle(currentDiagramIndex);
            tabbedPane.addTab(savePanelTitle, currentComponent);
            diagramFame.setContentPane(tabbedPane);
        } else {
            tabbedPane = (JTabbedPane) currentComponent;
        }
        tabbedPane.addTab(diagramTitle, diagramComponent);
    }

    @Override
    Component getSelectedDiagram() {
        // todo: this could cause issues if the application does not have focus
        //return KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSelectedDiagram(Component diagramComponent) {
        ((JFrame) diagramComponent).toFront();
    }

    @Override
    public void setSelectedDiagram(int diagramIndex) {
        if (diagramArray.size() > diagramIndex) {
            setSelectedDiagram(diagramArray.get(diagramIndex));
        } else {
            setSelectedDiagram(null);
        }
    }

    public int getSavePanelIndex(Component parentComponent) {
        return diagramArray.indexOf(parentComponent);
    }

    @Override
    public SavePanel getCurrentSavePanel(Component parentComponent) {
        if (parentComponent instanceof SavePanelFrame) {
            final Container contentPane = ((SavePanelFrame) parentComponent).getContentPane();
            if (contentPane instanceof SavePanel) {
                return (SavePanel) contentPane;
            }
        }
        if (parentComponent instanceof SavePanel) {
            return (SavePanel) parentComponent; // in the case of clicking the panels menu on the import error window, this will not be a savepanel.
        } else {
            return null;
        }
    }

    public String getSavePanelTitle(int selectedIndex) {
        return titleMap.get(diagramArray.get(selectedIndex));
    }

    @Override
    Component getDiagramAt(int diagramIndex) {
        return diagramArray.get(diagramIndex);
    }

    public void closeSavePanel(int selectedIndex) {
        final JFrame diagramFrame = diagramArray.get(selectedIndex);
        titleMap.remove(diagramArray.get(selectedIndex));
        diagramArray.remove(selectedIndex);
        diagramFrame.dispose();
        while (diagramArray.size() <= selectedIndex && selectedIndex > 0) {
            selectedIndex--;
        }
        setSelectedDiagram(selectedIndex);
    }

    public void setDiagramTitle(int diagramIndex, String diagramTitle) {
        titleMap.put(diagramArray.get(diagramIndex), diagramTitle);
        setWindowTitle(diagramArray.get(diagramIndex), diagramTitle);
    }

    @Override
    public Component[] getAllDiagrams() {
        return diagramArray.toArray(new Component[]{});
    }
}
