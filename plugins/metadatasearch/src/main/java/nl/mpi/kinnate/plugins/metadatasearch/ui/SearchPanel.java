package nl.mpi.kinnate.plugins.metadatasearch.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.plugin.PluginDialogHandler;
import nl.mpi.arbil.ui.ArbilNodeSearchColumnComboBox;
import nl.mpi.arbil.ui.ArbilNodeSearchPanel;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.entityindexer.QueryException;
import nl.mpi.kinnate.plugins.metadatasearch.db.ArbilDatabase;
import nl.mpi.kinnate.plugins.metadatasearch.db.ArbilDatabase.CriterionJoinType;
import nl.mpi.kinnate.plugins.metadatasearch.db.MetadataFileType;

/**
 * Document : SearchPanel Created on : Jul 31, 2012, 6:34:07 PM
 *
 * @author Peter Withers
 */
public class SearchPanel extends JPanel implements ActionListener {

    final private ArbilDatabase arbilDatabase;
    final PluginDialogHandler arbilWindowManager;
    final JProgressBar jProgressBar;
    final JTextArea resultsTextArea;
    final ArrayList<SearchCriterionPanel> criterionPanelArray;
    final JPanel criterionArrayPanel;
    final JComboBox criterionJoinComboBox;

    public SearchPanel() {
        arbilWindowManager = new ArbilWindowManager();
        arbilDatabase = new ArbilDatabase(new ArbilSessionStorage(), arbilWindowManager, BugCatcherManager.getBugCatcher());
        this.setLayout(new BorderLayout());
        this.add(new ArbilNodeSearchPanel(null, null, new ArbilNode[0]), BorderLayout.PAGE_END);
        criterionPanelArray = new ArrayList<SearchCriterionPanel>();

        criterionArrayPanel = new JPanel();
        criterionArrayPanel.setLayout(new BoxLayout(criterionArrayPanel, BoxLayout.PAGE_AXIS));

        this.add(criterionArrayPanel, BorderLayout.PAGE_START);
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
        criterionJoinComboBox = new JComboBox(CriterionJoinType.values());
        criterionJoinComboBox.setSelectedItem(CriterionJoinType.intersect);
        searchButtonsPanel.add(criterionJoinComboBox);
        final JButton searchButton = new JButton("Search");
        searchButton.setActionCommand("search");
        searchButton.addActionListener(this);
        searchButtonsPanel.add(searchButton);
        progressPanel.add(searchButtonsPanel, BorderLayout.LINE_END);

        centerPanel.add(progressPanel, BorderLayout.PAGE_START);
        resultsTextArea = new JTextArea();
        centerPanel.add(new JScrollPane(resultsTextArea), BorderLayout.CENTER);

        this.add(centerPanel, BorderLayout.CENTER);
    }

    static public void main(String[] args) {
        ArbilNodeSearchColumnComboBox.setSessionStorage(new ArbilSessionStorage());
        JFrame jFrame = new JFrame("Search Panel Test");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SearchPanel searchPanel = new SearchPanel();
        jFrame.setContentPane(searchPanel);
        jFrame.pack();
        jFrame.setVisible(true);
        new Thread(searchPanel.getRunnable("add", null)).start();
    }

    public void actionPerformed(ActionEvent e) {
        jProgressBar.setIndeterminate(true);
        final String actionCommand = e.getActionCommand();
        System.out.println(actionCommand);
        SearchCriterionPanel eventCriterionPanel = null;
        Object sourceObject = e.getSource();
//        if (sourceObject instanceof SearchOptionBox) {
            while (sourceObject != null) {
                sourceObject = ((Component) sourceObject).getParent();
                if (sourceObject instanceof SearchCriterionPanel) {
                    eventCriterionPanel = (SearchCriterionPanel) sourceObject;
                    break;
                }
            }
//        }
        new Thread(getRunnable(actionCommand, eventCriterionPanel)).start();
    }

    private Runnable getRunnable(final String actionCommand, final SearchCriterionPanel eventCriterionPanel) {
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
                } else if ("remove".equals(actionCommand)) {
                    criterionPanelArray.remove(eventCriterionPanel);
                    criterionArrayPanel.remove(eventCriterionPanel);
                    SearchPanel.this.revalidate();
                } else if ("add".equals(actionCommand)) {
                    System.out.println("run query");
                    MetadataFileType[] metadataPathTypes = arbilDatabase.getMetadataTypes(null);
                    System.out.println("done");
                    System.out.println("run query");
                    MetadataFileType[] metadataFieldTypes = arbilDatabase.getFieldMetadataTypes(null);
                    System.out.println("done");
                    // todo: store the types so a query is not required each time
                    final SearchCriterionPanel searchCriterionPanel = new SearchCriterionPanel(SearchPanel.this, metadataPathTypes, metadataFieldTypes, criterionPanelArray.size());
                    criterionPanelArray.add(searchCriterionPanel);
                    criterionArrayPanel.add(searchCriterionPanel);
                    SearchPanel.this.revalidate();
                } else if ("paths".equals(actionCommand)) {
                    System.out.println("run query");
                    MetadataFileType[] metadataFieldTypes = arbilDatabase.getFieldMetadataTypes(eventCriterionPanel.getMetadataFileType());
                    System.out.println("done");
                    eventCriterionPanel.setFieldOptions(metadataFieldTypes);
                } else if ("fields".equals(actionCommand)) {
                    // todo: get controlled vocabulary of field values for the search text area
                } else if ("search".equals(actionCommand)) {
                    System.out.println("run query");
                    MetadataFileType[] resultTypes = arbilDatabase.getSearchResultMetadataTypes(eventCriterionPanel.getMetadataFileType(), eventCriterionPanel.getMetadataFieldType(), eventCriterionPanel.getSearchNegator(), eventCriterionPanel.getSearchType(), eventCriterionPanel.getSearchText());
                    System.out.println("done");
                    StringBuilder stringBuilder = new StringBuilder();
                    if (resultTypes != null) {
                        for (MetadataFileType resultType : resultTypes) {
                            stringBuilder.append(resultType.getArbilPathString());
                            stringBuilder.append("\n");
                        }
                    }
                    resultsTextArea.setText(stringBuilder.toString());
                }
                jProgressBar.setIndeterminate(false);
            }
        };
    }
}
