package nl.mpi.kinnate.kindata;

import java.awt.Component;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import nl.mpi.kinnate.ui.HidePane;

/**
 *  Document   : VisiblePanelSetting
 *  Created on : Sept 25, 2011, 12:02:44 PM
 *  Author     : Peter Withers
 */
public class VisiblePanelSetting {

    public enum PanelType {

        KinTypeStrings,
        KinTerms,
        ArchiveLinker,
//        @Deprecated
//        MetaData,
        IndexerSettings,
        DiagramTree,
        EntitySearch
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
    private Component targetPanel;

    public VisiblePanelSetting() {
    }

    public VisiblePanelSetting(PanelType panelType, boolean panelShown, int panelWidth) {
        this.panelType = panelType;
        this.panelShown = panelShown;
        this.panelWidth = panelWidth;
    }

    private void setUpdateUiState() {
        if (panelShown) {
            hidePane.addTab(this);
        } else {
            hidePane.remove(this);
        }
    }

    public void setTargetPanel(HidePane hidePane, Component targetPanel, String displayName) {
        this.hidePane = hidePane;
        this.targetPanel = targetPanel;
        this.displayName = displayName;
        setUpdateUiState();
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

    public Component getTargetPanel() {
        return targetPanel;
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
}
