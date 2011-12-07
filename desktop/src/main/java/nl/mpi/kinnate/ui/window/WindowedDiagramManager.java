package nl.mpi.kinnate.ui.window;

import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.ui.menu.MainMenuBar;

/**
 *  Document   : DiagramWindowManager
 *  Created on : Dec 1, 2011, 4:03:01 PM
 *  Author     : Peter Withers
 */
public class WindowedDiagramManager extends AbstractDiagramManager {

    private HashMap<JFrame, String> titleMap = new HashMap<JFrame, String>();
    private ArrayList<JFrame> diagramArray = new ArrayList<JFrame>();

    public WindowedDiagramManager(ApplicationVersionManager versionManager, JFrame mainFrame) {
        super(versionManager, mainFrame);
        titleMap.put(mainFrame, mainFrame.getTitle());
        diagramArray.add(mainFrame);
    }

    @Override
    public void createDiagramContainer(String diagramTitle, Component diagramComponent) {
        JFrame diagramFame = new JFrame(diagramTitle);
        diagramFame.setJMenuBar(new MainMenuBar(this));
        diagramFame.setContentPane((Container) diagramComponent);
        titleMap.put(diagramFame, diagramTitle);
        diagramArray.add(diagramFame);
        setWindowIcon(diagramFame);
        diagramFame.doLayout();
        diagramFame.pack();
        diagramFame.setVisible(true);
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
