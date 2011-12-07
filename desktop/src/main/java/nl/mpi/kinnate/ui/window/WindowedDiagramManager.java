package nl.mpi.kinnate.ui.window;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.SavePanel;

/**
 *  Document   : DiagramWindowManager
 *  Created on : Dec 1, 2011, 4:03:01 PM
 *  Author     : Peter Withers
 */
public class WindowedDiagramManager extends AbstractDiagramManager {

    private HashMap<JFrame, String> titleMap = new HashMap<JFrame, String>();
    private ArrayList<JFrame> diagramArray = new ArrayList<JFrame>();

    public WindowedDiagramManager(ApplicationVersionManager versionManager) {
        super(versionManager);
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
                    windowFrame.dispose();
                }
            } else {
                closeSavePanel(selectedIndex);
                windowFrame.dispose();
            }
        }
    }

    @Override
    public void createDiagramContainer(String diagramTitle, Component diagramComponent) {
        JFrame diagramWindow = super.createDiagramWindow(diagramTitle, diagramComponent);
        titleMap.put(diagramWindow, diagramTitle);
        diagramArray.add(diagramWindow);
    }

    @Override
    public void createDiagramSubPanel(String diagramTitle, Component diagramComponent) {
        int currentDiagramIndex = getSavePanelIndex();
        JFrame diagramFame = (JFrame) getDiagramAt(currentDiagramIndex);
        Component currentComponent = diagramFame.getContentPane();
        JTabbedPane tabbedPane;
        if (!(currentComponent instanceof JTabbedPane)) {
            tabbedPane = new JTabbedPane();
            final String savePanelTitle = getSavePanelTitle(currentDiagramIndex);
            tabbedPane.addTab(savePanelTitle, currentComponent);
            diagramFame.setContentPane(tabbedPane);
        } else {
            tabbedPane = (JTabbedPane) currentComponent;
        }
        tabbedPane.addTab(diagramTitle, diagramComponent);
    }

    @Override
    Component getSelectedDiagram() {
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
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
        titleMap.put(diagramArray.get(diagramIndex), diagramTitle);
        setWindowTitle(diagramArray.get(diagramIndex), diagramTitle);
    }

    @Override
    public Component[] getAllDiagrams() {
        return diagramArray.toArray(new Component[]{});
    }
}
