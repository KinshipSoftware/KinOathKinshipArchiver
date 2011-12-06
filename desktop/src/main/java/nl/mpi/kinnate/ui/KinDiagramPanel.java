package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.kindata.VisiblePanelSetting;
import nl.mpi.kinnate.kindata.VisiblePanelSetting.PanelType;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityService;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.entityindexer.ProcessAbortException;
import nl.mpi.kinnate.entityindexer.QueryParser;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kintypestrings.KinTermCalculator;
import nl.mpi.kinnate.kintypestrings.KinTermGroup;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.kintypestrings.ParserHighlight;
import nl.mpi.kinnate.svg.MouseListenerSvg;
import nl.mpi.kinnate.ui.menu.DocumentNewMenu.DocumentType;
import nl.mpi.kinnate.ui.kintypeeditor.KinTypeDefinitions;

/**
 *  Document   : KinTypeStringTestPanel
 *  Created on : Sep 29, 2010, 12:52:01 PM
 *  Author     : Peter Withers
 */
public class KinDiagramPanel extends JPanel implements SavePanel, KinTermSavePanel, ArbilDataNodeContainer {

    private EntityCollection entityCollection;
    private KinTypeStringInput kinTypeStringInput;
    private GraphPanel graphPanel;
    private EgoSelectionPanel egoSelectionPanel;
    private ArchiveEntityLinkerPanel archiveEntityLinkerPanelRemote;
    private ArchiveEntityLinkerPanel archiveEntityLinkerPanelLocal;
    private ArchiveEntityLinkerPanel archiveEntityLinkerPanelMpiRemote;
    private HidePane kinTermHidePane;
    private HidePane kinTypeHidePane;
    private EntityService entityIndex;
    private JProgressBar progressBar;
    static private File defaultDiagramTemplate;
    private HashMap<ArbilDataNode, UniqueIdentifier> registeredArbilDataNode;
    private HashMap<ArbilNode, Boolean> arbilDataNodesChangedStatus;
//    private ArrayList<ArbilTree> treeLoadQueue = new ArrayList<ArbilTree>();

    public KinDiagramPanel(URI existingFile, boolean savableType) {
        initKinDiagramPanel(existingFile, null, savableType);
    }

    public KinDiagramPanel(DocumentType documentType) {
        initKinDiagramPanel(null, documentType, false);
    }

    private void initKinDiagramPanel(URI existingFile, DocumentType documentType, boolean savableType) {
        entityCollection = new EntityCollection();
        progressBar = new JProgressBar();
        graphPanel = new GraphPanel(this);
        kinTypeStringInput = new KinTypeStringInput(graphPanel.dataStoreSvg);

        boolean showKinTerms = false;
        boolean showArchiveLinker = false;
        boolean showDiagramTree = false;
        boolean showEntitySearch = false;
        boolean showIndexerSettings = false;
        boolean showKinTypeStrings = false;
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
            if (documentType == null) {
                // this is the default document that users see when they run the application for the first time
                documentType = DocumentType.Simple;
            }
            switch (documentType) {
                case ArchiveLinker:
                    showMetaData = true;
                    showDiagramTree = true;
                    showArchiveLinker = true;
                    break;
                case CustomQuery:
                    showMetaData = true;
                    showKinTypeStrings = true;
                    showDiagramTree = true;
                    showIndexerSettings = true;
                    break;
                case EntitySearch:
                    showMetaData = true;
                    showEntitySearch = true;
                    showDiagramTree = true;
                    break;
                case KinTerms:
                    showKinTerms = true;
                    graphPanel.addKinTermGroup();
                    break;
                case Freeform:
//                    showDiagramTree = true;
                    showKinTypeStrings = true;
                    break;
                case Simple:
                    showMetaData = true;
                    showDiagramTree = true;
                    break;
                case Query:
                    showMetaData = true;
                    showDiagramTree = true;
                    showKinTypeStrings = true;
                default:
                    break;
            }
            graphPanel.generateDefaultSvg();
        }
        this.setLayout(new BorderLayout());

        progressBar.setVisible(false);
        graphPanel.add(progressBar, BorderLayout.PAGE_START);


        registeredArbilDataNode = new HashMap<ArbilDataNode, UniqueIdentifier>();
        arbilDataNodesChangedStatus = new HashMap<ArbilNode, Boolean>();
        egoSelectionPanel = new EgoSelectionPanel(this, graphPanel);
//        kinTermPanel = new KinTermTabPane(this, graphPanel.getkinTermGroups());

//        kinTypeStringInput.setText(defaultString);

        JPanel kinGraphPanel = new JPanel(new BorderLayout());

        kinTypeHidePane = new HidePane(HidePane.HidePanePosition.top, 0);

        HidePane tableHidePane = new HidePane(HidePane.HidePanePosition.bottom, 150);

        KinDragTransferHandler dragTransferHandler = new KinDragTransferHandler(this);
        graphPanel.setTransferHandler(dragTransferHandler);
        egoSelectionPanel.setTransferHandler(dragTransferHandler);

        EntitySearchPanel entitySearchPanel = new EntitySearchPanel(entityCollection, this, graphPanel);
        entitySearchPanel.setTransferHandler(dragTransferHandler);

        HidePane egoSelectionHidePane = new HidePane(HidePane.HidePanePosition.left, 0);

        kinTermHidePane = new HidePane(HidePane.HidePanePosition.right, 0);

        TableCellDragHandler tableCellDragHandler = new TableCellDragHandler();
        graphPanel.setArbilTableModel(new MetadataPanel(graphPanel, tableHidePane, tableCellDragHandler));

        if (graphPanel.dataStoreSvg.getVisiblePanels() == null) {
            // in some older files and non kinoath files these values would not be set, so we make sure that they are here
            graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.KinTerms, 150, showKinTerms);
            graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.ArchiveLinker, 150, showArchiveLinker);
            graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.DiagramTree, 150, showDiagramTree);
            graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.EntitySearch, 150, showEntitySearch);
            graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.IndexerSettings, 150, showIndexerSettings);
            graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.KinTypeStrings, 150, showKinTypeStrings);
//            graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.MetaData, 150, showMetaData);
        }
        for (VisiblePanelSetting panelSetting : graphPanel.dataStoreSvg.getVisiblePanels()) {
            if (panelSetting.getPanelType() != null) {
                switch (panelSetting.getPanelType()) {
                    case ArchiveLinker:
                        panelSetting.setHidePane(kinTermHidePane, "Archive Linker");
                        archiveEntityLinkerPanelRemote = new ArchiveEntityLinkerPanel(panelSetting, this, graphPanel, dragTransferHandler, ArchiveEntityLinkerPanel.TreeType.RemoteTree);
                        archiveEntityLinkerPanelLocal = new ArchiveEntityLinkerPanel(panelSetting, this, graphPanel, dragTransferHandler, ArchiveEntityLinkerPanel.TreeType.LocalTree);
                        archiveEntityLinkerPanelMpiRemote = new ArchiveEntityLinkerPanel(panelSetting, this, graphPanel, dragTransferHandler, ArchiveEntityLinkerPanel.TreeType.MpiTree);
                        panelSetting.addTargetPanel(archiveEntityLinkerPanelRemote, false);
                        panelSetting.addTargetPanel(archiveEntityLinkerPanelLocal, false);
                        panelSetting.addTargetPanel(archiveEntityLinkerPanelMpiRemote, false);
                        break;
                    case DiagramTree:
                        panelSetting.setHidePane(egoSelectionHidePane, "Diagram Tree");
                        panelSetting.addTargetPanel(egoSelectionPanel, false);
                        break;
                    case EntitySearch:
                        panelSetting.setHidePane(kinTermHidePane, "Search Entities");
                        panelSetting.addTargetPanel(entitySearchPanel, false);
                        break;
                    case IndexerSettings:
                        panelSetting.setHidePane(kinTypeHidePane, "Diagram Settings");
                        graphPanel.getIndexParameters().symbolFieldsFields.setParent(graphPanel.getIndexParameters());
                        graphPanel.getIndexParameters().labelFields.setParent(graphPanel.getIndexParameters());
                        final JScrollPane symbolFieldsPanel = new JScrollPane(new FieldSelectionList(this, graphPanel.getIndexParameters().symbolFieldsFields, tableCellDragHandler));
                        final JScrollPane labelFieldsPanel = new JScrollPane(new FieldSelectionList(this, graphPanel.getIndexParameters().labelFields, tableCellDragHandler));
                        // todo: Ticket #1115 add overlay fields as paramters
                        symbolFieldsPanel.setName("Symbol Fields");
                        labelFieldsPanel.setName("Label Fields");
                        panelSetting.addTargetPanel(symbolFieldsPanel, false);
                        panelSetting.addTargetPanel(labelFieldsPanel, false);
                        panelSetting.addTargetPanel(new KinTypeDefinitions("Kin Type Definitions", this, graphPanel.dataStoreSvg), false);
                        break;
                    case KinTerms:
                        panelSetting.setHidePane(kinTermHidePane, "Kin Terms");
                        for (KinTermGroup kinTerms : graphPanel.getkinTermGroups()) {
                            panelSetting.addTargetPanel(new KinTermPanel(this, kinTerms), false); //  + kinTerms.titleString
                        }
                        break;
                    case KinTypeStrings:
                        panelSetting.setHidePane(kinTypeHidePane, "Kin Type Strings");
                        panelSetting.addTargetPanel(new JScrollPane(kinTypeStringInput), false);
                        break;
//                case MetaData:
//                    panelSetting.setTargetPanel(tableHidePane, tableScrollPane, "Metadata");
//                    break;
                }
            }
        }
        tableHidePane.setVisible(false);
//        tableHidePane.toggleHiddenState(); // put the metadata table plane into the closed state
        kinGraphPanel.add(kinTypeHidePane, BorderLayout.PAGE_START);
        kinGraphPanel.add(egoSelectionHidePane, BorderLayout.LINE_START);
        kinGraphPanel.add(graphPanel, BorderLayout.CENTER);
        kinGraphPanel.add(kinTermHidePane, BorderLayout.LINE_END);
        kinGraphPanel.add(tableHidePane, BorderLayout.PAGE_END);

        this.add(kinGraphPanel);

        entityIndex = new QueryParser(graphPanel.dataStoreSvg.graphData.getDataNodes());
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
    }

    static public File getDefaultDiagramFile() {
        if (defaultDiagramTemplate == null) {
            defaultDiagramTemplate = new File(ArbilSessionStorage.getSingleInstance().getStorageDirectory(), "DefaultKinDiagram.svg");
        }
        return defaultDiagramTemplate;
    }

    public void redrawIfKinTermsChanged() {
        if (kinTypeStringInput.hasChanges()) {
            graphPanel.setKinTypeStrigs(kinTypeStringInput.getCurrentStrings());
            drawGraph();
        }
    }
    boolean graphThreadRunning = false;
    boolean graphUpdateRequired = false;

    public synchronized void drawGraph() {
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
                                ParserHighlight[] parserHighlight = new ParserHighlight[kinTypeStrings.length];
                                progressBar.setValue(0);
                                progressBar.setVisible(true);
                                boolean isQuery = false;
                                if (!graphPanel.dataStoreSvg.egoEntities.isEmpty() || !graphPanel.dataStoreSvg.requiredEntities.isEmpty()) {
                                    isQuery = true;
                                } else {
                                    for (String currentLine : kinTypeStrings) {
                                        if (currentLine.contains("[")) {
                                            isQuery = true;
                                            break;
                                        }
                                    }
                                }
                                if (isQuery) {
                                    EntityData[] graphNodes = entityIndex.processKinTypeStrings(null, kinTypeStrings, parserHighlight, graphPanel.getIndexParameters(), graphPanel.dataStoreSvg, progressBar);
                                    progressBar.setIndeterminate(true);
                                    graphPanel.dataStoreSvg.graphData.setEntitys(graphNodes);
                                    // register interest Arbil updates and update the graph when data is edited in the table
//                                registerCurrentNodes(graphSorter.getDataNodes());
                                    graphPanel.drawNodes(graphPanel.dataStoreSvg.graphData);
                                    egoSelectionPanel.setTreeNodes(graphPanel);
                                    new KinTermCalculator().insertKinTerms(graphPanel.dataStoreSvg.graphData.getDataNodes(), graphPanel.getkinTermGroups());
                                } else {
                                    KinTypeStringConverter graphData = new KinTypeStringConverter(graphPanel.dataStoreSvg);
                                    graphData.readKinTypes(kinTypeStrings, graphPanel.getkinTermGroups(), graphPanel.dataStoreSvg, parserHighlight);
                                    graphPanel.drawNodes(graphData);
                                    egoSelectionPanel.setTransientNodes(graphData.getDataNodes());
//                KinDiagramPanel.this.doLayout();
                                    new KinTermCalculator().insertKinTerms(graphData.getDataNodes(), graphPanel.getkinTermGroups());
                                }
                                kinTypeStringInput.highlightKinTypeStrings(parserHighlight, kinTypeStrings);
//        kinTypeStrings = graphPanel.getKinTypeStrigs();
                            } catch (EntityServiceException exception) {
                                GuiHelper.linorgBugCatcher.logError(exception);
                                ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Failed to load all entities required", "Draw Graph");
                            }
                        } catch (ProcessAbortException exception) {
                            // if the process has been aborted then it should be safe to let the next thread loop take over from here
                        }
                    }
                    progressBar.setVisible(false);
                    graphThreadRunning = false;
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
        drawGraph();
    }

    public void addEgoNodes(UniqueIdentifier[] egoIdentifierArray) {
        // todo: this does not update the ego highlight on the graph and the trees.
        graphPanel.dataStoreSvg.egoEntities.addAll(Arrays.asList(egoIdentifierArray));
        drawGraph();
    }

    public void removeEgoNodes(UniqueIdentifier[] egoIdentifierArray) {
        // todo: this does not update the ego highlight on the graph and the trees.
        graphPanel.dataStoreSvg.egoEntities.removeAll(Arrays.asList(egoIdentifierArray));
        drawGraph();
    }

    public void addRequiredNodes(UniqueIdentifier[] egoIdentifierArray) {
        graphPanel.dataStoreSvg.requiredEntities.addAll(Arrays.asList(egoIdentifierArray));
        drawGraph();
    }

    public void removeRequiredNodes(UniqueIdentifier[] egoIdentifierArray) {
        graphPanel.dataStoreSvg.requiredEntities.removeAll(Arrays.asList(egoIdentifierArray));
        drawGraph();
    }

    public void loadAllTrees() {
        egoSelectionPanel.setTreeNodes(graphPanel); // init the trees in the side panel
        archiveEntityLinkerPanelRemote.loadTreeNodes();
        archiveEntityLinkerPanelLocal.loadTreeNodes();
        archiveEntityLinkerPanelMpiRemote.loadTreeNodes();
//        while (!treeLoadQueue.isEmpty()) {
//            treeLoadQueue.remove(0).requestResort();
//        }
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
        this.drawGraph();
    }

    public void doActionCommand(MouseListenerSvg.ActionCode actionCode) {
        graphPanel.mouseListenerSvg.performMenuAction(actionCode);
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

    public void addKinTermGroup() {
        final KinTermGroup kinTermGroup = graphPanel.addKinTermGroup();
        for (VisiblePanelSetting panelSetting : graphPanel.dataStoreSvg.getVisiblePanels()) {
            if (panelSetting.getPanelType() == PanelType.KinTerms) {
                panelSetting.addTargetPanel(new KinTermPanel(this, kinTermGroup), true);
            }
        }
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
        graphPanel.clearEntityLocations(selectedIdentifiers);
        graphPanel.getIndexParameters().valuesChanged = true;
        drawGraph();
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
                    entityCollection.updateDatabase(arbilDataNode.getURI());
                    graphPanel.getIndexParameters().valuesChanged = true;
                }
            }
            if (redrawRequired) {
                drawGraph();
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
}
