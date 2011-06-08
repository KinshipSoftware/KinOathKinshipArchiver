package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.FocusManager;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import nl.mpi.kinnate.svg.GraphPanel;
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
    GraphPanel initialGraphPanel = null;

    public EditMenu() {
        this.addMenuListener(new MenuListener() {

            public void menuCanceled(MenuEvent evt) {
                enableMenuKeys();
            }

            public void menuDeselected(MenuEvent evt) {
//                enableMenuKeys();
            }

            public void menuSelected(MenuEvent evt) {
                initMenu();
            }
        });
    }

    private void initMenu() {
        initialGraphPanel = getGraphPanel();
        boolean graphPanelFocused = initialGraphPanel != null;
        selectAllMenu.setEnabled(graphPanelFocused);
        selectRelatedMenu.setEnabled(graphPanelFocused);
        expandSelectionMenu.setEnabled(false); // graphPanelFocused); // removed until the code is completed
        deselectAllMenu.setEnabled(graphPanelFocused);
    }

    protected void enableMenuKeys() {
        initialGraphPanel = null;
        if (selectAllMenu == null) {
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
        selectAllMenu.setEnabled(true);
        selectRelatedMenu.setEnabled(true);
        expandSelectionMenu.setEnabled(true);
        deselectAllMenu.setEnabled(true);
    }

    private GraphPanel getGraphPanel() {
        Component focusedComponent = FocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        GraphPanel graphPanel = null;
        while (focusedComponent != null) {
//            System.out.println("focus on: " + focusedComponent.toString());
            if (focusedComponent instanceof GraphPanel) {
//                System.out.println("bingo");
                graphPanel = (GraphPanel) focusedComponent;
                break;
            }
            focusedComponent = focusedComponent.getParent();
        }
        return graphPanel;
    }

    public void actionPerformed(ActionEvent e) {
        GraphPanel graphPanel = getGraphPanel();
        if (graphPanel == null) {
            //  initialGraphPanel is set before the menu is shown and should never be set if this is a key action 
            graphPanel = initialGraphPanel;
        }
        if (graphPanel != null) {
            new MouseListenerSvg(graphPanel).performMenuAction(MouseListenerSvg.ActionCode.valueOf(e.getActionCommand()));
        }
        enableMenuKeys();
    }
}
