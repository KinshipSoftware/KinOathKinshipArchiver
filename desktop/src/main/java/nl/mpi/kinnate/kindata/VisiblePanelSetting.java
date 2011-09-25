package nl.mpi.kinnate.kindata;

import javax.xml.bind.annotation.XmlAttribute;

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
        MetaData,
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

    public VisiblePanelSetting() {
    }

    public VisiblePanelSetting(PanelType panelType, boolean panelShown, int panelWidth) {
        this.panelType = panelType;
        this.panelShown = panelShown;
        this.panelWidth = panelWidth;
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
