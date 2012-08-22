package nl.mpi.kinnate.plugins.metadatasearch.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.plugin.PluginDialogHandler;
import nl.mpi.arbil.ui.ArbilNodeSearchColumnComboBox;
import nl.mpi.arbil.ui.ArbilNodeSearchPanel;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.entityindexer.QueryException;
import nl.mpi.kinnate.plugins.metadatasearch.db.ArbilDatabase;
import nl.mpi.kinnate.plugins.metadatasearch.db.MetadataFileType;

/**
 * Document : SearchPanel Created on : Jul 31, 2012, 6:34:07 PM
 *
 * @author Peter Withers
 */
public class SearchPanel extends JPanel implements ActionListener {

    final private ArbilDatabase arbilDatabase;
    final PluginDialogHandler arbilWindowManager;
    final JFrame jFrame;
    final SearchOptionBox searchPathOptionBox;
    final SearchOptionBox searchFieldOptionBox;
    final JComboBox searchNegatorOption;
    final JComboBox searchTypeOption;
    final JTextField searchText;
    final JProgressBar jProgressBar;
    final JTextArea resultsTextArea;

    public SearchPanel(JFrame jFrame) {
        this.jFrame = jFrame;
        arbilWindowManager = new ArbilWindowManager();
        arbilDatabase = new ArbilDatabase(new ArbilSessionStorage(), arbilWindowManager, BugCatcherManager.getBugCatcher());
        this.setLayout(new BorderLayout());
        this.add(new ArbilNodeSearchPanel(null, null, new ArbilNode[0]), BorderLayout.PAGE_END);
        final JPanel criterionPanel = new JPanel();
        criterionPanel.setLayout(new FlowLayout());

        final JPanel criterionOuterPanel = new JPanel(new BorderLayout());

        searchPathOptionBox = new SearchOptionBox();
        searchPathOptionBox.addItem("Loading");
        searchPathOptionBox.addActionListener(this);
        searchPathOptionBox.setActionCommand("paths");
        criterionPanel.add(searchPathOptionBox);

        searchFieldOptionBox = new SearchOptionBox();
        searchFieldOptionBox.addItem("Loading");
        searchFieldOptionBox.addActionListener(this);
        searchFieldOptionBox.setActionCommand("fields");
        criterionPanel.add(searchFieldOptionBox);

        searchNegatorOption = new JComboBox(ArbilDatabase.SearchNegator.values());
        criterionPanel.add(searchNegatorOption);

        searchTypeOption = new JComboBox(ArbilDatabase.SearchType.values());
        criterionPanel.add(searchTypeOption);

        criterionOuterPanel.add(criterionPanel, BorderLayout.LINE_START);

        searchText = new JTextField();
        criterionOuterPanel.add(searchText, BorderLayout.CENTER);

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
        resultsTextArea = new JTextArea();
        centerPanel.add(new JScrollPane(resultsTextArea), BorderLayout.CENTER);

        this.add(centerPanel, BorderLayout.CENTER);
    }

    static public void main(String[] args) {
        ArbilNodeSearchColumnComboBox.setSessionStorage(new ArbilSessionStorage());
        JFrame jFrame = new JFrame("Search Panel Test");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setContentPane(new SearchPanel(jFrame));
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
                final Object selectedTypeItem = searchPathOptionBox.getSelectedItem();
                MetadataFileType metadataFileType = null;
                if (selectedTypeItem instanceof MetadataFileType) {
                    metadataFileType = (MetadataFileType) selectedTypeItem;
                }
                final Object selectedFieldItem = searchFieldOptionBox.getSelectedItem();
                MetadataFileType metadataFieldType = null;
                if (selectedFieldItem instanceof MetadataFileType) {
                    metadataFieldType = (MetadataFileType) selectedFieldItem;
                }

                ArbilDatabase.SearchNegator searchNegator = ArbilDatabase.SearchNegator.values()[searchNegatorOption.getSelectedIndex()];

                ArbilDatabase.SearchType searchType = ArbilDatabase.SearchType.values()[searchTypeOption.getSelectedIndex()];

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
                    MetadataFileType[] metadataPathTypes = arbilDatabase.getMetadataTypes(null);
                    System.out.println("done");
                    searchPathOptionBox.setTypes(metadataPathTypes);

                    System.out.println("run query");
                    MetadataFileType[] metadataFieldTypes = arbilDatabase.getFieldMetadataTypes(null);
                    System.out.println("done");
                    searchFieldOptionBox.setTypes(metadataFieldTypes);
                } else if ("paths".equals(actionCommand)) {
                    System.out.println("run query");
                    MetadataFileType[] metadataFieldTypes = arbilDatabase.getFieldMetadataTypes(metadataFileType);
                    System.out.println("done");
                    searchFieldOptionBox.setTypes(metadataFieldTypes);
                } else if ("fields".equals(actionCommand)) {
                    // todo: get controlled vocabulary of field values for the search text area
                } else if ("search".equals(actionCommand)) {
                    System.out.println("run query");
                    MetadataFileType[] resultTypes = arbilDatabase.getSearchResultMetadataTypes(metadataFileType, metadataFieldType, searchNegator, searchType, searchText.getText());
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
