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
import java.util.ResourceBundle;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.svg.MouseListenerSvg;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 * Document : SvgKeyListener
 * Created on : May 27, 2011, 1:01:02 PM
 * Author : Peter Withers
 */
public class EditMenu extends JMenu implements ActionListener {
    private static final ResourceBundle menus = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Menus");
    //    ctrl + a = select all
    //    ctrl + r = select related
    //    ctrl + e = expand selection
    //    ctrl + d = deselect all

    JMenuItem selectAllMenu = null;
    JMenuItem selectRelatedMenu = null;
    JMenuItem expandSelectionMenu = null;
    JMenuItem deselectAllMenu = null;
    JMenuItem recalculateDiagramMenuItem = null;
    SavePanel menuSavePanel = null;
    AbstractDiagramManager diagramWindowManager;
    private Component parentComponent;
    
    public EditMenu(AbstractDiagramManager diagramWindowManager, Component parentComponent) {
        this.diagramWindowManager = diagramWindowManager;
        this.parentComponent = parentComponent;
        this.setText(menus.getString("EDIT"));
        this.addMenuListener(new MenuListener() {
            
            public void menuCanceled(MenuEvent evt) {
                enableMenuKeys();
            }
            
            public void menuDeselected(MenuEvent evt) {
                enableMenuKeys();
            }
            
            public void menuSelected(MenuEvent evt) {
                initMenu();
            }
        });
        selectAllMenu = new JMenuItem(menus.getString("SELECT ALL"));
        selectAllMenu.setActionCommand(MouseListenerSvg.ActionCode.selectAll.name());
        selectAllMenu.addActionListener(EditMenu.this);
        selectAllMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectRelatedMenu = new JMenuItem(menus.getString("SELECT RELATED"));
        selectRelatedMenu.setActionCommand(MouseListenerSvg.ActionCode.selectRelated.name());
        selectRelatedMenu.addActionListener(EditMenu.this);
        selectRelatedMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        expandSelectionMenu = new JMenuItem(menus.getString("EXPAND SELECTION"));
        expandSelectionMenu.setActionCommand(MouseListenerSvg.ActionCode.expandSelection.name());
        expandSelectionMenu.addActionListener(EditMenu.this);
        expandSelectionMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        
        deselectAllMenu = new JMenuItem(menus.getString("DESELECT ALL"));
        deselectAllMenu.setActionCommand(MouseListenerSvg.ActionCode.deselectAll.name());
        deselectAllMenu.addActionListener(EditMenu.this);
        deselectAllMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        
        recalculateDiagramMenuItem = new JMenuItem(menus.getString("RECALCULATE THE DIAGRAM"));
        recalculateDiagramMenuItem.setActionCommand("RecalculateDiagram");
        recalculateDiagramMenuItem.addActionListener(EditMenu.this);
        
        EditMenu.this.add(selectAllMenu);
        EditMenu.this.add(selectRelatedMenu);
        EditMenu.this.add(expandSelectionMenu);
        EditMenu.this.add(deselectAllMenu);
        EditMenu.this.add(recalculateDiagramMenuItem);
        enableMenuKeys();
    }
    
    private void initMenu() {
        menuSavePanel = diagramWindowManager.getCurrentSavePanel(parentComponent);
        boolean savePanelFocused = menuSavePanel != null;
        selectAllMenu.setEnabled(savePanelFocused);
        selectRelatedMenu.setEnabled(savePanelFocused);
        expandSelectionMenu.setEnabled(savePanelFocused);
        deselectAllMenu.setEnabled(savePanelFocused);
        recalculateDiagramMenuItem.setEnabled(savePanelFocused);
    }
    
    private void enableMenuKeys() {
        menuSavePanel = null;
        selectAllMenu.setEnabled(true);
        selectRelatedMenu.setEnabled(true);
        expandSelectionMenu.setEnabled(true);
        deselectAllMenu.setEnabled(true);
    }

//    private SavePanel getGraphPanel() {
//        return diagramWindowManager.getCurrentSavePanel();
//        // todo: this might not be the best way to do this when the tab change event could be used
//        // todo: this could be casuing issues with the keyboard short cuts when the needed menu items are dissabled
////        Component focusedComponent = FocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
////        GraphPanel graphPanel = null;
////        while (focusedComponent != null) {
//////            System.out.println("focus on: " + focusedComponent.toString());
////            if (focusedComponent instanceof GraphPanel) {
//////                System.out.println("bingo");
////                graphPanel = (GraphPanel) focusedComponent;
////                break;
////            }
////            focusedComponent = focusedComponent.getParent();
////        }
////        return graphPanel;
//    }
    public void actionPerformed(ActionEvent e) {
        if (menuSavePanel == null) {
            // if this is a menu action then menuSavePanel was set as the menu was shown, if this is a key event then the current save panel must be abtained
            menuSavePanel = diagramWindowManager.getCurrentSavePanel(parentComponent);
        }
        if (menuSavePanel != null) {
            if (e.getActionCommand().equals("RecalculateDiagram")) {
                menuSavePanel.updateGraph();
            } else {
                menuSavePanel.doActionCommand(MouseListenerSvg.ActionCode.valueOf(e.getActionCommand()));
            }
        }
        enableMenuKeys();
    }
}
