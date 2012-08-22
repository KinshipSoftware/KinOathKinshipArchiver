package nl.mpi.kinnate.plugins.metadatasearch.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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
        searchPathOptionBox.addActionListener(this);
        searchPathOptionBox.setActionCommand("path");
        criterionPanel.add(searchPathOptionBox);

        searchFieldOptionBox = new SearchOptionBox();
        searchFieldOptionBox.addActionListener(this);
        searchFieldOptionBox.setActionCommand("field");
        criterionPanel.add(searchFieldOptionBox);

        criterionOuterPanel.add(criterionPanel, BorderLayout.LINE_START);

        JTextField searchText = new JTextField();
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

        progressPanel.add(new JProgressBar(), BorderLayout.CENTER);

        JPanel searchButtonsPanel = new JPanel();
        searchButtonsPanel.add(new JButton("Search"));
        progressPanel.add(searchButtonsPanel, BorderLayout.LINE_END);

        centerPanel.add(progressPanel, BorderLayout.PAGE_START);
        centerPanel.add(new JTextArea(), BorderLayout.CENTER);

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
        if ("create".equals(e.getActionCommand())) {
            try {
                System.out.println("create db");
                arbilDatabase.createDatabase();
                System.out.println("done");
            } catch (QueryException exception) {
                arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Database Error");
            }
        } else if ("options".equals(e.getActionCommand())) {
            System.out.println("run query");
            MetadataFileType[] metadataPathTypes = arbilDatabase.getMetadataTypes(null);
            System.out.println("done");
            searchPathOptionBox.setTypes(metadataPathTypes);

            System.out.println("run query");
            MetadataFileType[] metadataFieldTypes = arbilDatabase.getFieldMetadataTypes(null);
            System.out.println("done");
            searchFieldOptionBox.setTypes(metadataFieldTypes);
        } else if ("fields".equals(e.getActionCommand())) {
        } else if ("paths".equals(e.getActionCommand())) {
        }
    }
}
