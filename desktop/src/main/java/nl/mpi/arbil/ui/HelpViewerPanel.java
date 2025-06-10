/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import nl.mpi.arbil.help.HelpIndex;
import nl.mpi.arbil.help.HelpItem;
import nl.mpi.arbil.help.HelpItemsParser;
import nl.mpi.arbil.util.BugCatcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * A panel to view help resource sets consisting of an index file and a set of HTML pages and linked images.
 * This evolved from a class formerly known as ArbilHelp. A class by that name still exists but now is an Arbil specific extention
 * of this class.
 *
 * @author Peter Withers <Peter.Withers@mpi.nl>
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class HelpViewerPanel extends javax.swing.JPanel {

    private final static Logger logger = LoggerFactory.getLogger(HelpViewerPanel.class);
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    final static public String SHOTCUT_KEYS_PAGE = widgets.getString("SHORTCUT KEYS");
    final static public String INTRODUCTION_PAGE = widgets.getString("OVERVIEW");
    final static public String helpWindowTitle = widgets.getString("HELP VIEWER");
    final static public String DEFAULT_HELPSET = widgets.getString("HELP");
    private JTabbedPane tabbedPane;
    private JSplitPane jSplitPane1;
    private HtmlViewPane helpTextPane;
    private HelpTree currentTree;
    private final Map<String, HelpTree> helpTreesMap;
    private final List<HelpTree> helpTrees;

    /**
     * Constructs the viewer panel with a single help resource set given the name of the {@link #DEFAULT_HELPSET}
     *
     * @param resourcesClass Class for accessing resources (through {@link Class#getResourceAsStream(java.lang.String) })
     * @param helpResourceBase specification of base package (accessible from resourcesClass) for help resources
     * @param indexXml xml that specifies the contents of the help resources
     * @throws IOException
     * @throws SAXException
     */
    public HelpViewerPanel(final Class resourcesClass, final String helpResourceBase, final String indexXml) throws IOException, SAXException {
	this(Collections.singletonList(new HelpResourceSet(DEFAULT_HELPSET, resourcesClass, helpResourceBase, indexXml)));
    }

    /**
     *
     * @param helpSets Help resource sets to include (each will create a tab with a tree). Should contain at least one item.
     * @throws IOException
     * @throws SAXException
     */
    public HelpViewerPanel(final List<HelpResourceSet> helpSets) throws IOException, SAXException {
	if (helpSets.size() < 1) {
	    throw new IllegalArgumentException("Should provide at least one help resource set");
	}

	initComponents();

	final HelpItemsParser parser = new HelpItemsParser();
	this.helpTrees = new ArrayList<HelpTree>(helpSets.size());
	this.helpTreesMap = new HashMap<String, HelpTree>(helpSets.size());
	for (HelpResourceSet helpSet : helpSets) {
	    HelpTree helpTree = createHelpTree(helpSet, parser);
	    helpTrees.add(helpTree);
	    helpTreesMap.put(helpSet.getName(), helpTree);
	}

	currentTree = helpTrees.get(0);
	initIndexPane();
    }

    private HelpTree createHelpTree(HelpResourceSet helpSet, final HelpItemsParser parser) throws SAXException, IOException {
	HelpTree helpTree = new HelpTree(helpSet);
	initHelpTreeComponents(helpTree);
	final InputStream helpStream = helpSet.getResourcesClass().getResourceAsStream(helpSet.getIndexXml());
	if (helpStream != null) {
	    try {
		helpTree.setHelpIndex(parser.parse(helpStream));
	    } finally {
		helpStream.close();
	    }
	} else {
	    HelpIndex helpIndex = new HelpIndex();
	    final HelpItem helpItem = new HelpItem();
	    helpItem.setName(widgets.getString("HELP CONTENTS NOT FOUND"));
	    helpIndex.addSubItem(helpItem);
	    helpTree.setHelpIndex(helpIndex);
	}
	helpTree.getIndexTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	helpTree.setRootContentsNode(new DefaultMutableTreeNode(widgets.getString("CONTENTS")));
	helpTree.setHelpTreeModel(new DefaultTreeModel(helpTree.getRootContentsNode(), true));
	helpTree.getIndexTree().setModel(helpTree.getHelpTreeModel());
	populateIndex(helpTree);
	return helpTree;
    }

    private HelpTree getHelpTree(String resourceSetName) {
	return helpTreesMap.get(resourceSetName);
    }

    /**
     *
     * @param helpPage page title <strong>without section number</strong> of the help page (file name will be looked up)
     */
    public void setCurrentPage(String helpSet, String helpPage) {
	final HelpItem helpFile = getHelpFileByName(getHelpTree(helpSet).getHelpIndex(), helpPage);
	if (helpFile != null) {
	    showHelpItem(getHelpTree(helpSet), helpFile.getFile());
	} else {
	    logger.error("Help page not found: {}", helpPage);
	}
    }

    private HelpItem getHelpFileByName(HelpIndex parentItem, String name) {
	for (HelpItem child : parentItem.getSubItems()) {
	    if (child.getName().replaceAll("^\\d+\\.", "").trim().equals(name)) {
		return child;
	    } else {
		HelpItem childResult = getHelpFileByName(child, name);
		if (childResult != null) {
		    return childResult;
		}
	    }
	}
	return null;
    }

    private void populateIndex(HelpTree tree) {
	final DefaultMutableTreeNode rootNode = tree.getRootContentsNode();
	for (HelpItem item : tree.getHelpIndex().getSubItems()) {
	    populateIndex(rootNode, item);
	}
	tree.getHelpTreeModel().reload(rootNode);
    }

    private void populateIndex(DefaultMutableTreeNode parent, HelpItem item) {
	DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode(item, item.getSubItems().size() > 0);
	//helpTreeModel.insertNodeInto(node, parent, parent.getChildCount());
	parent.add(itemNode);
	for (HelpItem subItem : item.getSubItems()) {
	    populateIndex(itemNode, subItem);
	}
    }

    private void initComponents() {
	helpTextPane = new HtmlViewPane();
	helpTextPane.addHyperlinkListener(new ArbilHyperlinkListener());
	helpTextPane.setMinimumSize(new Dimension(100, 100));

	jSplitPane1 = new javax.swing.JSplitPane();
	jSplitPane1.setDividerLocation(250);
	jSplitPane1.setRightComponent(helpTextPane.createScrollPane());

	setLayout(new java.awt.BorderLayout());
	add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }

    private void initHelpTreeComponents(HelpTree helpTree) {
	JTree indexTree = new javax.swing.JTree();
	indexTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
	    public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
		indexTreeValueChanged();
	    }
	});
	helpTree.setIndexTree(indexTree);

	JScrollPane indexScrollPane = new JScrollPane();
	indexScrollPane.setViewportView(indexTree);
	helpTree.setIndexScrollPane(indexScrollPane);
    }

    /**
     * Initializes the index pane (split pane left component). Depending on the number of help resource sets, this is either a single
     * scrolling tree or a tab pane containing a number of trees.
     */
    private void initIndexPane() {
	if (helpTrees.size() > 1) {
	    // Create a panel with a tab for each tree
	    initHelpTreeTabs();
	} else {
	    // Current tree is the only tree
	    jSplitPane1.setLeftComponent(currentTree.getIndexScrollPane());
	    final HelpResourceSet currentResourceSet = currentTree.getHelpResourceSet();
	    helpTextPane.setDocumentBase(currentResourceSet.getResourcesClass(), currentResourceSet.getHelpResourceBase());
	}
    }

    private void initHelpTreeTabs() {
	//TODO: If only one helpTree, skip the tabs
	tabbedPane = new JTabbedPane();
	for (HelpTree tree : helpTrees) {
	    tabbedPane.add(tree.getHelpResourceSet().getName(), tree.getIndexScrollPane());
	}
	// Add a handler for when the selected tab changes
	tabbedPane.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		updateTabState();
	    }
	});
	jSplitPane1.setLeftComponent(tabbedPane);
	updateTabState();
    }

    private void updateTabState() {
	if (tabbedPane != null) {
	    currentTree = helpTrees.get(tabbedPane.getSelectedIndex());
	    final HelpResourceSet currentResourceSet = currentTree.getHelpResourceSet();
	    helpTextPane.setDocumentBase(currentResourceSet.getResourcesClass(), currentResourceSet.getHelpResourceBase());
	    if (currentTree.getIndexTree().getSelectionCount() == 0) {
		// Select first node so that there is a selection for this tree
		currentTree.getIndexTree().setSelectionRow(1);
	    } else {
		// Trigger update for existing selection of new current tree
		indexTreeValueChanged();
	    }
	}
    }

    private void indexTreeValueChanged() {
	DefaultMutableTreeNode node = (DefaultMutableTreeNode) currentTree.getIndexTree().getLastSelectedPathComponent();
	if (node != null) {
	    Object nodeInfo = node.getUserObject();
	    if (nodeInfo instanceof HelpItem) {
		showHelpItem(currentTree, ((HelpItem) nodeInfo).getFile());
	    }
	}
    }

    /**
     * Shows the help item; in this implementation the {@link #DEFAULT_HELPSET default help set} is used.
     * Other implementation may override this logic (e.g. determining the help set from the URL)
     *
     * @param itemURL URL of help item to show
     * @return Whether the help item was successfully retrieved and shown
     * @see #showHelpItem(java.lang.String, java.net.URL)
     */
    public boolean showHelpItem(URL itemURL) {
	return showHelpItem(DEFAULT_HELPSET, itemURL);
    }

    /**
     * Shows the help item from the specified help resource set
     *
     * @param helpSetName Name of the help set that contains the specified help item
     * @param itemURL URL of help item to show
     * @return Whether the help item was successfully retrieved and shown
     */
    protected final boolean showHelpItem(String helpSetName, URL itemURL) {
	final HelpTree helpTree = getHelpTree(helpSetName);
	final HelpResourceSet helpSet = helpTree.getHelpResourceSet();

	try {
	    final URI baseUri = helpSet.getResourcesClass().getResource(helpSet.getHelpResourceBase()).toURI();
	    if (itemURL.toString().startsWith(baseUri.toString())) {
		URI relativeURI = getRelativeURI(baseUri, itemURL.toURI());
		// Update index, which will show the help item if found.
		final String itemPath = relativeURI.getPath();
		if (itemPath != null) {
		    return updateIndex(helpTree, itemPath.toString(), relativeURI.getFragment());
		}
	    }
	} catch (URISyntaxException usEx) {
	    BugCatcherManager.getBugCatcher().logError(usEx);
	}
	return false;
    }

    private URI getRelativeURI(final URI baseUri, URI itemURI) throws URISyntaxException {
	if (baseUri.getScheme().equals("jar") && itemURI.getScheme().equals("jar")) {
	    URI baseInternalUri = new URI(baseUri.getSchemeSpecificPart());
	    URI itemInternalUri = new URI(itemURI.getSchemeSpecificPart());
	    return baseInternalUri.relativize(itemInternalUri);
	} else {
	    return baseUri.relativize(itemURI);
	}
    }

    private void showHelpItem(HelpTree helpTree, final String itemResource) {
	final HelpResourceSet helpSet = helpTree.getHelpResourceSet();
	final InputStream itemStream = helpSet.getResourcesClass().getResourceAsStream(helpSet.getHelpResourceBase() + itemResource);
	try {
	    helpTextPane.setContents(itemStream);
	} catch (IOException ioEx) {
	    helpTextPane.setText(widgets.getString("<P><STRONG>I/O EXCEPTION WHILE READING HELP CONTENTS</STRONG></P>"));
	    BugCatcherManager.getBugCatcher().logError(ioEx);
	}
	updateIndex(helpTree, itemResource);
    }

    private boolean updateIndex(final HelpTree tree, final String itemResource, final String fragment) {
	if (updateIndex(tree, itemResource)) {
	    if (fragment != null) {
		//TODO: Jump to fragment. The code below may not do this reliably
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			helpTextPane.scrollToReference(fragment);
		    }
		});
	    }
	    return true;
	}
	return false;
    }

    /**
     * Sets the selection of the index tree to the item that refers to the specified file
     *
     * @param itemResource file to select
     */
    private boolean updateIndex(final HelpTree tree, final String itemResource) {
	if (itemResource == null) {
	    return false;
	}

	// First check current selection
	DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) tree.getIndexTree().getSelectionPath().getLastPathComponent();
	if (lastPathComponent.getUserObject() instanceof HelpItem) {
	    final HelpItem helpItem = (HelpItem) lastPathComponent.getUserObject();
	    if (itemResource.equals(helpItem.getFile())) {
		// Selection is equal to specified item, do not change selection
		return true;
	    }
	}

	// Search node with specified resource
	DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getIndexTree().getModel().getRoot();
	DefaultMutableTreeNode selectionItem = findChild(root, itemResource);
	if (selectionItem != null) {
	    // Node found, set selection
	    final TreePath treePath = new TreePath(selectionItem.getPath());
	    tree.getIndexTree().setSelectionPath(treePath);
	    // And scroll to it
	    tree.getIndexTree().scrollPathToVisible(treePath);
	    return true;
	} else {
	    return false;
	}
    }

    private DefaultMutableTreeNode findChild(DefaultMutableTreeNode root, String itemResource) {
	for (int i = 0; i < root.getChildCount(); i++) {
	    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) root.getChildAt(i);
	    Object userObject = childNode.getUserObject();
	    if (userObject instanceof HelpItem) {
		HelpItem helpItem = (HelpItem) userObject;
		if (helpItem.getFile().equals(itemResource)) {
		    return childNode;
		}
	    }

	    DefaultMutableTreeNode findChild = findChild(childNode, itemResource);
	    if (findChild != null) {
		return findChild;
	    }
	}
	return null;
    }

    public static class HelpResourceSet {

	private final String name;
	private final Class resourcesClass;
	private final String helpResourceBase;
	private final String indexXml;

	public HelpResourceSet(String name, Class resourcesClass, String helpResourceBase, String indexXml) {
	    this.name = name;
	    this.resourcesClass = resourcesClass;
	    this.helpResourceBase = helpResourceBase;
	    this.indexXml = indexXml;
	}

	public String getName() {
	    return name;
	}

	public Class getResourcesClass() {
	    return resourcesClass;
	}

	public String getHelpResourceBase() {
	    return helpResourceBase;
	}

	public String getIndexXml() {
	    return indexXml;
	}
    }

    private static class HelpTree {

	private final HelpResourceSet helpResourceSet;
	private HelpIndex helpIndex;
	private javax.swing.JTree indexTree;
	private DefaultTreeModel helpTreeModel;
	private DefaultMutableTreeNode rootContentsNode;
	private javax.swing.JScrollPane indexScrollPane;

	public JScrollPane getIndexScrollPane() {
	    return indexScrollPane;
	}

	public void setIndexScrollPane(JScrollPane indexScrollPane) {
	    this.indexScrollPane = indexScrollPane;
	}

	public DefaultTreeModel getHelpTreeModel() {
	    return helpTreeModel;
	}

	public void setHelpTreeModel(DefaultTreeModel helpTreeModel) {
	    this.helpTreeModel = helpTreeModel;
	}

	public DefaultMutableTreeNode getRootContentsNode() {
	    return rootContentsNode;
	}

	public void setRootContentsNode(DefaultMutableTreeNode rootContentsNode) {
	    this.rootContentsNode = rootContentsNode;
	}

	public HelpTree(HelpResourceSet helpResourceSet) {
	    this.helpResourceSet = helpResourceSet;
	}

	public HelpIndex getHelpIndex() {
	    return helpIndex;
	}

	public void setHelpIndex(HelpIndex helpIndex) {
	    this.helpIndex = helpIndex;
	}

	public JTree getIndexTree() {
	    return indexTree;
	}

	public void setIndexTree(JTree indexTree) {
	    this.indexTree = indexTree;
	}

	public HelpResourceSet getHelpResourceSet() {
	    return helpResourceSet;
	}
    }
}
