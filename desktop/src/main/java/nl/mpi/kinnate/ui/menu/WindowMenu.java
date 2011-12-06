package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
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
    ArrayList<Component> diagramArray = new ArrayList<Component>();

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
        for (Component currentDiagram : diagramWindowManager.getAllDiagrams()) {
            diagramArray.add(currentDiagram);
            JMenuItem currentMenuItem = new JMenuItem(currentDiagram.getName());
            currentMenuItem.setActionCommand(Integer.toString(diagramArray.size() - 1));
            currentMenuItem.addActionListener(this);
            this.add(currentMenuItem);
        }
    }

    public void actionPerformed(ActionEvent e) {
        int diagramIndex = Integer.getInteger(e.getActionCommand());
        Component diagramComponent = diagramArray.get(diagramIndex);
        diagramWindowManager.setSelectedDiagram(diagramComponent);
    }
}
