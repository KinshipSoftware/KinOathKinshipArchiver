package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.kindata.VisiblePanelSetting;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 * Document : VisiblePanelSetting
 * Created on : Sept 25, 2011, 12:50:44 PM
 * Author : Peter Withers
 */
public class DiagramPanelsMenu extends JMenu implements ActionListener {

    private AbstractDiagramManager diagramWindowManager;
    private Component parentComponent;

    public DiagramPanelsMenu(AbstractDiagramManager diagramWindowManager, Component parentComponent) {
        this.diagramWindowManager = diagramWindowManager;
        this.parentComponent = parentComponent;
        this.setText("Panels");
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
        KinTermSavePanel kinTermPanel = diagramWindowManager.getKinTermPanel(parentComponent);
        if (kinTermPanel != null) {
            VisiblePanelSetting[] visiblePanelsArray = kinTermPanel.getVisiblePanels();
            Arrays.sort(visiblePanelsArray);
            for (VisiblePanelSetting panelSetting : visiblePanelsArray) {
                if (panelSetting.getPanelType() != null) {
                    menuItemsAdded = true;
                    JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(panelSetting.getDisplayName());
                    menuItem.setSelected(panelSetting.isPanelShown());
                    menuItem.setActionCommand(panelSetting.getPanelType().name());
                    menuItem.addActionListener(this);
                    menuItem.setEnabled(panelSetting.isMenuEnabled());
                    this.add(menuItem);
                }
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
        KinTermSavePanel kinTermPanel = diagramWindowManager.getKinTermPanel(parentComponent);
        if (kinTermPanel != null) {
            for (VisiblePanelSetting panelSetting : kinTermPanel.getVisiblePanels()) {
                if (selectedPanelType.equals(panelSetting.getPanelType())) {
                    panelSetting.setPanelShown(!panelSetting.isPanelShown());
                    // todo: this should set the added pannel as the active panel (note that there can be multiple added)
                }
            }
        }
    }
}
