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
package nl.mpi.arbil.ui.favourites;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.favourites.ArbilFavourites;
import nl.mpi.arbil.favourites.FavouritesExporter;
import nl.mpi.arbil.favourites.FavouritesExporterImpl;
import nl.mpi.arbil.favourites.FavouritesImporterImpl;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.flap.plugin.PluginDialogHandler;

/**
 * GUI that shows all favourites provided by a {@link TreeHelper}, allows the user to select a subset of these and perform an export on it
 * or to re-import favourites from disk.
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class FavouritesImportExportGUI implements ArbilDataNodeContainer {

    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    // UI components
    private final JPanel panel;
    private final DefaultListModel nodesListModel;
    private final ListSelectionModel nodesListSelectionModel;
    // Controller actions
    private final Action importAction;
    private final Action exportAction;
    private final Action refreshAction = new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	    update();
	}
    };
    // Arbil services
    private final TreeHelper treeHelper;
    private ArbilDataNode[] allFavourites = {};

    public FavouritesImportExportGUI(PluginDialogHandler dialog, SessionStorage sessionStorage, TreeHelper treeHelper, ApplicationVersion appVersion) {
	this(new ImportAction(dialog, new FavouritesImporterImpl(ArbilFavourites.getSingleInstance())),
		new ExportAction(dialog, new FavouritesExporterImpl(sessionStorage, appVersion)), treeHelper);
    }

    /**
     *
     * @param importAction action that will be bound to the import button
     * @param exportAction action that will be bound to the export button
     * @param treeHelper tree helper from which the collection of all favourites will be retrieved
     */
    public FavouritesImportExportGUI(Action importAction, Action exportAction, TreeHelper treeHelper) {
	this.importAction = importAction;
	this.exportAction = exportAction;
	this.treeHelper = treeHelper;
	this.nodesListModel = new DefaultListModel();
	this.nodesListSelectionModel = new DefaultListSelectionModel();

	this.panel = createPanel();
	panel.addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentShown(ComponentEvent e) {
		update();
	    }
	});
	update();
    }

    private JPanel createPanel() {
	final JPanel importExportPanel = new JPanel();
	importExportPanel.setLayout(new BoxLayout(importExportPanel, BoxLayout.PAGE_AXIS));
	importExportPanel.setPreferredSize(new Dimension(600, 400));

	final JPanel importPanel = createImportPanel();
	importPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), widgets.getString("IMPORT FAVOURITES")));
	importExportPanel.add(importPanel);

	final JPanel exportPanel = createExportPanel();
	exportPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), widgets.getString("EXPORT FAVOURITES")));
	importExportPanel.add(exportPanel);

	return importExportPanel;
    }

    private JPanel createImportPanel() {
	final JPanel importPanel = new JPanel(new BorderLayout());

	final JTextArea importInstructions = new JTextArea(MessageFormat.format(
		widgets.getString("FAVOURITES_PRESS THE IMPORT BUTTON")
		+ widgets.getString("FAVOURITES_THE DIRECTORY SHOULD CONTAIN A FILE CALLED"),
		FavouritesExporter.FAVOURITES_LIST_FILE));
	importInstructions.setEditable(false);
	importInstructions.setLineWrap(true);
	importInstructions.setWrapStyleWord(true);
	importInstructions.setOpaque(false);
	importPanel.add(importInstructions, BorderLayout.NORTH);

	final JPanel buttonsPanel = new JPanel();
	buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
	buttonsPanel.add(Box.createHorizontalGlue()); // horizontal glue to align to right
	importPanel.add(buttonsPanel, BorderLayout.SOUTH);

	final JButton importButton = new ImportButton(importAction);
	importButton.setText(widgets.getString("IMPORT FROM DISK"));
	buttonsPanel.add(importButton);

	return importPanel;
    }

    private JPanel createExportPanel() {
	final JPanel exportPanel = new JPanel(new BorderLayout());

	final JTextArea exportInstructions = new JTextArea(widgets.getString("FAVOURITES_SELECT ONE OR MORE FAVOURITES THAT YOU WISH TO EXPORT"));
	exportInstructions.setEditable(false);
	exportInstructions.setLineWrap(true);
	exportInstructions.setWrapStyleWord(true);
	exportInstructions.setOpaque(false);
	exportPanel.add(exportInstructions, BorderLayout.NORTH);

	final JPanel buttonsPanel = new JPanel();
	buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
	buttonsPanel.add(Box.createHorizontalGlue()); // horizontal glue to align to right
	exportPanel.add(buttonsPanel, BorderLayout.SOUTH);

	final JButton refreshButton = new JButton(refreshAction);
	refreshButton.setText(widgets.getString("REFRESH"));
	buttonsPanel.add(refreshButton);

	final JButton exportButton = new ExportButton(exportAction);
	exportButton.setText(widgets.getString("EXPORT SELECTION"));
	buttonsPanel.add(exportButton);

	JList nodesList = new JList(nodesListModel);
	nodesList.setSelectionModel(nodesListSelectionModel);
	nodesList.setCellRenderer(new DefaultListCellRenderer() {
	    @Override
	    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		setIcon(((ArbilDataNode) value).getIcon());
		return this;
	    }
	});
	nodesList.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		exportButton.setEnabled(!nodesListSelectionModel.isSelectionEmpty());
	    }
	});
	nodesList.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	exportPanel.add(nodesList, BorderLayout.CENTER);
	return exportPanel;
    }

    public final void update() {
	getAllFavourites();
	updateFavouritesList();
	// Select all nodes. TODO: keep previous selection
	nodesListSelectionModel.addSelectionInterval(0, nodesListModel.getSize());
    }

    private void getAllFavourites() {
	// Unregister this as container from current set
	for (ArbilDataNode node : allFavourites) {
	    node.removeContainer(this);
	}
	// Renew set
	allFavourites = treeHelper.getFavouriteNodes();
	// Register this as container
	for (ArbilDataNode node : allFavourites) {
	    node.registerContainer(this);
	}
    }

    private void updateFavouritesList() {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		nodesListModel.removeAllElements();
		for (ArbilDataNode node : allFavourites) {
		    nodesListModel.addElement(node);
		}
	    }
	});
    }

    public JPanel getPanel() {
	return panel;
    }
    /*
     * ArbilDataNodeContainer methods
     */

    public void dataNodeRemoved(ArbilNode an) {
	update();
    }

    public void dataNodeIconCleared(final ArbilNode an) {
	// Something has changed, update
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		int index = nodesListModel.indexOf(an);
		if (index >= 0) {
		    nodesListModel.set(index, an);
		}
	    }
	});
    }

    public void dataNodeChildAdded(ArbilNode an, ArbilNode an1) {
	update();
    }

    public boolean isFullyLoadedNodeRequired() {
	return false;
    }

    public void showDialog(JFrame owner) {
	JDialog dialog = new JDialog(owner);
	dialog.add(getPanel());
	dialog.setSize(600, 400);
	dialog.setVisible(true);
    }

    /**
     * Extension of button that implements ExportUI so that the ExportAction controller class can get the actual favourites selection
     * through the button object.
     */
    private class ExportButton extends JButton implements ExportUI {

	public ExportButton(Action a) {
	    super(a);
	}

	public List<ArbilDataNode> getSelectedFavourites() {
	    // Get list model contents as data nodes array
	    final List<ArbilDataNode> selectedNodes = new ArrayList<ArbilDataNode>(nodesListModel.getSize());
	    for (int i = 0; i < nodesListModel.getSize(); i++) {
		if (nodesListSelectionModel.isSelectedIndex(i)) {
		    selectedNodes.add((ArbilDataNode) nodesListModel.elementAt(i));
		}
	    }
	    return selectedNodes;
	}
    }

    /**
     * Extension of button that implements ImportUI so that the ImportAction controller class can trigger a refresh after an import.
     */
    private class ImportButton extends JButton implements ImportUI {

	public ImportButton(Action a) {
	    super(a);
	}

	public void refresh() {
	    FavouritesImportExportGUI.this.update();
	}
    }
}
