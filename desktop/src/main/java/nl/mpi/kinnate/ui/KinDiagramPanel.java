/**
 * Copyright (C) 2012 The Language Archive
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
import java.awt.Component;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.flap.module.BaseModule;
import nl.mpi.flap.plugin.KinOathPanelPlugin;
import nl.mpi.flap.plugin.PluginException;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityService;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.entityindexer.ProcessAbortException;
import nl.mpi.kinnate.entityindexer.QueryParser;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kindata.VisiblePanelSetting;
import nl.mpi.kinnate.kindata.VisiblePanelSetting.PanelType;
import nl.mpi.kinnate.kindocument.ProfileManager;
import nl.mpi.kinnate.kintypestrings.ImportRequiredException;
import nl.mpi.kinnate.kintypestrings.KinTermCalculator;
import nl.mpi.kinnate.kintypestrings.KinTermGroup;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.projects.ProjectManager;
import nl.mpi.kinnate.projects.ProjectRecord;
import nl.mpi.kinnate.svg.DataStoreSvg.DiagramMode;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.svg.MouseListenerSvg;
import nl.mpi.kinnate.ui.entityprofiles.CmdiProfileSelectionPanel;
import nl.mpi.kinnate.ui.kintypeeditor.KinTypeDefinitions;
import nl.mpi.kinnate.ui.menu.DocumentNewMenu.DocumentType;
import nl.mpi.kinnate.ui.relationsettings.RelationSettingsPanel;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : KinTypeStringTestPanel Created on : Sep 29, 2010, 12:52:01 PM
 * Author : Peter Withers
 */
public class KinDiagramPanel extends JPanel implements SavePanel, KinTermSavePanel, ArbilDataNodeContainer {

    private ProjectManager projectManager;
    private EntityCollection entityCollection;
    private KinTypeStringInput kinTypeStringInput;
    private ArrayList<KinTypeStringProvider> kinTypeStringProviders;
    private GraphPanel graphPanel;
    private EgoSelectionPanel egoSelectionPanel;
    private ProjectTreePanel projectTree = null;
    private ArchiveEntityLinkerPanel archiveEntityLinkerPanelRemote;
    private ArchiveEntityLinkerPanel archiveEntityLinkerPanelLocal;
    private ArchiveEntityLinkerPanel archiveEntityLinkerPanelMpiRemote;
    private HidePane kinTermHidePane;
    private HidePane kinTypeHidePane;
    private EntityService entityIndex;
    private JProgressBar progressBar;
    private HashMap<ArbilDataNode, UniqueIdentifier> registeredArbilDataNode;
    private HashMap<ArbilNode, Boolean> arbilDataNodesChangedStatus;
//    private ArrayList<ArbilTree> treeLoadQueue = new ArrayList<ArbilTree>();
    private SessionStorage sessionStorage;
    private ArbilWindowManager dialogHandler;
    private ArbilDataNodeLoader dataNodeLoader;
    private ArbilTreeHelper treeHelper;
    private KinDragTransferHandler dragTransferHandler;
    private AbstractDiagramManager diagramWindowManager;
    private StatusBar statusBar;

    public KinDiagramPanel(URI existingFile, boolean savableType, ProjectRecord projectRecord, SessionStorage sessionStorage, ArbilWindowManager dialogHandler, ArbilDataNodeLoader dataNodeLoader, ArbilTreeHelper treeHelper, ProjectManager projectManager, AbstractDiagramManager diagramWindowManager) throws EntityServiceException {
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
        this.dataNodeLoader = dataNodeLoader;
        this.treeHelper = treeHelper;
        this.diagramWindowManager = diagramWindowManager;
        this.projectManager = projectManager;
        initKinDiagramPanel(existingFile, null, savableType, projectRecord);
    }

    public KinDiagramPanel(DocumentType documentType, SessionStorage sessionStorage, ArbilWindowManager dialogHandler, ArbilDataNodeLoader dataNodeLoader, ArbilTreeHelper treeHelper, ProjectManager projectManager, AbstractDiagramManager diagramWindowManager) throws EntityServiceException {
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
        this.dataNodeLoader = dataNodeLoader;
        this.treeHelper = treeHelper;
        this.diagramWindowManager = diagramWindowManager;
        this.projectManager = projectManager;
        //ProjectRecord projectRecord, 
        initKinDiagramPanel(null, documentType, false, null);
    }

    public KinDiagramPanel(DocumentType documentType, SessionStorage sessionStorage, ArbilWindowManager dialogHandler, ArbilDataNodeLoader dataNodeLoader, ArbilTreeHelper treeHelper, EntityCollection entityCollection, AbstractDiagramManager diagramWindowManager) throws EntityServiceException {
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
        this.dataNodeLoader = dataNodeLoader;
        this.treeHelper = treeHelper;
        this.entityCollection = entityCollection; // we are setting the entity collection here because it has the project that has just been imported to and we want to show the results
        this.diagramWindowManager = diagramWindowManager;
        initKinDiagramPanel(null, documentType, false, null);
    }

    private void initKinDiagramPanel(URI existingFile, DocumentType documentType, boolean savableType, ProjectRecord projectRecord) throws EntityServiceException {
        progressBar = new JProgressBar();
        graphPanel = new GraphPanel(this, dialogHandler, sessionStorage, dataNodeLoader);
        kinTypeStringInput = new KinTypeStringInput(graphPanel.dataStoreSvg);

        boolean showKinTerms = false;
        boolean showArchiveLinker = false;
        boolean showDiagramTree = false;
        boolean showEntitySearch = false;
        boolean showIndexerSettings = false;
        boolean showKinTypeStrings = false;
        boolean showExportPanel = false;
        boolean showMetaData = false;

        if (existingFile != null) {
            graphPanel.readSvg(existingFile, savableType);
            String kinTermContents = null;
            for (String currentKinTypeString : graphPanel.getKinTypeStrigs()) {
                if (currentKinTypeString.trim().length() > 0) {
                    if (kinTermContents == null) {
                        kinTermContents = "";
                    } else {
                        kinTermContents = kinTermContents + "\n";
                    }
                    kinTermContents = kinTermContents + currentKinTypeString.trim();
                }
            }
            kinTypeStringInput.setText(kinTermContents);
        } else {
            kinTypeStringInput.setDefaultText();
            if (documentType == null) {
                // this is the default document that users see when they run the application for the first time
                documentType = DocumentType.Simple;
            }
            switch (documentType) {
                case ArchiveLinker:
                    showMetaData = true;
                    showDiagramTree = true;
                    showArchiveLinker = true;
                    graphPanel.dataStoreSvg.diagramMode = DiagramMode.KinTypeQuery;
                    break;
//                case CustomQuery:
//                    showMetaData = true;
//                    showKinTypeStrings = true;
//                    showDiagramTree = true;
//                    showIndexerSettings = true;
//                    graphPanel.dataStoreSvg.diagramMode = DiagramMode.KinTypeQuery;
//                    break;
//                case EntitySearch:
//                    showMetaData = true;
//                    showEntitySearch = true;
//                    showDiagramTree = true;
//                    graphPanel.dataStoreSvg.diagramMode = DiagramMode.KinTypeQuery;
//                    break;
                case KinTerms:
                    showKinTerms = true;
                    graphPanel.addKinTermGroup();
                    graphPanel.dataStoreSvg.diagramMode = DiagramMode.FreeForm;
                    break;
                case Freeform:
//                    showDiagramTree = true;
                    showKinTypeStrings = true;
                    graphPanel.dataStoreSvg.diagramMode = DiagramMode.FreeForm;
                    break;
                case Simple:
                    showMetaData = true;
                    showDiagramTree = true;
                    showEntitySearch = true;
                    graphPanel.dataStoreSvg.diagramMode = DiagramMode.KinTypeQuery;
                    break;
                case Query:
                    showMetaData = true;
                    showDiagramTree = true;
                    showKinTypeStrings = true;
                    graphPanel.dataStoreSvg.diagramMode = DiagramMode.KinTypeQuery;
                default:
                    break;
            }
            graphPanel.generateDefaultSvg();
        }
        // todo: resove the context menu actions on a free form diagram that require the entitycollection to exist
//        if (graphPanel.dataStoreSvg.diagramMode != DiagramMode.FreeForm) {
//        if (graphPanel.dataStoreSvg.projectRecord == null) {
        if (entityCollection != null) {
            // if an existing entity collection has been passed in, then set the project from it
            graphPanel.dataStoreSvg.projectRecord = entityCollection.getProjectRecord();
        } else {
            if (projectRecord != null) {
                graphPanel.dataStoreSvg.projectRecord = projectRecord;
            } else if (graphPanel.dataStoreSvg.projectRecord == null) {
                graphPanel.dataStoreSvg.projectRecord = projectManager.getDefaultProject(sessionStorage);
            }
            entityCollection = projectManager.getEntityCollectionForProject(graphPanel.dataStoreSvg.projectRecord);
        }
        graphPanel.setEntityCollection(entityCollection);
        try {
            projectManager.addRecentProjectRecord(graphPanel.dataStoreSvg.projectRecord);
        } catch (JAXBException exception) {
            dialogHandler.addMessageDialogToQueue("Failed to save the project in the recent list: " + exception.getMessage(), "Recent Project List Error");
        }
//        } else {
//            // do not store the project settings for a free form diagram but make sure the defalut project is available for importing
//            entityCollection = new EntityCollection(projectManager.getDefaultProject(sessionStorage));
//        }
        this.setLayout(new BorderLayout());

        progressBar.setVisible(false);
        graphPanel.add(progressBar, BorderLayout.PAGE_START);


        registeredArbilDataNode = new HashMap<ArbilDataNode, UniqueIdentifier>();
        arbilDataNodesChangedStatus = new HashMap<ArbilNode, Boolean>();
        egoSelectionPanel = new EgoSelectionPanel(this, graphPanel, dialogHandler, entityCollection, dataNodeLoader);
//        kinTermPanel = new KinTermTabPane(this, graphPanel.getkinTermGroups());

//        kinTypeStringInput.setText(defaultString);

        JPanel kinGraphPanel = new JPanel(new BorderLayout());

        kinTypeHidePane = new HidePane(HidePane.HidePanePosition.top, 0);

        HidePane tableHidePane = new HidePane(HidePane.HidePanePosition.bottom, 150);

        dragTransferHandler = new KinDragTransferHandler(this, sessionStorage, entityCollection);
        graphPanel.setTransferHandler(dragTransferHandler);
        egoSelectionPanel.setTransferHandler(dragTransferHandler);
        if (graphPanel.dataStoreSvg.diagramMode == DiagramMode.KinTypeQuery) {
            projectTree = new ProjectTreePanel(entityCollection, "Project Tree", this, graphPanel, dialogHandler, dataNodeLoader);
            projectTree.setTransferHandler(dragTransferHandler);
        }

        EntitySearchPanel entitySearchPanel = new EntitySearchPanel(entityCollection, this, graphPanel, dialogHandler, dataNodeLoader);
        entitySearchPanel.setTransferHandler(dragTransferHandler);

        HidePane egoSelectionHidePane = new HidePane(HidePane.HidePanePosition.left, 0);

        kinTermHidePane = new HidePane(HidePane.HidePanePosition.right, 0);

        TableCellDragHandler tableCellDragHandler = new TableCellDragHandler();
        graphPanel.setArbilTableModel(new MetadataPanel(graphPanel, entityCollection, this, tableHidePane, tableCellDragHandler, dataNodeLoader, null, sessionStorage, dialogHandler, null, null)); // todo: pass a ImageBoxRenderer here if you want thumbnails
        // in some older files and non kinoath files these VisiblePanelSettings would not be set, so we make sure that they are here
        final ProfileManager profileManager = new ProfileManager(sessionStorage, dialogHandler);
        final CmdiProfileSelectionPanel cmdiProfileSelectionPanel = new CmdiProfileSelectionPanel("Entity Profiles", profileManager, graphPanel);
        profileManager.loadProfiles(false, cmdiProfileSelectionPanel, graphPanel);
        for (PanelType panelType : PanelType.values()) {
            VisiblePanelSetting panelSetting = graphPanel.dataStoreSvg.getPanelSettingByType(panelType);
            switch (panelType) {
                case ArchiveLinker:
                    if (panelSetting == null) {
                        panelSetting = graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.ArchiveLinker, 150, showArchiveLinker);
                    }
                    panelSetting.setHidePane(kinTermHidePane, "Archive Linker"); // todo: this name is overwriting the correct tab titles
                    if (treeHelper.getRemoteCorpusNodes().length > 0) {
                        archiveEntityLinkerPanelRemote = new ArchiveEntityLinkerPanel(panelSetting, this, graphPanel, dragTransferHandler, ArchiveEntityLinkerPanel.TreeType.RemoteTree, treeHelper, dataNodeLoader);
                        panelSetting.addTargetPanel(archiveEntityLinkerPanelRemote, false);
                    }
                    if (treeHelper.getLocalCorpusNodes().length > 0) {
                        archiveEntityLinkerPanelLocal = new ArchiveEntityLinkerPanel(panelSetting, this, graphPanel, dragTransferHandler, ArchiveEntityLinkerPanel.TreeType.LocalTree, treeHelper, dataNodeLoader);
                        panelSetting.addTargetPanel(archiveEntityLinkerPanelLocal, false);
                    }
                    archiveEntityLinkerPanelMpiRemote = new ArchiveEntityLinkerPanel(panelSetting, this, graphPanel, dragTransferHandler, ArchiveEntityLinkerPanel.TreeType.MpiTree, treeHelper, dataNodeLoader);
                    panelSetting.addTargetPanel(archiveEntityLinkerPanelMpiRemote, false);
                    panelSetting.setMenuEnabled(graphPanel.dataStoreSvg.diagramMode == DiagramMode.KinTypeQuery);
                    break;
                case DiagramTree:
                    if (panelSetting == null) {
                        panelSetting = graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.DiagramTree, 150, showDiagramTree);
                    }
                    panelSetting.setHidePane(egoSelectionHidePane, "Diagram Tree");
                    panelSetting.addTargetPanel(egoSelectionPanel, false);
                    if (projectTree != null) {
                        panelSetting.addTargetPanel(projectTree, true);
                    }
                    panelSetting.setMenuEnabled(true);
                    break;
                case EntitySearch:
                    if (panelSetting == null) {
                        panelSetting = graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.EntitySearch, 150, showEntitySearch);
                    }
                    panelSetting.setHidePane(egoSelectionHidePane, "Search Entities");
                    panelSetting.addTargetPanel(entitySearchPanel, false);
                    panelSetting.setMenuEnabled(graphPanel.dataStoreSvg.diagramMode != DiagramMode.FreeForm);
                    break;
                case IndexerSettings:
                    if (panelSetting == null) {
                        panelSetting = graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.IndexerSettings, 150, showIndexerSettings);
                    }
                    panelSetting.setHidePane(kinTypeHidePane, "Diagram Settings");
                    graphPanel.getIndexParameters().symbolFieldsFields.setParent(graphPanel.getIndexParameters());
                    graphPanel.getIndexParameters().labelFields.setParent(graphPanel.getIndexParameters());
                    panelSetting.addTargetPanel(new KinTypeDefinitions("Kin Type Definitions", this, graphPanel.dataStoreSvg), false);
                    panelSetting.addTargetPanel(new RelationSettingsPanel("Relation Type Definitions", this, graphPanel.dataStoreSvg, dialogHandler), false);
                    if (graphPanel.dataStoreSvg.diagramMode != DiagramMode.FreeForm) {
                        // hide some of the settings panels from freeform diagrams
                        final JScrollPane symbolFieldsPanel = new JScrollPane(new FieldSelectionList(entityCollection, this, graphPanel.getIndexParameters().symbolFieldsFields, tableCellDragHandler));
                        final JScrollPane labelFieldsPanel = new JScrollPane(new FieldSelectionList(entityCollection, this, graphPanel.getIndexParameters().labelFields, tableCellDragHandler));
                        // todo: Ticket #1115 add overlay fields as paramters
                        symbolFieldsPanel.setName("Symbol Fields");
                        labelFieldsPanel.setName("Label Fields");
                        panelSetting.addTargetPanel(symbolFieldsPanel, false);
                        panelSetting.addTargetPanel(labelFieldsPanel, false);
                        panelSetting.addTargetPanel(cmdiProfileSelectionPanel, false);
                    }
                    panelSetting.setMenuEnabled(true);
                    break;
                case KinTerms:
                    if (panelSetting == null) {
                        panelSetting = graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.KinTerms, 150, showKinTerms);
                    }
                    panelSetting.setHidePane(kinTermHidePane, "Kin Terms");
                    for (KinTermGroup kinTerms : graphPanel.getkinTermGroups()) {
                        panelSetting.addTargetPanel(new KinTermPanel(this, kinTerms, dialogHandler), false); //  + kinTerms.titleString
                    }
                    panelSetting.setMenuEnabled(graphPanel.dataStoreSvg.diagramMode == DiagramMode.FreeForm);
                    break;
                case KinTypeStrings:
                    if (panelSetting == null) {
                        panelSetting = graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.KinTypeStrings, 150, showKinTypeStrings);
                    }
                    panelSetting.setHidePane(kinTypeHidePane, "Kin Type Strings");
                    panelSetting.addTargetPanel(new JScrollPane(kinTypeStringInput), false);
                    panelSetting.setMenuEnabled(true);
                    break;
                case ExportPanel:
                    if (panelSetting == null) {
                        panelSetting = graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.ExportPanel, 150, showExportPanel);
                    }
                    panelSetting.setHidePane(kinTypeHidePane, "Export Data");
                    panelSetting.addTargetPanel(new ExportPanel(), false);
                    panelSetting.setMenuEnabled(false);
                    break;
//                case MetaData:
//                    if (panelSetting == null) {
//                       panelSetting = graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.MetaData, 150, showMetaData);
//                    }
//                    panelSetting.setTargetPanel(tableHidePane, tableScrollPane, "Metadata");
//                    break;
                case PluginPanel:
                    if (panelSetting == null) {
                        panelSetting = graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.PluginPanel, 150, false);
                    }
                    panelSetting.setHidePane(kinTypeHidePane, "Active Plugins");
                    panelSetting.setMenuEnabled(panelSetting.getTargetPanels().length > 0);
                    break;
                default:
                    dialogHandler.addMessageDialogToQueue("Panel type '" + panelType.name() + "' unknown or unsupported.", "Load Diagram");
            }
        }
        tableHidePane.setVisible(false);
//        tableHidePane.toggleHiddenState(); // put the metadata table plane into the closed state
        kinGraphPanel.add(kinTypeHidePane, BorderLayout.PAGE_START);
        kinGraphPanel.add(egoSelectionHidePane, BorderLayout.LINE_START);
        kinGraphPanel.add(graphPanel, BorderLayout.CENTER);
        kinGraphPanel.add(kinTermHidePane, BorderLayout.LINE_END);
        kinGraphPanel.add(tableHidePane, BorderLayout.PAGE_END);

        this.add(kinGraphPanel, BorderLayout.CENTER);
        statusBar = new StatusBar("diagram data not yet loaded");
        this.add(statusBar, BorderLayout.SOUTH);

        EntityData[] svgDataNodes;
        if (graphPanel.dataStoreSvg.graphData != null) {
            svgDataNodes = graphPanel.dataStoreSvg.graphData.getDataNodes();
        } else {
            // if the data has not been loaded from the svg then we do not need to pre load it so we cah just use an empty array
            svgDataNodes = new EntityData[]{};
        }
        entityIndex = new QueryParser(/* graphPanel.getDiagramUniqueIdentifiers(), */entityCollection);
        kinTypeStringInput.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                synchronized (e) {
                    redrawIfKinTermsChanged();
                }
            }
        });
        kinTypeStringProviders = new ArrayList<KinTypeStringProvider>();
        kinTypeStringProviders.add(kinTypeStringInput);
        kinTypeStringProviders.add(entitySearchPanel);
        kinTypeStringProviders.addAll(Arrays.asList(graphPanel.getkinTermGroups()));
//        graphPanel.svgUpdateHandler.updateEntities();
    }

    public void setStatusBarText(String statusText) {
        statusBar.setStatusBarText(statusText);
    }

    static public File getGlobalDefaultDiagramFile(SessionStorage sessionStorage) {
        return new File(sessionStorage.getProjectDirectory(), "DefaultKinDiagram.svg");
    }

    static public File getDefaultDiagramFile(ProjectRecord projectRecord) {
        return new File(projectRecord.getProjectDirectory(), "DefaultKinDiagram.svg");
    }

    public void redrawIfKinTermsChanged() {
        if (kinTypeStringInput.hasChanges()) {
            graphPanel.setKinTypeStrigs(kinTypeStringInput.getCurrentStrings());
            drawGraph(false);
        }
    }
    boolean graphThreadRunning = false;
    boolean graphUpdateRequired = false;

    public boolean verifyDiagramDataLoaded() {
        if (graphPanel.dataStoreSvg.graphData == null) {
//            // if the first draw has not occured then we must do this now
            if (dialogHandler.showConfirmDialogBox("The diagram needs to be recalculated before it can be interacted with.\nRecalculate now?", "Recalculate Diagram")) {
                this.drawGraph(true);
            }
            return false;
        } else {
            return true;
        }
    }

    public synchronized void drawGraph(boolean resetZoom) {
        drawGraph(null, resetZoom);
    }

    public synchronized void drawGraph(final UniqueIdentifier[] uniqueIdentifiers, final boolean resetZoom) {
        graphUpdateRequired = true;
        entityIndex.requestAbortProcess();
        if (!graphThreadRunning) {
            graphThreadRunning = true;
            new Thread() {
                @Override
                public void run() {
                    while (graphUpdateRequired) {
                        try {
                            graphUpdateRequired = false;
                            entityIndex.clearAbortRequest();
                            try {
                                String[] kinTypeStrings = graphPanel.getKinTypeStrigs();
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        progressBar.setValue(0);
                                        progressBar.setVisible(true);
                                    }
                                });
                                if (graphPanel.dataStoreSvg.diagramMode == DiagramMode.Undefined) {
                                    graphPanel.dataStoreSvg.diagramMode = DiagramMode.FreeForm;
                                    if (!graphPanel.dataStoreSvg.egoEntities.isEmpty() || !graphPanel.dataStoreSvg.requiredEntities.isEmpty()) {
                                        graphPanel.dataStoreSvg.diagramMode = DiagramMode.KinTypeQuery;
                                    } else {
                                        for (String currentLine : kinTypeStrings) {
                                            if (currentLine.contains("[")) {
                                                graphPanel.dataStoreSvg.diagramMode = DiagramMode.KinTypeQuery;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (graphPanel.dataStoreSvg.diagramMode == DiagramMode.KinTypeQuery) {
//                                    diagramMode = DiagramMode.KinTypeQuery;
                                    final EntityData[] graphNodes = entityIndex.processKinTypeStrings(kinTypeStringProviders, graphPanel.getIndexParameters(), graphPanel.dataStoreSvg, progressBar);
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            progressBar.setIndeterminate(true);
                                        }
                                    });
                                    if (graphPanel.dataStoreSvg.graphData == null) {
                                        // this will only be null when the diagram has been opened but not recalculated yet
                                        graphPanel.dataStoreSvg.graphData = new GraphSorter();
                                    }
                                    graphPanel.dataStoreSvg.graphData.setEntitys(graphNodes);
                                    // register interest Arbil updates and update the graph when data is edited in the table
//                                registerCurrentNodes(graphSorter.getDataNodes());
                                    graphPanel.drawNodes(graphPanel.dataStoreSvg.graphData, resetZoom);
                                    egoSelectionPanel.setTreeNodes(graphPanel);
                                    new KinTermCalculator().insertKinTerms(graphPanel.dataStoreSvg.graphData.getDataNodes(), graphPanel.getkinTermGroups());
                                } else {
//                                    diagramMode = DiagramMode.FreeForm;
                                    KinTypeStringConverter graphData = new KinTypeStringConverter(graphPanel.dataStoreSvg);
                                    graphData.readKinTypes(kinTypeStringProviders, graphPanel.dataStoreSvg);
                                    graphPanel.drawNodes(graphData, resetZoom);
                                    egoSelectionPanel.setTreeNodes(graphPanel);
//                KinDiagramPanel.this.doLayout();
                                    new KinTermCalculator().insertKinTerms(graphData.getDataNodes(), graphPanel.getkinTermGroups());
                                }
//        kinTypeStrings = graphPanel.getKinTypeStrigs();
                            } catch (EntityServiceException exception) {
                                BugCatcherManager.getBugCatcher().logError(exception);
                                dialogHandler.addMessageDialogToQueue("Failed to load all entities required", "Draw Graph");
                            }
                        } catch (ProcessAbortException exception) {
                            // if the process has been aborted then it should be safe to let the next thread loop take over from here
                            System.out.println("draw graph process has been aborted, it should be safe to let the next thread loop take over from here");
                        } catch (ImportRequiredException exception) {
                            if (exception.getImportURI() != null) {
                                final String[] optionStrings = new String[]{"Import", "Cancel"};
                                int userOption = dialogHandler.showDialogBox(exception.getMessageString() + "\nDo you want to import this data now?\n" + exception.getImportURI().toASCIIString(), "Import Required", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, optionStrings, optionStrings[0]);
                                // ask the user if they want to import the required file and start the import on yes
                                if (userOption == 0) {
                                    try {
                                        if ("jar".equals(exception.getImportURI().getScheme())) {
                                            diagramWindowManager.openJarImportPanel(exception.getImportURI().getPath(), KinDiagramPanel.this, entityCollection);
                                        } else {
                                            diagramWindowManager.openImportPanel(exception.getImportURI().toASCIIString(), KinDiagramPanel.this, entityCollection);
                                        }
                                    } catch (ImportException exception1) {
                                        dialogHandler.addMessageDialogToQueue(exception1.getMessage() + "\n" + exception.getImportURI().toASCIIString(), "Import Required Data");
                                    }
                                }
                            } else {
                                dialogHandler.addMessageDialogToQueue(exception.getMessageString(), "Draw Graph");
                            }
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            progressBar.setVisible(false);
                        }
                    });
                    graphThreadRunning = false;
                    if (uniqueIdentifiers != null) {
                        graphPanel.setSelectedIds(uniqueIdentifiers);
                    }
                }
            }.start();
        }
    }

//    @Deprecated
//    public void setDisplayNodes(String typeString, String[] egoIdentifierArray) {
//        // todo: should this be replaced by the required nodes?
//        if (kinTypeStringInput.getText().equals(defaultString)) {
//            kinTypeStringInput.setText("");
//        }
//        String kinTermContents = kinTypeStringInput.getText();
//        for (String currentId : egoIdentifierArray) {
//            kinTermContents = kinTermContents + typeString + "=[" + currentId + "]\n";
//        }
//        kinTypeStringInput.setText(kinTermContents);
//        graphPanel.setKinTypeStrigs(kinTypeStringInput.getText().split("\n"));
////        kinTypeStrings = graphPanel.getKinTypeStrigs();
//        drawGraph();
//    }
    public void setEgoNodes(UniqueIdentifier[] egoIdentifierArray) {
        // todo: this does not update the ego highlight on the graph and the trees.
        graphPanel.dataStoreSvg.egoEntities = new HashSet<UniqueIdentifier>(Arrays.asList(egoIdentifierArray));
        drawGraph(false);
    }

    public void addEgoNodes(UniqueIdentifier[] egoIdentifierArray) {
        // todo: this does not update the ego highlight on the graph and the trees.
        graphPanel.dataStoreSvg.egoEntities.addAll(Arrays.asList(egoIdentifierArray));
        drawGraph(false);
    }

    public void removeEgoNodes(UniqueIdentifier[] egoIdentifierArray) {
        // todo: this does not update the ego highlight on the graph and the trees.
        graphPanel.dataStoreSvg.egoEntities.removeAll(Arrays.asList(egoIdentifierArray));
        drawGraph(false);
    }

    public void addNodeCollection(UniqueIdentifier[] entityIdentifiers, String nodeSetTitle) {
        // todo:. consider if this should be added to the panels menu also as us done for addKinTermGroup
        EntitySearchPanel entitySearchPanel = new EntitySearchPanel(entityCollection, this, graphPanel, dialogHandler, dataNodeLoader, nodeSetTitle, entityIdentifiers);
        entitySearchPanel.setTransferHandler(dragTransferHandler);
        kinTermHidePane.addTab(nodeSetTitle, entitySearchPanel);
        kinTermHidePane.setHiddeState();
        kinTypeStringProviders.add(entitySearchPanel);
    }

    public void addKinTermGroup() {
        final KinTermGroup kinTermGroup = graphPanel.addKinTermGroup();
        for (VisiblePanelSetting panelSetting : graphPanel.dataStoreSvg.getVisiblePanels()) {
            if (panelSetting.getPanelType() == PanelType.KinTerms) {
                panelSetting.addTargetPanel(new KinTermPanel(this, kinTermGroup, dialogHandler), true);
            }
        }
        kinTypeStringProviders.add(kinTermGroup);
    }

    public void addPluginPanel(KinOathPanelPlugin kinOathPanelPlugin, boolean isVisible) throws PluginException {
        VisiblePanelSetting panelSetting = graphPanel.dataStoreSvg.getPanelSettingByType(PanelType.PluginPanel);
        if (panelSetting != null) {
            final JScrollPane uiPanel = kinOathPanelPlugin.getUiPanel(dialogHandler, sessionStorage, BugCatcherManager.getBugCatcher());
            uiPanel.setName("Plugin: " + ((BaseModule) kinOathPanelPlugin).getName());
            if (isVisible) {
                panelSetting.setPanelShown(true);
                panelSetting.addTargetPanel(uiPanel, true);
            } else {
                panelSetting.removeTargetPanel(uiPanel);
            }
            panelSetting.setMenuEnabled(panelSetting.getTargetPanels().length > 0);
        }
    }

    public void addRequiredNodes(UniqueIdentifier[] egoIdentifierArray, Point screenLocation) {
        if (screenLocation != null) {
            Point defaultLocation = graphPanel.svgUpdateHandler.getEntityPointOnDocument(screenLocation);
            graphPanel.dataStoreSvg.graphData.setPreferredEntityLocation(egoIdentifierArray, defaultLocation);
        }
        graphPanel.dataStoreSvg.requiredEntities.addAll(Arrays.asList(egoIdentifierArray));
        drawGraph(false);
    }

    public void removeRequiredNodes(UniqueIdentifier[] egoIdentifierArray) {
        graphPanel.dataStoreSvg.requiredEntities.removeAll(Arrays.asList(egoIdentifierArray));
        drawGraph(false);
    }

    public void loadAllTrees() {
        egoSelectionPanel.setTreeNodes(graphPanel); // init the trees in the side panel
        if (archiveEntityLinkerPanelRemote != null) {
            archiveEntityLinkerPanelRemote.loadTreeNodes();
        }
        if (archiveEntityLinkerPanelLocal != null) {
            archiveEntityLinkerPanelLocal.loadTreeNodes();
        }
        if (archiveEntityLinkerPanelMpiRemote != null) {
            archiveEntityLinkerPanelMpiRemote.loadTreeNodes();
        }
        if (projectTree != null) {
            projectTree.loadProjectTree();
            entityCollection.addDatabaseUpdateListener(projectTree);
        }
    }

    public void showProgressBar() {
        if (SwingUtilities.isEventDispatchThread()) {

            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            KinDiagramPanel.this.revalidate();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressBar.setIndeterminate(true);
                    progressBar.setVisible(true);
                    KinDiagramPanel.this.revalidate();
                }
            });
        }
    }

    public void clearProgressBar() {
        if (SwingUtilities.isEventDispatchThread()) {
            progressBar.setIndeterminate(false);
            progressBar.setVisible(false);
            KinDiagramPanel.this.revalidate();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisible(false);
                    KinDiagramPanel.this.revalidate();
                }
            });
        }
    }

    public boolean hasSaveFileName() {
        return (graphPanel.hasSaveFileName());
    }

    public File getFileName() {
        return graphPanel.getFileName();
    }

    public boolean requiresSave() {
        return graphPanel.requiresSave();
    }

    public void setRequiresSave() {
        graphPanel.setRequiresSave();
    }

    public void saveToFile() {
        graphPanel.saveToFile();
    }

    public void saveToFile(File saveFile) {
        graphPanel.saveToFile(saveFile);
    }

    public void updateGraph() {
        this.drawGraph(true);
    }

    public void doActionCommand(MouseListenerSvg.ActionCode actionCode) {
        graphPanel.mouseListenerSvg.performMenuAction(actionCode);
    }

    public GraphPanel getGraphPanel() {
        return graphPanel;
    }

    public void exportKinTerms() {
        Component tabComponent = kinTermHidePane.getSelectedComponent();
        if (tabComponent instanceof KinTermPanel) {
            ((KinTermPanel) tabComponent).exportKinTerms();
        }
    }

//    public void hideShow() {
//        kinTermHidePane.toggleHiddenState();
//    }
    public void importKinTerms() {
        Component tabComponent = kinTermHidePane.getSelectedComponent();
        if (tabComponent instanceof KinTermPanel) {
            ((KinTermPanel) tabComponent).importKinTerms();
        }
    }

    public EntityCollection getEntityCollection() {
        return entityCollection;
    }

    public int getKinTermGroupCount() {
        return graphPanel.getkinTermGroups().length;
    }

    public VisiblePanelSetting[] getVisiblePanels() {
        return graphPanel.dataStoreSvg.getVisiblePanels();
    }

    public void setPanelState(PanelType panelType, boolean panelVisible) {
        for (VisiblePanelSetting panelSetting : graphPanel.dataStoreSvg.getVisiblePanels()) {
            if (panelSetting.getPanelType() == panelType) {
                panelSetting.setPanelShown(panelVisible);
            }
        }
    }

    public boolean getPanelState(PanelType panelType) {
        for (VisiblePanelSetting panelSetting : graphPanel.dataStoreSvg.getVisiblePanels()) {
            if (panelSetting.getPanelType() == panelType) {
                return panelSetting.isPanelShown();
            }
        }
        return false;
    }

    public void setSelectedKinTypeSting(String kinTypeStrings) {
        for (Component tabComponent : kinTermHidePane.getComponents()) {
            if (tabComponent instanceof KinTermPanel) {
                KinTermPanel kinTermPanel = (KinTermPanel) tabComponent;
                kinTermPanel.setDefaultKinType(kinTypeStrings);
            }
        }
    }

//    public boolean isHidden() {
//        return kinTermHidePane.isHidden();
//    }
    public EntityData[] getGraphEntities() {
        if (graphPanel.dataStoreSvg.graphData == null) {
            // when the diagram has not yet been loaded the graphData will be null
            return new EntityData[0];
        }
        return graphPanel.dataStoreSvg.graphData.getDataNodes();
    }

    public void registerArbilNode(UniqueIdentifier uniqueIdentifier, ArbilDataNode arbilDataNode) {
        // todo: i think this is resolved but double check the issue where arbil nodes update frequency is too high and breaks basex
        // todo: load the nodes in the KinDataNode when putting them in the table and pass on the reload requests here when they occur
        // todo: replace the data node registering process.
//        for (EntityData entityData : currentEntities) {
//            ArbilDataNode arbilDataNode = null;
        if (!registeredArbilDataNode.containsKey(arbilDataNode)) {
            arbilDataNode.registerContainer(this);
            registeredArbilDataNode.put(arbilDataNode, uniqueIdentifier);
        }
//                try {
//                    String metadataPath = entityData.getEntityPath();
//                    if (metadataPath != null) {
//                        // todo: this should not load the arbil node only register an interest
////                        and this needs to be tested
//                        arbilDataNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNodeWithoutLoading(new URI(metadataPath));
//                        registeredArbilDataNode.put(entityData.getUniqueIdentifier(), arbilDataNode);
//                        arbilDataNode.registerContainer(this);
//                        // todo: keep track of registered nodes and remove the unrequired ones here
//                    } else {
//                        GuiHelper.linorgBugCatcher.logError(new Exception("Error getting path for: " + entityData.getUniqueIdentifier().getAttributeIdentifier() + " : " + entityData.getLabel()[0]));
//                    }
//                } catch (URISyntaxException exception) {
//                    GuiHelper.linorgBugCatcher.logError(exception);
//                }
//            } else {
//                arbilDataNode = registeredArbilDataNode.get(entityData.getUniqueIdentifier());
//            }
//            if (arbilDataNode != null) {
//                entityData.metadataRequiresSave = arbilDataNode.getNeedsSaveToDisk(false);
//            }
//        }
    }

    public void entityRelationsChanged(UniqueIdentifier[] selectedIdentifiers) {
        // this method does not need to update the database because the link changing process has already done that
        // remove the stored graph locations of the selected ids
//        graphPanel.clearEntityLocations(selectedIdentifiers);
        graphPanel.getIndexParameters().valuesChanged = true;
        drawGraph(false);
    }

    public void dataNodeIconCleared(ArbilNode arbilNode) {
        if (arbilDataNodesChangedStatus.containsKey(arbilNode)) {
            boolean dataBaseRequiresUpdate = false;
            boolean redrawRequired = false;
            if (arbilNode instanceof ArbilDataNode) {
                ArbilDataNode arbilDataNode = (ArbilDataNode) arbilNode;
                boolean currentlyNeedsSave = arbilDataNode.getNeedsSaveToDisk(false);
                if (currentlyNeedsSave != arbilDataNodesChangedStatus.get(arbilNode)) {
                    arbilDataNodesChangedStatus.put(arbilNode, currentlyNeedsSave);
                    UniqueIdentifier uniqueIdentifier = registeredArbilDataNode.get(arbilDataNode);
//                     find the entity data for this arbil data node
                    for (EntityData entityData : graphPanel.dataStoreSvg.graphData.getDataNodes()) {
                        if (entityData.getUniqueIdentifier().equals(uniqueIdentifier)) {
                            // clear or set the needs save flag
                            entityData.metadataRequiresSave = currentlyNeedsSave;
                        }
                    }
                    dataBaseRequiresUpdate = !currentlyNeedsSave; // if there was a change that has been saved then an db update is required
                    redrawRequired = true;
                }
                if (dataBaseRequiresUpdate) {
                    try {
                        entityCollection.updateDatabase(arbilDataNode.getURI(), registeredArbilDataNode.get(arbilDataNode));
                        graphPanel.getIndexParameters().valuesChanged = true;
                    } catch (EntityServiceException exception) {
                        dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Update Database");
                    }
                }
            }
            if (redrawRequired) {
                drawGraph(false);
            }
        } else {
            // this will occur in the initial loading process, hence this does not perform db updates nor graph redraws
            arbilDataNodesChangedStatus.put(arbilNode, false);
        }
    }

    public void dataNodeChildAdded(ArbilNode destination, ArbilNode newChildNode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void dataNodeRemoved(ArbilNode adn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isFullyLoadedNodeRequired() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
