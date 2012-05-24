package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 * Document : WindowMenu
 * Created on : Dec 6, 2011, 12:58:54 PM
 * Author : Peter Withers
 */
public class WindowMenu extends JMenu implements ActionListener {

    AbstractDiagramManager diagramWindowManager;

    public WindowMenu(AbstractDiagramManager diagramWindowManager, final Component parentComponent) {
        this.setText("Window");
        this.diagramWindowManager = diagramWindowManager;
        this.addMenuListener(new MenuListener() {

            public void menuCanceled(MenuEvent evt) {
            }

            public void menuDeselected(MenuEvent evt) {
            }

            public void menuSelected(MenuEvent evt) {
                initMenu(parentComponent);
            }
        });
    }

    private void initMenu(Component parentComponent) {
        this.removeAll();
        int diagramCount = diagramWindowManager.getAllDiagrams().length;
        int selectedDiagramIndex = diagramWindowManager.getSavePanelIndex(parentComponent);
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
