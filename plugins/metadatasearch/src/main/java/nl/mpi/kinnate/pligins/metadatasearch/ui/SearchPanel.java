package nl.mpi.kinnate.pligins.metadatasearch.ui;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.ArbilNodeSearchColumnComboBox;
import nl.mpi.arbil.ui.ArbilNodeSearchPanel;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;

/**
 * Document : SearchPanel
 * Created on : Jul 31, 2012, 6:34:07 PM
 * Author : Peter Withers
 */
public class SearchPanel extends JPanel {

    public SearchPanel() {
        this.setLayout(new BorderLayout());
        this.add(new ArbilNodeSearchPanel(null, null, new ArbilNode[0]), BorderLayout.CENTER);
    }

    static public void main(String[] args) {
        ArbilNodeSearchColumnComboBox.setSessionStorage(new ArbilSessionStorage());
        JFrame jFrame = new JFrame("Search Panel Test");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setContentPane(new SearchPanel());
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
