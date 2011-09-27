package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableModel;
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
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.kintypestrings.ParserHighlight;

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
    private KinTermTabPane kinTermPanel;
    private EntityService entityIndex;
    private JProgressBar progressBar;
    public ArbilTable imdiTable;
    static private File defaultDiagramTemplate;
    private HashMap<UniqueIdentifier, ArbilDataNode> registeredArbilDataNode;
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

    public KinDiagramPanel(File existingFile) {
        entityCollection = new EntityCollection();
        progressBar = new JProgressBar();
        EntityData[] svgStoredEntities = null;
        graphPanel = new GraphPanel(this);
        kinTypeStringInput = new KinTypeStringInput(defaultString);
        if (existingFile != null && existingFile.exists()) {
            svgStoredEntities = graphPanel.readSvg(existingFile);
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
            graphPanel.generateDefaultSvg();
            graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.KinTerms, 150, false);
            graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.ArchiveLinker, 150, false);
            graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.DiagramTree, 150, true);
            graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.EntitySearch, 150, false);
            graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.IndexerSettings, 150, false);
            graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.KinTypeStrings, 150, false);
            graphPanel.dataStoreSvg.setPanelState(VisiblePanelSetting.PanelType.MetaData, 150, true);
        }
        this.setLayout(new BorderLayout());

        ArbilTableModel imdiTableModel = new ArbilTableModel();
        progressBar.setVisible(false);
        graphPanel.add(progressBar, BorderLayout.PAGE_START);
        imdiTable = new ArbilTable(imdiTableModel, "Selected Nodes");

        TableCellDragHandler tableCellDragHandler = new TableCellDragHandler();
        imdiTable.setTransferHandler(tableCellDragHandler);
        imdiTable.setDragEnabled(true);

        registeredArbilDataNode = new HashMap<UniqueIdentifier, ArbilDataNode>();
        egoSelectionPanel = new EgoSelectionPanel(imdiTable, graphPanel);
        kinTermPanel = new KinTermTabPane(this, graphPanel.getkinTermGroups());

//        kinTypeStringInput.setText(defaultString);

        JPanel kinGraphPanel = new JPanel(new BorderLayout());

        kinTypeHidePane = new HidePane(HidePane.HidePanePosition.top, 0);
        IndexerParametersPanel indexerParametersPanel = new IndexerParametersPanel(this, graphPanel, tableCellDragHandler);
//        JPanel advancedPanel = new JPanel(new BorderLayout());

        JScrollPane tableScrollPane = new JScrollPane(imdiTable);
//        advancedPanel.add(tableScrollPane, BorderLayout.CENTER);
        //HidePane indexParamHidePane = new HidePane(HidePane.HidePanePosition.right, 0);
        //advancedPanel.add(indexParamHidePane, BorderLayout.LINE_END);

        HidePane tableHidePane = new HidePane(HidePane.HidePanePosition.bottom, 0);

        KinDragTransferHandler dragTransferHandler = new KinDragTransferHandler(this);
        graphPanel.setTransferHandler(dragTransferHandler);
        egoSelectionPanel.setTransferHandler(dragTransferHandler);

        EntitySearchPanel entitySearchPanel = new EntitySearchPanel(entityCollection, graphPanel, imdiTable);
        entitySearchPanel.setTransferHandler(dragTransferHandler);

        HidePane egoSelectionHidePane = new HidePane(HidePane.HidePanePosition.left, 0);

        kinTermHidePane = new HidePane(HidePane.HidePanePosition.right, 0);

        graphPanel.setArbilTableModel(imdiTableModel, tableHidePane);

        for (VisiblePanelSetting panelSetting : graphPanel.dataStoreSvg.getVisiblePanels()) {
            switch (panelSetting.getPanelType()) {
                case ArchiveLinker:
                    panelSetting.setTargetPanel(kinTermHidePane, new ArchiveEntityLinkerPanel(imdiTable, dragTransferHandler), "Archive Linker");
                    break;
                case DiagramTree:
                    panelSetting.setTargetPanel(egoSelectionHidePane, egoSelectionPanel, "Diagram Tree");
                    break;
                case EntitySearch:
                    panelSetting.setTargetPanel(egoSelectionHidePane, entitySearchPanel, "Search Entities");
                    break;
                case IndexerSettings:
                    panelSetting.setTargetPanel(kinTypeHidePane, indexerParametersPanel, "Indexer Parameters");
                    break;
                case KinTerms:
                    panelSetting.setTargetPanel(kinTermHidePane, kinTermPanel, "Kin Terms");
                    break;
                case KinTypeStrings:
                    panelSetting.setTargetPanel(kinTypeHidePane, new JScrollPane(kinTypeStringInput), "Kin Type Strings");
                    break;
                case MetaData:
                    panelSetting.setTargetPanel(tableHidePane, tableScrollPane, "Metadata");
                    break;
            }
        }

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
                                EntityData[] graphNodes = entityIndex.processKinTypeStrings(null, graphPanel.dataStoreSvg.egoEntities, graphPanel.dataStoreSvg.requiredEntities, kinTypeStrings, parserHighlight, graphPanel.getIndexParameters(), progressBar);
                                graphSorter.setEntitys(graphNodes);
                                // register interest Arbil updates and update the graph when data is edited in the table
                                registerCurrentNodes(graphSorter.getDataNodes());
                                graphPanel.drawNodes(graphSorter);
                                egoSelectionPanel.setTreeNodes(graphPanel.dataStoreSvg.egoEntities, graphPanel.dataStoreSvg.requiredEntities, graphSorter.getDataNodes());
                            } else {
                                KinTypeStringConverter graphData = new KinTypeStringConverter();
                                graphData.readKinTypes(kinTypeStrings, graphPanel.getkinTermGroups(), graphPanel.dataStoreSvg, parserHighlight);
                                graphPanel.drawNodes(graphData);
                                egoSelectionPanel.setTransientNodes(graphData.getDataNodes());
//                KinDiagramPanel.this.doLayout();
                            }
                            kinTypeStringInput.highlightKinTerms(parserHighlight, kinTypeStrings);
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
        return (graphPanel.hasSaveFileName() && getDefaultDiagramFile() != graphPanel.getFileName());
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
        kinTermPanel.getSelectedKinTermPanel().exportKinTerms();
    }

    public void hideShow() {
        kinTermHidePane.toggleHiddenState();
    }

    public void importKinTerms() {
        kinTermPanel.getSelectedKinTermPanel().importKinTerms();
    }

    public void addKinTermGroup() {
        graphPanel.addKinTermGroup();
        kinTermPanel.updateKinTerms(graphPanel.getkinTermGroups());
    }

    public VisiblePanelSetting[] getVisiblePanels() {
        return graphPanel.dataStoreSvg.getVisiblePanels();
    }

    public void setPanelState(PanelType panelType, int panelWidth, boolean panelVisible) {
        // todo: show / hide the requested panel
        graphPanel.dataStoreSvg.setPanelState(panelType, panelWidth, panelVisible);
    }

    public void setSelectedKinTypeSting(String kinTypeStrings) {
        kinTermPanel.setAddableKinTypeSting(kinTypeStrings);
    }

    public boolean isHidden() {
        return kinTermHidePane.isHidden();
    }

    public EntityData[] getGraphEntities() {
        return graphSorter.getDataNodes();
    }

    private void registerCurrentNodes(EntityData[] currentEntities) {
        // todo: i think this is resolved but double check the issue where arbil nodes update frequency is too high and breaks basex
        // todo: load the nodes in the KinDataNode when putting them in the table and pass on the reload requests here when they occur
        // todo: replace the data node registering process.
//        for (EntityData entityData : currentEntities) {
//            ArbilDataNode arbilDataNode = null;
//            if (!registeredArbilDataNode.containsKey(entityData.getUniqueIdentifier())) {
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
//         todo: this needs to be updated to be multi threaded so users can link or save multiple nodes at once
        boolean dataBaseRequiresUpdate = false;
        boolean redrawRequired = false;
        if (arbilNode instanceof ArbilDataNode) {
            ArbilDataNode arbilDataNode = (ArbilDataNode) arbilNode;
            // find the entity data for this arbil data node
            for (EntityData entityData : graphSorter.getDataNodes()) {
                try {
                    String entityPath = entityData.getEntityPath();
                    if (entityPath != null && arbilDataNode.getURI().equals(new URI(entityPath))) {
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
                } catch (URISyntaxException exception) {
                    GuiHelper.linorgBugCatcher.logError(exception);
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

    public void dataNodeChildAdded(ArbilNode destination, ArbilNode newChildNode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void dataNodeRemoved(ArbilNode adn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
