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
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import nl.mpi.arbil.plugin.PluginDialogHandler;
import nl.mpi.arbil.ui.ArbilNodeSearchColumnComboBox;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.entityindexer.QueryException;
import nl.mpi.kinnate.plugins.metadatasearch.db.ArbilDatabase;
import nl.mpi.kinnate.plugins.metadatasearch.db.MetadataFileType;
import nl.mpi.kinnate.plugins.metadatasearch.db.DbTreeNode;

/**
 * Document : FacetedTreePanel <br> Created on Aug 23, 2012, 3:20:13 PM <br>
 *
 * @author Peter Withers <br>
 */
public class FacetedTreePanel extends JPanel implements ActionListener {

    final private ArbilDatabase arbilDatabase;
    final PluginDialogHandler arbilWindowManager;
    final JFrame jFrame;
    final ArrayList<SearchOptionBox> searchPathOptionBoxList;
    final JProgressBar jProgressBar;
    final JTree resultsTree;
    final JPanel criterionPanel;
    private MetadataFileType[] metadataFieldTypes = null;

    public FacetedTreePanel(JFrame jFrame) {
        this.jFrame = jFrame;
        arbilWindowManager = new ArbilWindowManager();
        arbilDatabase = new ArbilDatabase(new ArbilSessionStorage(), arbilWindowManager, BugCatcherManager.getBugCatcher());
        this.setLayout(new BorderLayout());
        criterionPanel = new JPanel();
        criterionPanel.setLayout(new FlowLayout());

        final JPanel criterionOuterPanel = new JPanel(new BorderLayout());

        searchPathOptionBoxList = new ArrayList<SearchOptionBox>();

        final JPanel criterionButtonsPanel = new JPanel();

        final JButton addButton = new JButton("+");
        addButton.setActionCommand("add");
        addButton.addActionListener(this);
        criterionButtonsPanel.add(addButton);

        final JButton removeButton = new JButton("-");
        removeButton.setActionCommand("remove");
        removeButton.addActionListener(this);
        criterionButtonsPanel.add(removeButton);

        criterionOuterPanel.add(new JScrollPane(criterionPanel), BorderLayout.CENTER);
        criterionOuterPanel.add(criterionButtonsPanel, BorderLayout.LINE_END);

        this.add(criterionOuterPanel, BorderLayout.PAGE_START);
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel progressPanel = new JPanel(new BorderLayout());

        final JButton createButton = new JButton("create db");
        createButton.setActionCommand("create");
        final JButton optionsButton = new JButton("get options");
        optionsButton.setActionCommand("options");
        optionsButton.addActionListener(this);
        createButton.addActionListener(this);

        JPanel dbButtonsPanel = new JPanel();
        dbButtonsPanel.add(createButton);
        dbButtonsPanel.add(optionsButton);

        progressPanel.add(dbButtonsPanel, BorderLayout.LINE_START);
        jProgressBar = new JProgressBar();

        progressPanel.add(jProgressBar, BorderLayout.CENTER);

        JPanel searchButtonsPanel = new JPanel();
        final JButton searchButton = new JButton("Search");
        searchButton.setActionCommand("search");
        searchButton.addActionListener(this);
        searchButtonsPanel.add(searchButton);
        progressPanel.add(searchButtonsPanel, BorderLayout.LINE_END);

        centerPanel.add(progressPanel, BorderLayout.PAGE_START);
        resultsTree = new JTree();
        centerPanel.add(new JScrollPane(resultsTree), BorderLayout.CENTER);

        this.add(centerPanel, BorderLayout.CENTER);
    }

    static public void main(String[] args) {
        ArbilNodeSearchColumnComboBox.setSessionStorage(new ArbilSessionStorage());
        JFrame jFrame = new JFrame("Search Panel Test");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setContentPane(new FacetedTreePanel(jFrame));
        jFrame.pack();
        jFrame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        jProgressBar.setIndeterminate(true);
        final String actionCommand = e.getActionCommand();
        System.out.println(actionCommand);
        new Thread(getRunnable(actionCommand)).start();
    }

    private Runnable getRunnable(final String actionCommand) {
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
                    ArrayList<MetadataFileType> treeBranchTypeList = new ArrayList<MetadataFileType>();
                    for (SearchOptionBox searchOptionBox : searchPathOptionBoxList) {
                        final Object selectedTypeItem = searchOptionBox.getSelectedItem();
                        if (selectedTypeItem instanceof MetadataFileType) {
                            treeBranchTypeList.add((MetadataFileType) selectedTypeItem);
                        }
                    }
                    System.out.println("run query");
                    DbTreeNode rootTreeNode = arbilDatabase.getTreeData(treeBranchTypeList);
                    resultsTree.setModel(new DefaultTreeModel(rootTreeNode));
                    System.out.println("done");

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
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jProgressBar.setIndeterminate(false);
                    }
                });
            }
        };
    }
}
