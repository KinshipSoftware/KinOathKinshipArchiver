package nl.mpi.kinnate.ui;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

/**
 * Document : StatusBar
 * Created on : Aug 9, 2012, 3:02:40 PM
 * Author : Peter Withers
 */
public class StatusBar extends JPanel {

    final private JLabel statusLabel;

    public StatusBar(String initialText) {
        this.setBorder(new BevelBorder(BevelBorder.LOWERED));
        this.setPreferredSize(new Dimension(this.getWidth(), 16));
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        statusLabel = new JLabel(initialText);
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        this.add(statusLabel);
    }

    public void setStatusBarText(final String statusText) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                statusLabel.setText(statusText);
            }
        });
    }
}
