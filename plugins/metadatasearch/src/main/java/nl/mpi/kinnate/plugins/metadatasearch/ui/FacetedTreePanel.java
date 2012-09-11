/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.kinnate.plugins.metadatasearch.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import nl.mpi.arbil.ArbilDesktopInjector;
import nl.mpi.arbil.ArbilVersion;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.plugin.PluginArbilDataNodeLoader;
import nl.mpi.arbil.plugin.PluginDialogHandler;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilMimeHashQueue;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.entityindexer.QueryException;
import nl.mpi.kinnate.plugins.metadatasearch.db.ArbilDatabase;
import nl.mpi.kinnate.plugins.metadatasearch.db.DbTreeNode;
import nl.mpi.kinnate.plugins.metadatasearch.db.MetadataFileType;
import nl.mpi.kinnate.plugins.metadatasearch.db.MetadataTreeNode;

/**
 * Document : FacetedTreePanel <br> Created on Aug 23, 2012, 3:20:13 PM <br>
 *
 * @author Peter Withers <br>
 */
public class FacetedTreePanel extends JPanel implements ActionListener {

    final private ArbilDatabase arbilDatabase;
    final private PluginDialogHandler arbilWindowManager;
    final private PluginArbilDataNodeLoader arbilDataNodeLoader;
    final private ArrayList<SearchOptionBox> searchPathOptionBoxList;
    final private JProgressBar jProgressBar;
    private int actionProgressCounter = 0;
    final private JTree resultsTree;
    final private DefaultTreeModel defaultTreeModel;
    final private ArbilTable arbilTable;
    final private ArbilTableModel arbilTableModel;
    final private JPanel criterionPanel;
    private MetadataFileType[] metadataFieldTypes = null;

    public FacetedTreePanel(final PluginArbilDataNodeLoader arbilDataNodeLoader, final PluginDialogHandler dialogHandler) {
        this.arbilDataNodeLoader = arbilDataNodeLoader;
        arbilWindowManager = new ArbilWindowManager();
        arbilDatabase = new ArbilDatabase(new ArbilSessionStorage(), arbilWindowManager, BugCatcherManager.getBugCatcher());
        this.setLayout(new BorderLayout());
        criterionPanel = new JPanel();
        criterionPanel.setLayout(new FlowLayout());

        final JPanel criterionOuterPanel = new JPanel(new BorderLayout());

        searchPathOptionBoxList = new ArrayList<SearchOptionBox>();

        final JPanel criterionButtonsPanel = new JPanel(new BorderLayout());

        final JButton addButton = new JButton("+");
        addButton.setActionCommand("add");
        addButton.addActionListener(this);
        criterionButtonsPanel.add(addButton, BorderLayout.PAGE_START);

        final JButton removeButton = new JButton("-");
        removeButton.setActionCommand("remove");
        removeButton.addActionListener(this);
        criterionButtonsPanel.add(removeButton, BorderLayout.PAGE_END);

        criterionOuterPanel.add(new JScrollPane(criterionPanel), BorderLayout.CENTER);
        criterionOuterPanel.add(criterionButtonsPanel, BorderLayout.LINE_END);

        this.add(criterionOuterPanel, BorderLayout.PAGE_START);
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel progressPanel = new JPanel(new BorderLayout());

//        final JButton createButton = new JButton("create db");
//        createButton.setActionCommand("create");
//        final JButton optionsButton = new JButton("reload options");
//        optionsButton.setActionCommand("options");
//        optionsButton.addActionListener(this);
//        createButton.addActionListener(this);

        JPanel dbButtonsPanel = new JPanel();
//        dbButtonsPanel.add(createButton);
//        dbButtonsPanel.add(optionsButton);

        progressPanel.add(dbButtonsPanel, BorderLayout.LINE_START);
        jProgressBar = new JProgressBar();

        progressPanel.add(jProgressBar, BorderLayout.CENTER);

        JPanel searchButtonsPanel = new JPanel();
//        final JButton searchButton = new JButton("Search");
//        searchButton.setActionCommand("search");
//        searchButton.addActionListener(this);
//        searchButtonsPanel.add(searchButton);
        progressPanel.add(searchButtonsPanel, BorderLayout.LINE_END);

        centerPanel.add(progressPanel, BorderLayout.PAGE_START);
        defaultTreeModel = new DefaultTreeModel(new DbTreeNode("Please add or select a facet"));
        resultsTree = new JTree(defaultTreeModel);
        resultsTree.setCellRenderer(new SearchTreeCellRenderer());
        resultsTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent tse) {
                ArrayList<ArbilDataNode> arbilDataNodeList = new ArrayList<ArbilDataNode>();
                final TreePath[] selectionPaths = resultsTree.getSelectionPaths();
                if (selectionPaths != null) {
                    for (TreePath treePath : selectionPaths) {
                        final Object lastPathComponent = treePath.getLastPathComponent();
                        if (lastPathComponent instanceof MetadataTreeNode) {
                            final ArbilDataNode arbilNode = ((MetadataTreeNode) lastPathComponent).getArbilNode();
                            if (arbilNode != null) {
                                arbilDataNodeList.add(arbilNode);
                            }
                        }
                    }
                }
                arbilTableModel.removeAllArbilDataNodeRows();
                arbilTableModel.addArbilDataNodes(arbilDataNodeList.toArray(new ArbilDataNode[0]));
            }
        });
        centerPanel.add(new JScrollPane(resultsTree), BorderLayout.CENTER);

        arbilTableModel = new ArbilTableModel(null);
        arbilTable = new ArbilTable(arbilTableModel, "FacetedTreeSelectionTable");
        JSplitPane jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerPanel, new JScrollPane(arbilTable));

        this.add(jSplitPane, BorderLayout.CENTER);
    }

    static public void main(String[] args) {
//        ArbilIcons.setVersionManager(new ApplicationVersionManager(new ArbilVersion()));
//        final ArbilSessionStorage arbilSessionStorage = new ArbilSessionStorage();
//        ArbilNodeSearchColumnComboBox.setSessionStorage(arbilSessionStorage);
//        ArbilFieldViews.setSessionStorage(arbilSessionStorage);
//        MetadataReader.setSessionStorage(arbilSessionStorage);
//        ArbilTemplateManager.setSessionStorage(arbilSessionStorage);
//        ArbilField.setSessionStorage(arbilSessionStorage);
//        ArbilVocabularies.setSessionStorage(arbilSessionStorage);
        final ArbilDesktopInjector injector = new ArbilDesktopInjector();
        injector.injectHandlers(new ApplicationVersionManager(new ArbilVersion()));
//        final ApplicationVersionManager versionManager = new ApplicationVersionManager(new ArbilVersion());
//        final ArbilDesktopInjector injector = new ArbilDesktopInjector();
//        injector.injectHandlers(versionManager);
        final ArbilSessionStorage arbilSessionStorage = new ArbilSessionStorage(); // todo: this is should use the same session storage as the injector but it is either not clear how to get it or it is not possible without changes


//        ArbilTableModel.setMessageDialogHandler(new ArbilWindowManager());
//        ArbilTable.set
        JFrame jFrame = new JFrame("Faceted Tree Panel Test");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final ArbilWindowManager arbilWindowManager = new ArbilWindowManager();
        final ArbilDataNodeLoader arbilDataNodeLoader = new ArbilDataNodeLoader(arbilWindowManager, arbilSessionStorage, new ArbilMimeHashQueue(arbilWindowManager, arbilSessionStorage), new ArbilTreeHelper(arbilSessionStorage, arbilWindowManager));
        final FacetedTreePanel facetedTreePanel = new FacetedTreePanel(arbilDataNodeLoader, arbilWindowManager);
        jFrame.setContentPane(facetedTreePanel);
        jFrame.pack();
        jFrame.setVisible(true);
        // trigger the facets to load
//        new Thread(facetedTreePanel.getRunnable("add")).start();
        new Thread(facetedTreePanel.getRunnable("options")).start();
    }

    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                actionProgressCounter++;
                jProgressBar.setIndeterminate(actionProgressCounter > 0);
            }
        });
        final String actionCommand = e.getActionCommand();
        System.out.println(actionCommand);
        new Thread(getRunnable(actionCommand)).start();
    }

    private void updateTree() {
        ArrayList<MetadataFileType> treeBranchTypeList = new ArrayList<MetadataFileType>();
        for (SearchOptionBox searchOptionBox : searchPathOptionBoxList) {
            final Object selectedTypeItem = searchOptionBox.getSelectedItem();
            if (selectedTypeItem instanceof MetadataFileType) {
                treeBranchTypeList.add((MetadataFileType) selectedTypeItem);
            }
        }
        System.out.println("run query");
        final DbTreeNode rootTreeNode = arbilDatabase.getTreeData(treeBranchTypeList);
        rootTreeNode.setParentDbTreeNode(null, defaultTreeModel, arbilDataNodeLoader);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                defaultTreeModel.setRoot(rootTreeNode);
            }
        });
        System.out.println("done");
    }

    public Runnable getRunnable(final String actionCommand) {
        return new Runnable() {
            public void run() {
                if ("create".equals(actionCommand)) {
                    try {
                        System.out.println("create db");
                        arbilDatabase.createDatabase();
                        System.out.println("done");
                    } catch (QueryException exception) {
                        arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Database Error");
                    }
                } else if ("options".equals(actionCommand)) {
                    System.out.println("run query");
                    metadataFieldTypes = arbilDatabase.getTreeFieldTypes(null);
                    System.out.println("done");
                    for (SearchOptionBox searchOptionBox : searchPathOptionBoxList) {
                        searchOptionBox.setTypes(metadataFieldTypes);
                    }
                } else if ("treechange".equals(actionCommand)) {
                    updateTree();

                } else if ("add".equals(actionCommand)) {
                    final SearchOptionBox treePathOptionBox = new SearchOptionBox();
                    if (metadataFieldTypes == null) {
                        treePathOptionBox.addItem("Loading");
                    } else {
                        treePathOptionBox.setTypes(metadataFieldTypes);
                    }
                    treePathOptionBox.addActionListener(FacetedTreePanel.this);
                    treePathOptionBox.setActionCommand("treechange");
                    searchPathOptionBoxList.add(treePathOptionBox);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            criterionPanel.add(treePathOptionBox);
                            criterionPanel.revalidate();
                            criterionPanel.repaint();
                        }
                    });
                    updateTree();
                } else if ("remove".equals(actionCommand)) {
                    if (searchPathOptionBoxList.size() > 0) {
                        final SearchOptionBox searchOptionBox = searchPathOptionBoxList.remove(searchPathOptionBoxList.size() - 1);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                criterionPanel.remove(searchOptionBox);
                                criterionPanel.revalidate();
                                criterionPanel.repaint();
                            }
                        });
                    }
                    updateTree();
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        actionProgressCounter--;
                        jProgressBar.setIndeterminate(actionProgressCounter > 0);
                    }
                });
            }
        };
    }
}
