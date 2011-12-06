package nl.mpi.kinnate.ui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import nl.mpi.kinnate.ui.window.DiagramWindowManager;

/**
 *  Document   : WindowMenu
 *  Created on : Dec 6, 2011, 12:58:54 PM
 *  Author     : Peter Withers
 */
public class WindowMenu extends JMenu implements ActionListener {

    DiagramWindowManager diagramWindowManager;

    public WindowMenu(DiagramWindowManager diagramWindowManager) {
        this.setText("Window");
        this.diagramWindowManager = diagramWindowManager;
        this.addMenuListener(new MenuListener() {

            public void menuCanceled(MenuEvent evt) {
            }

            public void menuDeselected(MenuEvent evt) {
            }

            public void menuSelected(MenuEvent evt) {
                initMenu();
            }
        });
    }

    private void initMenu() {
        this.removeAll();
        int diagramCount = diagramWindowManager.getAllDiagrams().length;
        int selectedDiagramIndex = diagramWindowManager.getSavePanelIndex();
        for (int diagramCounter = 0; diagramCounter < diagramCount; diagramCounter++) {
            JCheckBoxMenuItem currentMenuItem = new JCheckBoxMenuItem(diagramWindowManager.getSavePanelTitle(diagramCounter));
            currentMenuItem.setActionCommand(Integer.toString(diagramCounter));
            currentMenuItem.addActionListener(this);
            currentMenuItem.setSelected(diagramCounter == selectedDiagramIndex);
            this.add(currentMenuItem);
        }
    }

    public void actionPerformed(ActionEvent e) {
        int diagramIndex = Integer.valueOf(e.getActionCommand());
        diagramWindowManager.setSelectedDiagram(diagramIndex);
    }
}
