/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.kindocument.ProfileManager;
import nl.mpi.kinnate.svg.DataStoreSvg;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.ui.entityprofiles.CmdiProfileSelectionPanel;
import nl.mpi.kinnate.ui.kintypeeditor.KinTypeDefinitions;
import nl.mpi.kinnate.ui.relationsettings.RelationSettingsPanel;

/**
 * Created on : Jun 13, 2013, 3:59:38 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class DiagramSettingsPanel extends JPanel {

    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets");

    public DiagramSettingsPanel(KinDiagramPanel kinDiagramPanel, EntityCollection entityCollection, SessionStorage sessionStorage, GraphPanel graphPanel, ArbilWindowManager dialogHandler, TableCellDragHandler tableCellDragHandler) {
        super(new BorderLayout());
        this.setName(widgets.getString("DiagramSettingsPanel_DIAGRAM SETTINGS"));
        JTabbedPane jTabbedPane = new JTabbedPane();
        final ProfileManager profileManager = new ProfileManager(sessionStorage, graphPanel.dataStoreSvg.projectRecord, dialogHandler);
        final CmdiProfileSelectionPanel cmdiProfileSelectionPanel = new CmdiProfileSelectionPanel(widgets.getString("DiagramSettingsPanel_ENTITY PROFILES"), profileManager, graphPanel);
        profileManager.loadProfiles(false, cmdiProfileSelectionPanel, graphPanel);

//                    if (panelSetting == null) {
//                        panelSetting = graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.IndexerSettings, 150, showIndexerSettings);
//                    }
//        panelSetting.setHidePane(kinTypeHidePane, "Diagram Settings");
        graphPanel.getIndexParameters().symbolFieldsFields.setParent(graphPanel.getIndexParameters());
        graphPanel.getIndexParameters().labelFields.setParent(graphPanel.getIndexParameters());
        jTabbedPane.addTab(widgets.getString("DiagramSettingsPanel_KIN TYPE DEFINITIONS"), new KinTypeDefinitions(widgets.getString("DiagramSettingsPanel_KIN TYPE DEFINITIONS"), kinDiagramPanel, graphPanel.dataStoreSvg));
        jTabbedPane.addTab(widgets.getString("DiagramSettingsPanel_RELATION TYPE DEFINITIONS"), new RelationSettingsPanel(widgets.getString("DiagramSettingsPanel_RELATION TYPE DEFINITIONS"), kinDiagramPanel, graphPanel, dialogHandler));
        if (graphPanel.dataStoreSvg.diagramMode != DataStoreSvg.DiagramMode.FreeForm) {
            // hide some of the settings panels from freeform diagrams
            final JScrollPane symbolFieldsPanel = new JScrollPane(new FieldSelectionList(entityCollection, kinDiagramPanel, graphPanel.getIndexParameters().symbolFieldsFields, tableCellDragHandler));
            final JScrollPane labelFieldsPanel = new JScrollPane(new FieldSelectionList(entityCollection, kinDiagramPanel, graphPanel.getIndexParameters().labelFields, tableCellDragHandler));
            // todo: Ticket #1115 add overlay fields as paramters
            symbolFieldsPanel.setName(widgets.getString("DiagramSettingsPanel_SYMBOL FIELDS"));
            labelFieldsPanel.setName(widgets.getString("DiagramSettingsPanel_LABEL FIELDS"));
            jTabbedPane.addTab(widgets.getString("DiagramSettingsPanel_SYMBOL FIELDS"), symbolFieldsPanel);
            jTabbedPane.addTab(widgets.getString("DiagramSettingsPanel_LABEL FIELDS"), labelFieldsPanel);
            jTabbedPane.addTab(widgets.getString("DiagramSettingsPanel_ENTITY PROFILES"), cmdiProfileSelectionPanel);
        }
//        panelSetting.setMenuEnabled(true);
        this.add(jTabbedPane, BorderLayout.CENTER);
    }
}
