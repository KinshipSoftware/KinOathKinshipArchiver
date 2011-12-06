package nl.mpi.kinnate.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.svg.MouseListenerSvg;

/**
 *  Document   : SvgKeyListener
 *  Created on : May 27, 2011, 1:01:02 PM
 *  Author     : Peter Withers
 */
public class EditMenu extends JMenu implements ActionListener {
    //    ctrl + a = select all
    //    ctrl + r = select related
    //    ctrl + e = expand selection
    //    ctrl + d = deselect all

    JMenuItem selectAllMenu = null;
    JMenuItem selectRelatedMenu = null;
    JMenuItem expandSelectionMenu = null;
    JMenuItem deselectAllMenu = null;
    SavePanel menuSavePanel = null;
    DiagramWindowManager diagramWindowManager;

    public EditMenu(DiagramWindowManager diagramWindowManager) {
        this.diagramWindowManager = diagramWindowManager;
        this.setText("Edit");
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
        selectAllMenu = new JMenuItem("Select All");
        selectAllMenu.setActionCommand(MouseListenerSvg.ActionCode.selectAll.name());
        selectAllMenu.addActionListener(EditMenu.this);
        selectAllMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectRelatedMenu = new JMenuItem("Select Related");
        selectRelatedMenu.setActionCommand(MouseListenerSvg.ActionCode.selectRelated.name());
        selectRelatedMenu.addActionListener(EditMenu.this);
        selectRelatedMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        expandSelectionMenu = new JMenuItem("Expand Selection");
        expandSelectionMenu.setActionCommand(MouseListenerSvg.ActionCode.expandSelection.name());
        expandSelectionMenu.addActionListener(EditMenu.this);
        expandSelectionMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));

        deselectAllMenu = new JMenuItem("Deselect All");
        deselectAllMenu.setActionCommand(MouseListenerSvg.ActionCode.deselectAll.name());
        deselectAllMenu.addActionListener(EditMenu.this);
        deselectAllMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        EditMenu.this.add(selectAllMenu);
        EditMenu.this.add(selectRelatedMenu);
        EditMenu.this.add(expandSelectionMenu);
        EditMenu.this.add(deselectAllMenu);
    }

    private void initMenu() {
        menuSavePanel = diagramWindowManager.getCurrentSavePanel();
        boolean savePanelFocused = menuSavePanel != null;
        selectAllMenu.setEnabled(savePanelFocused);
        selectRelatedMenu.setEnabled(savePanelFocused);
        expandSelectionMenu.setEnabled(false); // graphPanelFocused); // removed until the code is completed
        deselectAllMenu.setEnabled(savePanelFocused);
    }

    protected void enableMenuKeys() {
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
            menuSavePanel = diagramWindowManager.getCurrentSavePanel();
        }
        if (menuSavePanel != null) {
            menuSavePanel.doActionCommand(MouseListenerSvg.ActionCode.valueOf(e.getActionCommand()));
        }
        enableMenuKeys();
    }
}
