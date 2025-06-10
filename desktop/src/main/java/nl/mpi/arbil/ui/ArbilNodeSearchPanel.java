/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.search.ArbilNodeSearchTerm;
import nl.mpi.arbil.search.ArbilSearch;
import nl.mpi.arbil.util.BugCatcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ArbilNodeSearchPanel Created on : Feb 17, 2009, 4:42:59 PM
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilNodeSearchPanel extends JPanel implements ArbilDataNodeContainer {

    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    private final static Logger logger = LoggerFactory.getLogger(ArbilNodeSearchPanel.class);
    private ArbilNodeSearchPanel thisPanel = this;
    private final JInternalFrame parentFrame;
    private final ArbilTableModel resultsTableModel;
    private final ArbilTable table;
    private final ArbilNode[] selectedNodes;
    private JPanel searchTermsPanel;
    private JPanel inputNodePanel;
    private JProgressBar searchProgressBar;
    private JButton searchButton;
    private JButton stopButton;
    private RemoteServerSearchTermPanel remoteServerSearchTerm = null;
    private ArbilSearch searchService;
    private Thread searchThread = null;

    public ArbilNodeSearchPanel(JInternalFrame parentFrame, ArbilTable table, ArbilNode[] selectedNodes) {
        this.parentFrame = parentFrame;
        this.table = table;
        this.resultsTableModel = table.getArbilTableModel();
        this.selectedNodes = selectedNodes.clone();

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        initNodePanel();
        add(inputNodePanel);

        initSearchTermsPanel();
        add(searchTermsPanel);

        JPanel buttonsProgressPanel = createButtonsProgressPanel();
        add(buttonsProgressPanel);

        hideFirstBooleanOption();
        parentFrame.pack();
    }

    private JPanel createButtonsProgressPanel() {
        JPanel buttonsProgressPanel = new JPanel();
        buttonsProgressPanel.setLayout(new BoxLayout(buttonsProgressPanel, BoxLayout.LINE_AXIS));

        JButton addButton = new JButton();
        addButton.setText("+");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    logger.debug("adding new term");
                    stopSearch();
                    getSearchTermsPanel().add(new ArbilNodeSearchTermPanel(thisPanel));
                    hideFirstBooleanOption();
//                searchTermsPanel.revalidate();
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        });
        buttonsProgressPanel.add(addButton);

        searchProgressBar = new JProgressBar();
        searchProgressBar.setString("");
        searchProgressBar.setStringPainted(true);
        buttonsProgressPanel.add(searchProgressBar);

        stopButton = new JButton();
        stopButton.setText(widgets.getString("SEARCH_STOP"));
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    stopSearch();
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        });
        stopButton.setEnabled(false);
        buttonsProgressPanel.add(stopButton);

        searchButton = new JButton();
        searchButton.setText(widgets.getString("SEARCH_SEARCH"));
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    startSearch();
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        });
        buttonsProgressPanel.add(searchButton);
        return buttonsProgressPanel;
    }

    private void initSearchTermsPanel() {
        searchTermsPanel = new JPanel();
        searchTermsPanel.setLayout(new BoxLayout(searchTermsPanel, BoxLayout.PAGE_AXIS));
        // check if this search includes remote nodes
        boolean remoteSearch = false;
        for (ArbilNode arbilDataNode : selectedNodes) {
            if (!arbilDataNode.isLocal()) {
                remoteSearch = true;
                break;
            }
        }
        if (remoteSearch) {
            remoteServerSearchTerm = new RemoteServerSearchTermPanel(this);
            this.add(remoteServerSearchTerm);
        }
        searchTermsPanel.add(new ArbilNodeSearchTermPanel(this));
    }

    private void initNodePanel() {
        inputNodePanel = new JPanel();
        inputNodePanel.setLayout(new java.awt.GridLayout());
        for (ArbilNode currentNode : selectedNodes) {
            JLabel currentLabel = new JLabel(currentNode.toString(), currentNode.getIcon(), JLabel.CENTER);
            inputNodePanel.add(currentLabel);
        }
    }

    private void hideFirstBooleanOption() {
        boolean firstTerm = true;
        for (Component currentTermComp : searchTermsPanel.getComponents()) {
            ((ArbilNodeSearchTermPanel) currentTermComp).setBooleanVisible(!firstTerm);
            firstTerm = false;
        }
        searchTermsPanel.revalidate();
    }

    public void stopSearch() {
        logger.debug("stop search");
        hideFirstBooleanOption();
        if (searchService != null) {
            searchService.stopSearch();
        }
    }

    public void waitForSearch() {
        synchronized (searchThreadLock) {
            while (searchThread != null && searchThread.isAlive()) {
                try {
                    searchThreadLock.wait(1000);
                } catch (InterruptedException ie) {
                    return;
                }
            }
        }
    }

    public synchronized void startSearch() {
        if (searchThread != null && searchThread.isAlive()) {
            stopSearch();
            waitForSearch();
        }
        logger.debug("start search");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                searchButton.setEnabled(false);
                stopButton.setEnabled(true);

                // clear previous results
                resultsTableModel.removeAllArbilDataNodeRows();

                //start search thread (on swing thread to make sure results table model was cleared first)
                searchThread = new Thread(new SearchThread(), "performSearch");
                searchThread.setPriority(Thread.NORM_PRIORITY - 1);
                searchThread.start();
            }
        });
    }

    /**
     * @return the searchTermsPanel
     */
    public JPanel getSearchTermsPanel() {
        return searchTermsPanel;
    }
    private final Object searchThreadLock = new Object();

    private class SearchThread implements Runnable, ArbilSearch.ArbilSearchListener {

        @Override
        public void run() {
            synchronized (searchThreadLock) {
                try {
                    initSearchService();
                    prepareUI();
                    populateSearchTerms();
                    saveColumnOptions();
                    executeSearch();
                    finishUI();
                } catch (InterruptedException exception) {
                    BugCatcherManager.getBugCatcher().logError(exception);
                } catch (InvocationTargetException exception) {
                    BugCatcherManager.getBugCatcher().logError(exception);
                }
                // add the results to the table
                resultsTableModel.addArbilDataNodes(Collections.enumeration(searchService.getFoundNodes()));
                for (ArbilNodeSearchTerm searchTerm : searchService.getNodeSearchTerms()) {
                    final String searchString = searchTerm.getSearchString();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            resultsTableModel.highlightMatchingText(searchString);
                        }
                    });
                }
                searchService = null;
                searchThreadLock.notifyAll();
            }
        }

        private void initSearchService() {
            ArrayList<ArbilNodeSearchTerm> searchTerms = new ArrayList<ArbilNodeSearchTerm>(getComponentCount());
            for (Component component : searchTermsPanel.getComponents()) {
                if (component instanceof ArbilNodeSearchTermPanel) {
                    searchTerms.add((ArbilNodeSearchTermPanel) component);
                }
            }
            searchService = new ArbilSearch(Arrays.asList(selectedNodes), searchTerms, remoteServerSearchTerm, resultsTableModel, ArbilNodeSearchPanel.this, this);
        }

        private void populateSearchTerms() throws InterruptedException, InvocationTargetException {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    for (Component currentTermComp : searchTermsPanel.getComponents()) {
                        ((ArbilNodeSearchTermPanel) currentTermComp).populateSearchTerm();
                    }
                }
            });
        }

        private void saveColumnOptions() throws InterruptedException, InvocationTargetException {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    ArrayList<String> columns = new ArrayList<String>(searchTermsPanel.getComponentCount());
                    for (Component currentTermComp : searchTermsPanel.getComponents()) {
                        ((ArbilNodeSearchTermPanel) currentTermComp).addCurrentSearchColumnOption();
                        columns.add(((ArbilNodeSearchTermPanel) currentTermComp).searchFieldName);
                    }
                    ArbilNodeSearchColumnComboBox.addOptions(columns);
                }
            });
        }

        private void executeSearch() {
            searchService.splitLocalRemote();

            if (remoteServerSearchTerm != null) {
                prepareRemoteSearch();
            }
            searchService.searchLocalNodes();

            if (searchService.isSearchStopped()) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        searchProgressBar.setString(widgets.getString("SEARCH CANCELED"));
                    }
                });
            } else {
                // collect the max nodes found only if the search completed
                searchService.setTotalNodesToSearch(searchService.getTotalSearched());
            }
        }

        private void prepareRemoteSearch() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    searchProgressBar.setIndeterminate(true);
                    searchProgressBar.setString(widgets.getString("SEARCH_CONNECTING TO SERVER"));
                }
            });

            searchService.fetchRemoteSearchResults();

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    searchProgressBar.setString("");
                    searchProgressBar.setIndeterminate(false);
                }
            });
        }

        private void finishUI() throws InterruptedException, InvocationTargetException {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    searchProgressBar.setIndeterminate(false);
                    searchProgressBar.setValue(0);
                    searchProgressBar.setMaximum(1000);
                    searchButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    table.setDeferColumnWidthUpdates(false);
                }
            });
        }

        private void prepareUI() throws InterruptedException, InvocationTargetException {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    searchProgressBar.setIndeterminate(true);
                    searchProgressBar.setMinimum(0);
                    searchProgressBar.setMaximum(searchService.getTotalNodesToSearch());
                    table.setColumnWidths();
                    table.setDeferColumnWidthUpdates(true);
                }
            });
        }

        /**
         * Implements ArbilSearchListener method, called for each element that
         * gets searched
         *
         * @param currentElement
         */
        public void searchProgress(Object currentElement) {
            if (currentElement instanceof ArbilNode) {
                // todo: indicate how many metadata files searched rather than sub nodes
                searchProgressBar.setString(MessageFormat.format(widgets.getString("SEARCHED: {0}/{1} FOUND: {2}"),
                        searchService.getTotalSearched(),
                        searchService.getTotalNodesToSearch(),
                        searchService.getFoundNodes().size()));
            }
            if (!parentFrame.isVisible() && searchService != null) {
                // in the case that the user has closed the search window we want to stop the searchThread
                searchService.stopSearch();
            }
        }
    }

    public boolean isFullyLoadedNodeRequired() {
        return true;
    }

    /**
     * Data node is to be removed from the container
     *
     * @param dataNode Data node that should be removed
     */
    public void dataNodeRemoved(ArbilNode dataNode) {
        // Nothing to do, but this is implements  ArbilDataNodeContainer
    }

    /**
     * Data node is clearing its icon
     *
     * @param dataNode Data node that is clearing its icon
     */
    public void dataNodeIconCleared(ArbilNode dataNode) {
        // Nothing to do
    }

    /**
     * A new child node has been added to the destination node
     *
     * @param destination Node to which a node has been added
     * @param newNode The newly added node
     */
    public void dataNodeChildAdded(ArbilNode destination, ArbilNode newNode) {
        // Nothing to do
    }
}
