/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
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
package nl.mpi.kinnate.kindata;

import java.awt.Component;
import java.util.HashSet;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import nl.mpi.kinnate.ui.HidePane;

/**
 * Document : VisiblePanelSetting
 * Created on : Sept 25, 2011, 12:02:44 PM
 * Author : Peter Withers
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
        ExportPanel,
        PluginPanel
    }
    @XmlAttribute(name = "type", namespace = "http://mpi.nl/tla/kin")
    PanelType panelType = null;
    @XmlAttribute(name = "show", namespace = "http://mpi.nl/tla/kin")
    boolean panelShown;
    @XmlAttribute(name = "width", namespace = "http://mpi.nl/tla/kin")
    int panelWidth;
    @XmlTransient
    boolean menuEnabled = true;
    @XmlTransient
    private String displayName = "";
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

    public void setMenuEnabled(boolean menuEnabled) {
        this.menuEnabled = menuEnabled;
    }

    public boolean isMenuEnabled() {
        return menuEnabled;
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
        if (getDisplayName() == null) {
            if (o.getDisplayName() == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (o.getDisplayName() == null) {
            return -1;
        } else {
            return getDisplayName().compareToIgnoreCase(o.getDisplayName());
        }
    }
}
