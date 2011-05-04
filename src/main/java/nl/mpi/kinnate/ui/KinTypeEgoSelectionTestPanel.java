package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.EntityService;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.entityindexer.QueryParser;
import nl.mpi.kinnate.entityindexer.QueryParser.ParserHighlight;

/**
 *  Document   : KinTypeStringTestPanel
 *  Created on : Sep 29, 2010, 12:52:01 PM
 *  Author     : Peter Withers
 */
public class KinTypeEgoSelectionTestPanel extends JPanel implements SavePanel, KinTermSavePanel {

    private JTextPane kinTypeStringInput;
    private GraphPanel graphPanel;
    private GraphSorter graphSorter;
    private EgoSelectionPanel egoSelectionPanel;
    private HidePane kinTermHidePane;
    private HidePane kinTypeHidePane;
    private KinTermTabPane kinTermPanel;
    private EntityService entityIndex;
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
        this.setLayout(new BorderLayout());
        graphPanel = new GraphPanel(this);
        egoSelectionPanel = new EgoSelectionPanel();
        kinTermPanel = new KinTermTabPane(this, graphPanel.getkinTermGroups());
        kinTypeStringInput = new JTextPane();
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
        entityIndex = new QueryParser();

        graphSorter = new GraphSorter();
        if (existingFile != null && existingFile.exists()) {
            graphPanel.readSvg(existingFile);
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
            // todo: filter out the noise and only save or use the actual kin type strings
//            graphPanel.setKinTypeStrigs(kinTypeStringInput.getText().split("\n"));
//            kinTypeStrings = graphPanel.getKinTypeStrigs();
        }

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

            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                graphPanel.setKinTypeStrigs(kinTypeStringInput.getText().split("\n"));
//                kinTypeStrings = graphPanel.getKinTypeStrigs();
                drawGraph();
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
            graphSorter.setEntitys(entityIndex.getRelationsOfEgo(null, graphPanel.getEgoUniquiIdentifiersList(), kinTypeStrings, parserHighlight, graphPanel.getIndexParameters()));
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
        } catch (EntityServiceException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Failed to load an entity", "Kinnate");
        }
        egoSelectionPanel.setEgoNodes(graphPanel.getEgoPaths());
//        kinTypeStrings = graphPanel.getKinTypeStrigs();
        graphPanel.drawNodes(graphSorter);
    }

    public void setEgoNodes(URI[] egoPathArray, String[] egoIdentifierArray) {
        graphPanel.setEgoList(egoPathArray, egoIdentifierArray);
        drawGraph();
    }

    public void setDisplayNodes(String typeString, String[] egoIdentifierArray) {
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

    public void addEgoNodes(URI[] egoPathArray, String[] egoIdentifierArray) {
        graphPanel.addEgo(egoPathArray, egoIdentifierArray);
        drawGraph();
    }

    public void removeEgoNodes(String[] egoIdentifierArray) {
        graphPanel.removeEgo(egoIdentifierArray);
        drawGraph();
    }

    public boolean hasSaveFileName() {
        return graphPanel.hasSaveFileName();
    }

    public boolean requiresSave() {
        return graphPanel.requiresSave();
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
}
