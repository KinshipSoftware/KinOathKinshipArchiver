package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
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
import nl.mpi.kinnate.kindata.EntityRelation;
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
    }

    public void loadProjectTree() {
        this.add(progressBar, BorderLayout.PAGE_END);
        progressBar.setIndeterminate(true);
        this.revalidate();
        new Thread() {

            @Override
            public void run() {
                ArrayList<ArbilNode> resultsArray = new ArrayList<ArbilNode>();
                EntityData[] searchResults = entityCollection.getEntityByEndPoint(DataTypes.RelationType.ancestor, graphPanel.getIndexParameters());
//                resultsArea.setText("Found " + searchResults.length + " entities\n");
                for (EntityData entityData : searchResults) {
                    boolean isHorizontalEndPoint = true;
                    for (EntityRelation entityRelation : entityData.getAllRelations()) {
                        if (entityRelation.getAlterNode() == null) {
                            // if the alter node has not been loaded then it must not be an end point
                            if (entityRelation.getRelationType() == DataTypes.RelationType.union || entityRelation.getRelationType() == DataTypes.RelationType.sibling) {
                                isHorizontalEndPoint = false;
                                break;
                            }
                        }
                    }
//            if (resultsArray.size() < 1000) {
                    if (isHorizontalEndPoint) {
                        resultsArray.add(new KinTreeNode(entityData, graphPanel.getIndexParameters(), dialogHandler, entityCollection, dataNodeLoader));
                    }
//            } else {
//                resultsArea.append("results limited to 1000\n");
//                break;
//            }
                }
                rootNode.setChildNodes(resultsArray.toArray(new ArbilNode[]{}));
                kinTree.requestResort();
                ProjectTreePanel.this.remove(progressBar);
                ProjectTreePanel.this.revalidate();
            }
        }.start();
        kinTree.requestResort();
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
