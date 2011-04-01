package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *  Document   : HidePane
 *  Created on : Mar 11, 2011, 9:03:55 AM
 *  Author     : Peter Withers
 */
public class HidePane extends JPanel {

    private Component contentComponent;
    private boolean hiddenState = true;
    private JButton removeButton;
    private int shownWidth = 300;
    private String openLabel;
    private String closeLabel;
    private boolean blockNextMouseUp = false;
    private String borderPosition;

    public HidePane(Component contentComponentLocal, String labelStringLocal, String borderPositionLocal) {
        borderPosition = borderPositionLocal;
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
        removeButton.addMouseMotionListener(new MouseMotionAdapter() {

            private int lastXpos;

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!hiddenState) {
                    if (borderPosition.equals(BorderLayout.LINE_END)) {
                        shownWidth = shownWidth - lastXpos + e.getX();
                    } else {
                        shownWidth = shownWidth - lastXpos - e.getX();
                    }
                    if (shownWidth < removeButton.getPreferredSize().width * 2) {
                        shownWidth = removeButton.getPreferredSize().width * 2;
                    } else if (shownWidth > HidePane.this.getParent().getWidth()) {
                        shownWidth = HidePane.this.getParent().getWidth() - removeButton.getPreferredSize().width;
                    }
                    HidePane.this.setPreferredSize(new Dimension(shownWidth, HidePane.this.getPreferredSize().height));
                    HidePane.this.revalidate();
                    HidePane.this.repaint();
                    blockNextMouseUp = true;
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                lastXpos = e.getX();
            }
        });
        removeButton.setToolTipText("hide/show");
        removeButton.setPreferredSize(new Dimension(24, 24));
        removeButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (blockNextMouseUp) {
                    blockNextMouseUp = false;
                } else {
                    toggleHiddenState();
                }
            }
        });
        this.add(removeButton, BorderLayout.CENTER);
    }

    public void toggleHiddenState() {
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

    public boolean isHidden() {
        return hiddenState;
    }
}
