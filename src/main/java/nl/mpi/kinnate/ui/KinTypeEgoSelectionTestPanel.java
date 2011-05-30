package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityService;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.entityindexer.QueryParser;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.kintypestrings.ParserHighlight;

/**
 *  Document   : KinTypeStringTestPanel
 *  Created on : Sep 29, 2010, 12:52:01 PM
 *  Author     : Peter Withers
 */
public class KinTypeEgoSelectionTestPanel extends JPanel implements SavePanel, KinTermSavePanel, ArbilDataNodeContainer {

    private JTextPane kinTypeStringInput;
    private GraphPanel graphPanel;
    private GraphSorter graphSorter;
    private EgoSelectionPanel egoSelectionPanel;
    private HidePane kinTermHidePane;
    private HidePane kinTypeHidePane;
    private KinTermTabPane kinTermPanel;
    private EntityService entityIndex;
    private ArrayList<String> registeredEntityIds;
    private String defaultString = "# The kin type strings entered here will determine how the entities show on the graph below\n";
    public static String defaultGraphString = "# The kin type strings entered here will determine how the entities show on the graph below\n"
            + "# Enter one string per line.\n"
            //+ "# By default all relations of the selected entity will be shown.\n"
            + "# for example:\n"
            + "E=[Bob]MFM\n"
            + "E=[Bob]MZ\n"
            + "E=[Bob]F\n"
            + "E=[Bob]M\n"
            + "E=[Bob]S";
//    private String kinTypeStrings[] = new String[]{};

    public KinTypeEgoSelectionTestPanel(File existingFile) {
        EntityData[] svgStoredEntities = null;
        graphPanel = new GraphPanel(this);
        kinTypeStringInput = new JTextPane();
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
        }
        this.setLayout(new BorderLayout());
        registeredEntityIds = new ArrayList<String>();
        egoSelectionPanel = new EgoSelectionPanel();
        kinTermPanel = new KinTermTabPane(this, graphPanel.getkinTermGroups());
        // set the styles for the kin type string text
        Style styleComment = kinTypeStringInput.addStyle("Comment", null);
//        StyleConstants.setForeground(styleComment, new Color(247,158,9));
        StyleConstants.setForeground(styleComment, Color.GRAY);
        Style styleKinType = kinTypeStringInput.addStyle("KinType", null);
        StyleConstants.setForeground(styleKinType, new Color(43, 32, 161));
        Style styleQuery = kinTypeStringInput.addStyle("Query", null);
        StyleConstants.setForeground(styleQuery, new Color(183, 7, 140));
        Style styleError = kinTypeStringInput.addStyle("Error", null);
//        StyleConstants.setForeground(styleError, new Color(172,3,57));
        StyleConstants.setForeground(styleError, Color.RED);
        Style styleUnknown = kinTypeStringInput.addStyle("Unknown", null);
        StyleConstants.setForeground(styleUnknown, Color.BLACK);

//        kinTypeStringInput.setText(defaultString);
//        kinTypeStringInput.setBorder(javax.swing.BorderFactory.createTitledBorder("Kin Type Strings"));
        JPanel kinGraphPanel = new JPanel(new BorderLayout());
//        kinGraphPanel.add(kinTypeStringInput, BorderLayout.PAGE_START);

        JPanel kintermSplitPane = new JPanel(new BorderLayout());
        kinTypeHidePane = new HidePane(new JScrollPane(kinTypeStringInput), "Kin Type Strings", BorderLayout.PAGE_END);
        kintermSplitPane.add(kinTypeHidePane, BorderLayout.PAGE_START);
//        JSplitPane egoSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//        kinGraphPanel.add(egoSplitPane, BorderLayout.CENTER);
//        outerSplitPane.setDividerLocation(0.5); // todo: add this to its parent so that the divider position sticks
        kintermSplitPane.add(new HidePane(egoSelectionPanel, "Ego Selection", BorderLayout.LINE_END), BorderLayout.LINE_START);
        kintermSplitPane.add(graphPanel, BorderLayout.CENTER);
        kinTermHidePane = new HidePane(kinTermPanel, "Kin Terms", BorderLayout.LINE_START);
        kintermSplitPane.add(kinTermHidePane, BorderLayout.LINE_END);
        kinGraphPanel.add(kintermSplitPane);

        ArbilTableModel imdiTableModel = new ArbilTableModel();
        ArbilTable imdiTable = new ArbilTable(imdiTableModel, "Selected Nodes");
        TableCellDragHandler tableCellDragHandler = new TableCellDragHandler();
        imdiTable.setTransferHandler(tableCellDragHandler);
        imdiTable.setDragEnabled(true);
        graphPanel.setArbilTableModel(imdiTableModel);

        JScrollPane tableScrollPane = new JScrollPane(imdiTable);
//        Dimension minimumSize = new Dimension(0, 0);
//        fieldListTabs.setMinimumSize(minimumSize);
//        tableScrollPane.setMinimumSize(minimumSize);

        // EntityIndex loads the xml files and reads the document for entity data
//        entityIndex = new EntityIndex(graphPanel.getIndexParameters());
        // EntityCollection queries the xml collection to get the entity data
        entityIndex = new QueryParser(svgStoredEntities);

        graphSorter = new GraphSorter();

        IndexerParametersPanel indexerParametersPanel = new IndexerParametersPanel(this, graphPanel, tableCellDragHandler);
        JPanel advancedPanel = new JPanel(new BorderLayout());
        advancedPanel.add(tableScrollPane, BorderLayout.CENTER);
        advancedPanel.add(new HidePane(indexerParametersPanel, "Indexer Parameters", BorderLayout.LINE_START), BorderLayout.LINE_END);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, kinGraphPanel, advancedPanel);
        this.add(splitPane);


        kinTypeStringInput.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                if (kinTypeStringInput.getText().equals(defaultString)) {
                    kinTypeStringInput.setText("");
//                    kinTypeStringInput.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (kinTypeStringInput.getText().length() == 0) {
                    kinTypeStringInput.setText(defaultString);
//                    kinTypeStringInput.setForeground(Color.lightGray);
                }
            }
        });
        kinTypeStringInput.addKeyListener(new KeyListener() {

            String previousKinTypeStrings = null;

            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                synchronized (e) {
                    if (previousKinTypeStrings == null || !previousKinTypeStrings.equals(kinTypeStringInput.getText())) {
                        graphPanel.setKinTypeStrigs(kinTypeStringInput.getText().split("\n"));
//                kinTypeStrings = graphPanel.getKinTypeStrigs();
                        drawGraph();
                        previousKinTypeStrings = kinTypeStringInput.getText();
                    }
                }
            }
        });
    }

    public void createDefaultGraph(String defaultGraphString) {
        kinTypeStringInput.setText(defaultGraphString);
        graphPanel.setKinTypeStrigs(kinTypeStringInput.getText().split("\n"));
        drawGraph();
    }

    public void drawGraph() {
        try {
            String[] kinTypeStrings = graphPanel.getKinTypeStrigs();
            ParserHighlight[] parserHighlight = new ParserHighlight[kinTypeStrings.length];
            EntityData[] graphNodes = entityIndex.getRelationsOfEgo(null, graphPanel.dataStoreSvg.egoEntities, graphPanel.dataStoreSvg.requiredEntities, kinTypeStrings, parserHighlight, graphPanel.getIndexParameters());
            boolean visibleNodeFound = false;
            for (EntityData currentNode : graphNodes) {
                if (currentNode.isVisible) {
                    visibleNodeFound = true;
                    break;
                }
            }
            if (!visibleNodeFound /*graphNodes.length == 0*/) {
                KinTypeStringConverter graphData = new KinTypeStringConverter();
                graphData.readKinTypes(kinTypeStringInput.getText().split("\n"), graphPanel.getkinTermGroups(), graphPanel.dataStoreSvg, parserHighlight);
                graphPanel.drawNodes(graphData);
                egoSelectionPanel.setTransientNodes(graphData.getDataNodes());
//                KinTypeEgoSelectionTestPanel.this.doLayout();
            } else {
                graphSorter.setEntitys(graphNodes);
                // register interest Arbil updates and update the graph when data is edited in the table
                registerCurrentNodes(graphSorter.getDataNodes());
                graphPanel.drawNodes(graphSorter);
                egoSelectionPanel.setTreeNodes(graphPanel.dataStoreSvg.egoEntities, graphPanel.dataStoreSvg.requiredEntities, graphSorter.getDataNodes());
            }
            StyledDocument styledDocument = kinTypeStringInput.getStyledDocument();
            int lineStart = 0;
            for (int lineCounter = 0; lineCounter < parserHighlight.length; lineCounter++) {
                ParserHighlight currentHighlight = parserHighlight[lineCounter];
//                int lineStart = styledDocument.getParagraphElement(lineCounter).getStartOffset();
//                int lineEnd = styledDocument.getParagraphElement(lineCounter).getEndOffset();
                int lineEnd = lineStart + kinTypeStrings[lineCounter].length();
                styledDocument.setCharacterAttributes(lineStart, lineEnd, kinTypeStringInput.getStyle("Unknown"), true);
                while (currentHighlight.highlight != null) {
                    int startPos = lineStart + currentHighlight.startChar;
                    int charCount = lineEnd - lineStart;
                    if (currentHighlight.nextHighlight.highlight != null) {
                        charCount = currentHighlight.nextHighlight.startChar - currentHighlight.startChar;
                    }
                    if (currentHighlight.highlight != null) {
                        String styleName = currentHighlight.highlight.name();
                        styledDocument.setCharacterAttributes(startPos, charCount, kinTypeStringInput.getStyle(styleName), true);
                    }
                    currentHighlight = currentHighlight.nextHighlight;
                }
                lineStart += kinTypeStrings[lineCounter].length() + 1;
            }
//        kinTypeStrings = graphPanel.getKinTypeStrigs();
        } catch (EntityServiceException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Failed to load an entity", "Kinnate");
        }
    }

    @Deprecated
    public void setDisplayNodes(String typeString, String[] egoIdentifierArray) {
        // todo: should this be replaced by the required nodes?
        if (kinTypeStringInput.getText().equals(defaultString)) {
            kinTypeStringInput.setText("");
        }
        String kinTermContents = kinTypeStringInput.getText();
        for (String currentId : egoIdentifierArray) {
            kinTermContents = kinTermContents + typeString + "=[" + currentId + "]\n";
        }
        kinTypeStringInput.setText(kinTermContents);
        graphPanel.setKinTypeStrigs(kinTypeStringInput.getText().split("\n"));
//        kinTypeStrings = graphPanel.getKinTypeStrigs();
        drawGraph();
    }

    public void setEgoNodes(URI[] egoPathArray, String[] egoIdentifierArray) {
        graphPanel.dataStoreSvg.egoEntities = new HashSet<String>(Arrays.asList(egoIdentifierArray));
        drawGraph();
    }

    public void addEgoNodes(URI[] egoPathArray, String[] egoIdentifierArray) {
        graphPanel.dataStoreSvg.egoEntities.addAll(Arrays.asList(egoIdentifierArray));
        drawGraph();
    }

    public void removeEgoNodes(String[] egoIdentifierArray) {
        graphPanel.dataStoreSvg.egoEntities.removeAll(Arrays.asList(egoIdentifierArray));
        drawGraph();
    }

    public void addRequiredNodes(URI[] egoPathArray, String[] egoIdentifierArray) {
        graphPanel.dataStoreSvg.requiredEntities.addAll(Arrays.asList(egoIdentifierArray));
        drawGraph();
    }

    public void removeRequiredNodes(String[] egoIdentifierArray) {
        graphPanel.dataStoreSvg.requiredEntities.removeAll(Arrays.asList(egoIdentifierArray));
        drawGraph();
    }

    public boolean hasSaveFileName() {
        return graphPanel.hasSaveFileName();
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

    public void setSelectedKinTypeSting(String kinTypeStrings) {
        kinTermPanel.setAddableKinTypeSting(kinTypeStrings);
    }

    public boolean isHidden() {
        return kinTermHidePane.isHidden();
    }

    private void registerCurrentNodes(EntityData[] currentEntities) {
        // todo: update the graph when data is edited in the table
        // todo: resolve issue where arbil nodes update frequency is too high and breaks basex
//        for (EntityData entityData : currentEntities) {
//            if (!registeredEntityIds.contains(entityData.getUniqueIdentifier())) {
//                try {
//                    registeredEntityIds.add(entityData.getUniqueIdentifier());
//                    ArbilDataNode arbilDataNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, new URI(entityData.getEntityPath()));
//                    arbilDataNode.registerContainer(this);
////                // todo: keep track of registered nodes and remove the unrequired ones here
//                } catch (URISyntaxException exception) {
//                    GuiHelper.linorgBugCatcher.logError(exception);
//                }
//            }
//        }
    }

    public void dataNodeIconCleared(String[] selectedIdentifiers) {
//        for(String currentIdentifier : selectedIdentifiers)
//        graphPanel.getPathForElementId(currentIdentifier)...
        // todo: get the paths and provide as URIs to update the database: assuming that updating individual files is worthwhile
        new EntityCollection().updateDatabase(null);
        graphPanel.getIndexParameters().valuesChanged = true;
        drawGraph();
    }

    public void dataNodeIconCleared(ArbilDataNode arbilDataNode) {
        new EntityCollection().updateDatabase(arbilDataNode.getURI());
        graphPanel.getIndexParameters().valuesChanged = true;
        drawGraph();
    }

    public void dataNodeRemoved(ArbilDataNode adn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
