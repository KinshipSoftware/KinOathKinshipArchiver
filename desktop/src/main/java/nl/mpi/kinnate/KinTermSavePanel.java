package nl.mpi.kinnate;

import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.VisiblePanelSetting;

/**
 *  Document   : KinTermPanel
 *  Created on : Apr 1, 2011, 9:41:11 AM
 *  Author     : Peter Withers
 */
public interface KinTermSavePanel {
    // todo: the requirements for this have change and much of it should probably be merged with savepanel and the rest made more generic

    public VisiblePanelSetting[] getVisiblePanels();

    public void setPanelState(VisiblePanelSetting.PanelType panelType, boolean panelVisible);

    public boolean getPanelState(VisiblePanelSetting.PanelType panelType);

    public EntityData[] getGraphEntities();

    public void addKinTermGroup();

    public void importKinTerms();

    public void exportKinTerms();

    public void setSelectedKinTypeSting(String kinTypeStrings);
}
