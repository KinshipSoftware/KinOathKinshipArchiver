package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *  Document   : HidePane
 *  Created on : Mar 11, 2011, 9:03:55 AM
 *  Author     : Peter Withers
 */
public class HidePane extends JPanel {

    Component contentComponent;
    boolean hiddenState = true;
    JButton removeButton;
    int shownWidth = 300;
    String openLabel;
    String closeLabel;

    public HidePane(Component contentComponentLocal, String labelStringLocal, final String borderPosition) {
        if (borderPosition.equals(BorderLayout.LINE_END)) {
            openLabel = ">"; //labelStringLocal;
            closeLabel = "<";
        } else {
            openLabel = "<"; //labelStringLocal;
            closeLabel = ">";
        }
        contentComponent = contentComponentLocal;
        this.setLayout(new BorderLayout());
        removeButton = new JButton(openLabel);
        removeButton.setToolTipText("hide/show");
        removeButton.setPreferredSize(new Dimension(24, 24));
        removeButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (!hiddenState) {
                    HidePane.this.remove(contentComponent);
                    removeButton.setText(openLabel);
                    HidePane.this.add(removeButton, BorderLayout.CENTER);
                    HidePane.this.setPreferredSize(new Dimension(removeButton.getPreferredSize().width, HidePane.this.getPreferredSize().height));
                } else {
                    HidePane.this.add(removeButton, borderPosition);
                    HidePane.this.add(contentComponent, BorderLayout.CENTER);
                    removeButton.setText(closeLabel);
                    HidePane.this.setPreferredSize(new Dimension(shownWidth, HidePane.this.getPreferredSize().height));
                }
                hiddenState = !hiddenState;
                HidePane.this.revalidate();
                HidePane.this.repaint();
            }
        });
        this.add(removeButton, BorderLayout.CENTER);
    }
}
