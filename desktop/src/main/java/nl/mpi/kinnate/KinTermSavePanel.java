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

    public int getKinTermGroupCount();

    public void importKinTerms();

    public void exportKinTerms();

    public void setSelectedKinTypeSting(String kinTypeStrings);
}
