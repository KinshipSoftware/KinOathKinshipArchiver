package nl.mpi.kinnate.pligins.metadatasearch.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.ArbilNodeSearchColumnComboBox;
import nl.mpi.arbil.ui.ArbilNodeSearchPanel;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.kinnate.entityindexer.QueryException;
import nl.mpi.kinnate.pligins.metadatasearch.db.ArbilDatabase;

/**
 * Document : SearchPanel
 * Created on : Jul 31, 2012, 6:34:07 PM
 * Author : Peter Withers
 */
public class SearchPanel extends JPanel implements ActionListener {

    final private ArbilDatabase arbilDatabase;
    final ArbilWindowManager arbilWindowManager;

    public SearchPanel() {
        arbilWindowManager = new ArbilWindowManager();
        arbilDatabase = new ArbilDatabase(new ArbilSessionStorage(), arbilWindowManager);
        this.setLayout(new BorderLayout());
        this.add(new ArbilNodeSearchPanel(null, null, new ArbilNode[0]), BorderLayout.CENTER);
        final JPanel lowerPanel = new JPanel();
        final JButton createButton = new JButton("create db");
        createButton.setActionCommand("create");
        final JButton optionsButton = new JButton("get options");
        optionsButton.setActionCommand("options");
        optionsButton.addActionListener(this);
        createButton.addActionListener(this);
        lowerPanel.add(createButton);
        lowerPanel.add(optionsButton);
        this.add(lowerPanel, BorderLayout.PAGE_END);
    }

    static public void main(String[] args) {
        ArbilNodeSearchColumnComboBox.setSessionStorage(new ArbilSessionStorage());
        JFrame jFrame = new JFrame("Search Panel Test");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setContentPane(new SearchPanel());
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
            arbilDatabase.getMetadataTypes(null);
            System.out.println("done");
        }
    }
}
