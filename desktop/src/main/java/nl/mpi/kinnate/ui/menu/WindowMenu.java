/**
 * Copyright (C) 2012 The Language Archive
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
