package nl.mpi.kinnate.plugins.metadatasearch.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
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
    final JPanel lowerPanel;
    final JFrame jFrame;

    public SearchPanel(JFrame jFrame) {
        this.jFrame = jFrame;
        arbilWindowManager = new ArbilWindowManager();
        arbilDatabase = new ArbilDatabase(new ArbilSessionStorage(), arbilWindowManager, BugCatcherManager.getBugCatcher());
        this.setLayout(new BorderLayout());
        this.add(new ArbilNodeSearchPanel(null, null, new ArbilNode[0]), BorderLayout.PAGE_END);
        lowerPanel = new JPanel();
        lowerPanel.setLayout(new FlowLayout());
        final JButton createButton = new JButton("create db");
        createButton.setActionCommand("create");
        final JButton optionsButton = new JButton("get options");
        optionsButton.setActionCommand("options");
        optionsButton.addActionListener(this);
        createButton.addActionListener(this);
        lowerPanel.add(createButton);
        lowerPanel.add(optionsButton);
        this.add(lowerPanel, BorderLayout.CENTER);
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
            MetadataFileType[] metadataPathTypes = arbilDatabase.getPathMetadataTypes(null);
            System.out.println("done");
            addOptionBox(metadataPathTypes, "paths");

            System.out.println("run query");
            MetadataFileType[] metadataFieldTypes = arbilDatabase.getFieldMetadataTypes(null);
            System.out.println("done");
            addOptionBox(metadataFieldTypes, "fields");
        } else if ("fields".equals(e.getActionCommand())) {

        } else if ("paths".equals(e.getActionCommand())) {

        }
    }

    private void addOptionBox(MetadataFileType[] metadataFileTypes, final String options) {
        SearchOptionBox searchOptionBox = new SearchOptionBox(metadataFileTypes);
        searchOptionBox.addActionListener(this);
        searchOptionBox.setActionCommand(options);
        lowerPanel.add(searchOptionBox);
        this.revalidate();
//            jFrame.pack();
    }
}
