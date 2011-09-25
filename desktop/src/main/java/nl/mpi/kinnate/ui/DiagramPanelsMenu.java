package nl.mpi.kinnate.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.kindata.VisiblePanelSetting;

/**
 *  Document   : VisiblePanelSetting
 *  Created on : Sept 25, 2011, 12:50:44 PM
 *  Author     : Peter Withers
 */
public class DiagramPanelsMenu extends JMenu implements ActionListener {

    MainFrame mainFrame;

    public DiagramPanelsMenu(MainFrame mainFrameLocal) {
        mainFrame = mainFrameLocal;
        this.setText("View");
        this.addMenuListener(new MenuListener() {

            public void menuSelected(MenuEvent e) {
                setupMenuItems();
            }

            public void menuDeselected(MenuEvent e) {
            }

            public void menuCanceled(MenuEvent e) {
            }
        });
    }

    private void setupMenuItems() {
        this.removeAll();
        boolean menuItemsAdded = false;
        KinTermSavePanel kinTermPanel = mainFrame.getKinTermPanel();
        if (kinTermPanel != null) {
            for (VisiblePanelSetting panelSetting : kinTermPanel.getVisiblePanels()) {
                menuItemsAdded = true;
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(panelSetting.getDisplayName());
                menuItem.setSelected(panelSetting.isPanelShown());
                menuItem.setActionCommand(panelSetting.getPanelType().name());
                menuItem.addActionListener(this);
                this.add(menuItem);
            }
        }
        if (menuItemsAdded == false) {
            JMenuItem noItemsMenu = new JMenuItem("<no items available in this context>");
            noItemsMenu.setEnabled(false);
            this.add(noItemsMenu);
        }
    }

    public void actionPerformed(ActionEvent e) {
        VisiblePanelSetting.PanelType selectedPanelType = VisiblePanelSetting.PanelType.valueOf(e.getActionCommand());
        KinTermSavePanel kinTermPanel = mainFrame.getKinTermPanel();
        if (kinTermPanel != null) {
            for (VisiblePanelSetting panelSetting : kinTermPanel.getVisiblePanels()) {
                if (panelSetting.getPanelType().equals(selectedPanelType)) {
                    panelSetting.setPanelShown(!panelSetting.isPanelShown());
                }
            }
        }
    }
}
