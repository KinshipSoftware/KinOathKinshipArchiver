package nl.mpi.kinnate.kindata;

import java.awt.Component;
import java.util.HashSet;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import nl.mpi.kinnate.ui.HidePane;

/**
 *  Document   : VisiblePanelSetting
 *  Created on : Sept 25, 2011, 12:02:44 PM
 *  Author     : Peter Withers
 */
public class VisiblePanelSetting implements Comparable<VisiblePanelSetting> {

    public enum PanelType {

        KinTypeStrings,
        KinTerms,
        ArchiveLinker,
        //        @Deprecated
        //        MetaData,
        IndexerSettings,
        DiagramTree,
        EntitySearch,
        ExportPanel
    }
    @XmlAttribute(name = "type", namespace = "http://mpi.nl/tla/kin")
    PanelType panelType;
    @XmlAttribute(name = "show", namespace = "http://mpi.nl/tla/kin")
    boolean panelShown;
    @XmlAttribute(name = "width", namespace = "http://mpi.nl/tla/kin")
    int panelWidth;
    @XmlTransient
    private String displayName;
    @XmlTransient
    private HidePane hidePane;
    @XmlTransient
    private HashSet<Component> targetPanel = new HashSet<Component>();

    public VisiblePanelSetting() {
    }

    public VisiblePanelSetting(PanelType panelType, boolean panelShown, int panelWidth) {
        this.panelType = panelType;
        this.panelShown = panelShown;
        this.panelWidth = panelWidth;
    }

    public void setHidePane(HidePane hidePane, String displayName) {
        this.hidePane = hidePane;
        this.displayName = displayName;
    }

    private void setUpdateUiState() {
        if (panelShown) {
            for (Component currentPanel : getTargetPanels()) {
                String tabStringName = currentPanel.getName();
                if (tabStringName == null || tabStringName.length() < 1) {
                    tabStringName = displayName;
                }
                hidePane.addTab(this, tabStringName, currentPanel);
            }
        } else {
            hidePane.remove(this);
        }
    }

    public void addTargetPanel(Component targetPanel, boolean setAsSelected) {
        this.targetPanel.add(targetPanel);
        setUpdateUiState();
        if (setAsSelected) {
            hidePane.setSelectedComponent(targetPanel);
        }
    }

    public void removeTargetPanel(Component targetPanel) {
        this.targetPanel.remove(targetPanel);
        hidePane.removeTab(targetPanel);
    }

    public boolean isPanelShown() {
        return panelShown;
    }

    @XmlTransient
    public void setPanelShown(boolean panelShown) {
        this.panelShown = panelShown;
        setUpdateUiState();
    }

    @XmlTransient
    public void setPanelWidth(int panelWidth) {
        this.panelWidth = panelWidth;
    }

    public int getPanelWidth() {
        return panelWidth;
    }

    @XmlTransient
    public Component[] getTargetPanels() {
        return targetPanel.toArray(new Component[]{});
    }

    public String getDisplayName() {
        return displayName;
    }

    public PanelType getPanelType() {
        return panelType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VisiblePanelSetting other = (VisiblePanelSetting) obj;
        if (this.panelType != other.panelType) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.panelType != null ? this.panelType.hashCode() : 0);
        return hash;
    }

    public int compareTo(VisiblePanelSetting o) {
        return getDisplayName().compareToIgnoreCase(o.getDisplayName());
    }
}
