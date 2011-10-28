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
import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kindata.VisiblePanelSetting;
import nl.mpi.kinnate.kindata.VisiblePanelSetting.PanelType;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityService;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.entityindexer.QueryParser;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kintypestrings.KinTermGroup;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.kintypestrings.ParserHighlight;
import nl.mpi.kinnate.ui.DocumentNewMenu.DocumentType;
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
    private GraphSorter graphSorter;
    private EgoSelectionPanel egoSelectionPanel;
    private HidePane kinTermHidePane;
    private HidePane kinTypeHidePane;
//    private KinTermTabPane kinTermPanel;
    private EntityService entityIndex;
    private JProgressBar progressBar;
    static private File defaultDiagramTemplate;
    private HashMap<ArbilDataNode, UniqueIdentifier> registeredArbilDataNode;
    private HashSet<ArbilNode> arbilDataNodesFirstLoadDone;
    private String defaultString = "# The kin type strings entered here will determine how the entities show on the graph below\n";
    public static String defaultGraphString = "# The kin type strings entered here will determine how the entities show on the graph below\n"
            + "# Enter one string per line.\n"
            //+ "# By default all relations of the selected entity will be shown.\n"
            + "# for example:\n"
            //            + "EmWMMM\n"
            //            + "E:1:FFE\n"
            //            + "EmWMMM:1:\n"
            //            + "E:1:FFE\n"
            + "Em:Charles II of Spain:W:Marie Louise d'Orl�ans\n"
            + "Em:Charles II of Spain:F:Philip IV of Spain:F:Philip III of Spain:F:Philip II of Spain:F:Charles V, Holy Roman Emperor:F:Philip I of Castile\n"
            + "Em:Charles II of Spain:M:Mariana of Austria:M:Maria Anna of Spain:M:Margaret of Austria:M:Maria Anna of Bavaria\n"
            + "M:Mariana of Austria:F:Ferdinand III, Holy Roman Emperor:\n"
            + "F:Philip IV of Spain:M:Margaret of Austria\n"
            + "F:Ferdinand III, Holy Roman Emperor:\n"
            + "M:Maria Anna of Spain:\n"
            + "F:Philip III of Spain\n"
            + "M:Margaret of Austria\n"
            + "\n";
//            + "FS:1:BSSWMDHFF:1:\n"
//            + "M:2:SSDHMFM:2:\n"
//            + "F:3:SSDHMF:3:\n"
//            + "";
//            + "E=[Bob]MFM\n"
//            + "E=[Bob]MZ\n"
//            + "E=[Bob]F\n"
//            + "E=[Bob]M\n"
//            + "E=[Bob]S";
//    private String kinTypeStrings[] = new String[]{};

    public KinDiagramPanel(URI existingFile, boolean savableType) {
        initKinDiagramPanel(existingFile, null, savableType);
    }

    public KinDiagramPanel(DocumentType documentType) {
        initKinDiagramPanel(null, documentType, false);
    }

    private void initKinDiagramPanel(URI existingFile, DocumentType documentType, boolean savableType) {
        entityCollection = new EntityCollection();
        progressBar = new JProgressBar();
        EntityData[] svgStoredEntities = null;
        graphPanel = new GraphPanel(this);
        kinTypeStringInput = new KinTypeStringInput(defaultString);

        boolean showKinTerms = false;
        boolean showArchiveLinker = false;
        boolean showDiagramTree = false;
        boolean showEntitySearch = false;
        boolean showIndexerSettings = false;
        boolean showKinTypeStrings = false;
        boolean showMetaData = false;

        if (existingFile != null) {
            svgStoredEntities = graphPanel.readSvg(existingFile, savableType);
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
        arbilDataNodesFirstLoadDone = new HashSet<ArbilNode>();
        egoSelectionPanel = new EgoSelectionPanel(graphPanel);
//        kinTermPanel = new KinTermTabPane(this, graphPanel.getkinTermGroups());

//        kinTypeStringInput.setText(defaultString);

        JPanel kinGraphPanel = new JPanel(new BorderLayout());

        kinTypeHidePane = new HidePane(HidePane.HidePanePosition.top, 0);

        HidePane tableHidePane = new HidePane(HidePane.HidePanePosition.bottom, 150);

        KinDragTransferHandler dragTransferHandler = new KinDragTransferHandler(this);
        graphPanel.setTransferHandler(dragTransferHandler);
        egoSelectionPanel.setTransferHandler(dragTransferHandler);

        EntitySearchPanel entitySearchPanel = new EntitySearchPanel(entityCollection, graphPanel);
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
                        panelSetting.addTargetPanel(new ArchiveEntityLinkerPanel(graphPanel, dragTransferHandler));
                        break;
                    case DiagramTree:
                        panelSetting.setHidePane(egoSelectionHidePane, "Diagram Tree");
                        panelSetting.addTargetPanel(egoSelectionPanel);
                        break;
                    case EntitySearch:
                        panelSetting.setHidePane(kinTermHidePane, "Search Entities");
                        panelSetting.addTargetPanel(entitySearchPanel);
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
                        panelSetting.addTargetPanel(symbolFieldsPanel);
                        panelSetting.addTargetPanel(labelFieldsPanel);
                        panelSetting.addTargetPanel(new KinTypeDefinitions("Kin Type Definitions", this, graphPanel.dataStoreSvg));
                        break;
                    case KinTerms:
                        panelSetting.setHidePane(kinTermHidePane, "Kin Terms");
                        for (KinTermGroup kinTerms : graphPanel.getkinTermGroups()) {
                            panelSetting.addTargetPanel(new KinTermPanel(this, kinTerms)); //  + kinTerms.titleString
                        }
                        break;
                    case KinTypeStrings:
                        panelSetting.setHidePane(kinTypeHidePane, "Kin Type Strings");
                        panelSetting.addTargetPanel(new JScrollPane(kinTypeStringInput));
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

        entityIndex = new QueryParser(svgStoredEntities);
        graphSorter = new GraphSorter();
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
        if (!graphThreadRunning) {
            graphThreadRunning = true;
            new Thread() {

                @Override
                public void run() {
                    // todo: there are probably other synchronisation issues to resolve here.
                    while (graphUpdateRequired) {
                        graphUpdateRequired = false;
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
                                    if (currentLine.contains("=")) {
                                        isQuery = true;
                                        break;
                                    }
                                }
                            }
                            if (isQuery) {
                                EntityData[] graphNodes = entityIndex.processKinTypeStrings(null, kinTypeStrings, parserHighlight, graphPanel.getIndexParameters(), graphPanel.dataStoreSvg, progressBar);
                                graphSorter.setEntitys(graphNodes);
                                // register interest Arbil updates and update the graph when data is edited in the table
//                                registerCurrentNodes(graphSorter.getDataNodes());
                                graphPanel.drawNodes(graphSorter);
                                egoSelectionPanel.setTreeNodes(graphPanel.dataStoreSvg.egoEntities, graphPanel.dataStoreSvg.requiredEntities, graphSorter.getDataNodes(), graphPanel.getIndexParameters());
                            } else {
                                KinTypeStringConverter graphData = new KinTypeStringConverter(graphPanel.dataStoreSvg);
                                graphData.readKinTypes(kinTypeStrings, graphPanel.getkinTermGroups(), graphPanel.dataStoreSvg, parserHighlight);
                                graphPanel.drawNodes(graphData);
                                egoSelectionPanel.setTransientNodes(graphData.getDataNodes());
//                KinDiagramPanel.this.doLayout();
                            }
                            kinTypeStringInput.highlightKinTypeStrings(parserHighlight, kinTypeStrings);
//        kinTypeStrings = graphPanel.getKinTypeStrigs();
                        } catch (EntityServiceException exception) {
                            GuiHelper.linorgBugCatcher.logError(exception);
                            ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Failed to load all entities required", "Draw Graph");
                        }
                        progressBar.setVisible(false);
                    }
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
                panelSetting.addTargetPanel(new KinTermPanel(this, kinTermGroup));
            }
        }
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
        return graphSorter.getDataNodes();
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
        if (arbilDataNodesFirstLoadDone.contains(arbilNode)) {
//         todo: this needs to be updated to be multi threaded so users can link or save multiple nodes at once
            boolean dataBaseRequiresUpdate = false;
            boolean redrawRequired = false;
            if (arbilNode instanceof ArbilDataNode) {
                ArbilDataNode arbilDataNode = (ArbilDataNode) arbilNode;
                UniqueIdentifier uniqueIdentifier = registeredArbilDataNode.get(arbilDataNode);
                // find the entity data for this arbil data node
                for (EntityData entityData : graphSorter.getDataNodes()) {
                    if (entityData.getUniqueIdentifier().equals(uniqueIdentifier)) {
                        // check if the metadata has been changed
                        // todo: something here fails to act on multiple nodes that have changed (it is the db update that was missed)
                        if (entityData.metadataRequiresSave && !arbilDataNode.getNeedsSaveToDisk(false)) {
                            dataBaseRequiresUpdate = true;
                            redrawRequired = true;
                        }
                        // clear or set the needs save flag
                        entityData.metadataRequiresSave = arbilDataNode.getNeedsSaveToDisk(false);
                        if (entityData.metadataRequiresSave) {
                            redrawRequired = true;
                        }
                    }
                }
                if (dataBaseRequiresUpdate) {
                    entityCollection.updateDatabase(arbilDataNode.getURI());
                    graphPanel.getIndexParameters().valuesChanged = true;
                }
            }
            if (redrawRequired) {
                drawGraph();
            }
        }
        if (!arbilNode.isLoading()) {
            // this is to make sure that the initial loading process does not cause db updates nor graph redraws
            arbilDataNodesFirstLoadDone.add(arbilNode);
        }
    }

    public void dataNodeChildAdded(ArbilNode destination, ArbilNode newChildNode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void dataNodeRemoved(ArbilNode adn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
