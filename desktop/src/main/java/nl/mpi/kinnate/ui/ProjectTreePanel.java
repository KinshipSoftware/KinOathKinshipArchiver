package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.data.KinTreeNode;
import nl.mpi.kinnate.data.ProjectNode;
import nl.mpi.kinnate.entityindexer.DatabaseUpdateListener;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.svg.GraphPanel;

/**
 * Document : ProjectTreePanel
 * Created on : Apr 25, 2012, 11:03:10 AM
 * Author : Peter Withers
 */
public class ProjectTreePanel extends JPanel implements DatabaseUpdateListener {

    private EntityCollection entityCollection;
    private KinTree kinTree;
    private String panelName;
    private KinDiagramPanel kinDiagramPanel;
    private GraphPanel graphPanel;
    private ProjectNode rootNode;
    private MessageDialogHandler dialogHandler;
    private ArbilDataNodeLoader dataNodeLoader;
    private JProgressBar progressBar;
    private ArrayList<KinTreeNode> treeNodesArray = null;
    private int currentPage = 0;
    private int maxNodesPerPage = 100;
    private JPanel pagePanel;
    private JLabel currentPageLabel;

    public ProjectTreePanel(EntityCollection entityCollection, String panelName, KinDiagramPanel kinDiagramPanel, GraphPanel graphPanel, MessageDialogHandler dialogHandler, ArbilDataNodeLoader dataNodeLoader) {
        super(new BorderLayout());
        this.setName(panelName);
        this.entityCollection = entityCollection;
        this.panelName = panelName;
        this.kinDiagramPanel = kinDiagramPanel;
        this.graphPanel = graphPanel;
        this.dataNodeLoader = dataNodeLoader;
        this.dialogHandler = dialogHandler;
        this.rootNode = new ProjectNode(entityCollection, panelName);
        kinTree = new KinTree(kinDiagramPanel, graphPanel, rootNode);
        kinTree.setBackground(this.getBackground());
        this.add(new JScrollPane(kinTree), BorderLayout.CENTER);
        progressBar = new JProgressBar();
        ActionListener actionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if ("<".equals(e.getActionCommand())) {
                    currentPage--;
                } else if (">".equals(e.getActionCommand())) {
                    currentPage++;
                }
                showPage();
            }
        };
        final JButton previousButton = new JButton("<");
        final JButton nextButton = new JButton(">");
        currentPageLabel = new JLabel("", JLabel.CENTER);

        previousButton.setActionCommand("<");
        nextButton.setActionCommand(">");

        previousButton.addActionListener(actionListener);
        nextButton.addActionListener(actionListener);

        final Dimension preferredSize = previousButton.getPreferredSize();
        final Dimension buttonDimension = new Dimension(preferredSize.height, preferredSize.height);
        previousButton.setPreferredSize(buttonDimension);
        nextButton.setPreferredSize(buttonDimension);

        pagePanel = new JPanel(new BorderLayout());
        pagePanel.add(previousButton, BorderLayout.LINE_START);
        pagePanel.add(currentPageLabel, BorderLayout.CENTER);
        pagePanel.add(nextButton, BorderLayout.LINE_END);
    }

    private void showPage() {
        if (treeNodesArray != null) {
            int pageCount = treeNodesArray.size() / maxNodesPerPage;
            if (currentPage < 0) {
                currentPage = 0;
            }
            if (currentPage > pageCount) {
                currentPage = pageCount;
            }
            final int startNode = currentPage * maxNodesPerPage;
            int endNode = startNode + maxNodesPerPage;
            if (endNode > treeNodesArray.size()) {
                endNode = treeNodesArray.size();
            }
            currentPageLabel.setText(currentPage + " of " + pageCount);
            rootNode.setChildNodes(treeNodesArray.subList(startNode, endNode).toArray(new ArbilNode[]{}));
            kinTree.requestResort();

            this.add(pagePanel, BorderLayout.PAGE_END);
        }
        this.revalidate();
    }

    public void loadProjectTree() {
        this.remove(pagePanel);
        this.add(progressBar, BorderLayout.PAGE_END);
        progressBar.setIndeterminate(true);
        this.revalidate();
        kinTree.requestResort();
        new Thread() {

            @Override
            public void run() {
//                int nodeCount = 0;
                treeNodesArray = new ArrayList<KinTreeNode>();
                EntityData[] projectEntities = entityCollection.getEntityByEndPoint(DataTypes.RelationType.ancestor, graphPanel.getIndexParameters());
//                resultsArea.setText("Found " + searchResults.length + " entities\n");
                for (EntityData entityData : projectEntities) {
                    boolean isHorizontalEndPoint = true;
                    // this check is for end points that have a sibling or spouse who are not an end point, but it is removed because it is not possible to browse to a spouse or sibling in a directional branch
//                    for (EntityRelation entityRelation : entityData.getAllRelations()) {
//                        if (entityRelation.getAlterNode() == null) {
//                            // if the alter node has not been loaded then it must not be an end point
//                            if (entityRelation.getRelationType() == DataTypes.RelationType.union || entityRelation.getRelationType() == DataTypes.RelationType.sibling) {
//                                isHorizontalEndPoint = false;
//                                break;
//                            }
//                        }
//                    }
//            if (resultsArray.size() < 1000) {
                    if (isHorizontalEndPoint) {
                        treeNodesArray.add(new KinTreeNode(entityData, graphPanel.getIndexParameters(), dialogHandler, entityCollection, dataNodeLoader));
//                        final String testNodeName = "Node: " + nodeCount;
//                        System.out.println("testNodeName: " + testNodeName);
//                        treeNodesArray.add(new ContainerNode(testNodeName, null, new ArbilNode[0]));
//                        nodeCount++;
                    }
//            } else {
//                resultsArea.append("results limited to 1000\n");
//                break;
//            }
                }
                Collections.sort(treeNodesArray);
                ProjectTreePanel.this.remove(progressBar);
                ProjectTreePanel.this.revalidate();
                showPage();
            }
        }.start();
    }

    @Override
    public void setTransferHandler(TransferHandler newHandler) {
        kinTree.setTransferHandler(newHandler);
        kinTree.setDragEnabled(true);
    }

    public void updateOccured() {
        loadProjectTree();
    }
}
