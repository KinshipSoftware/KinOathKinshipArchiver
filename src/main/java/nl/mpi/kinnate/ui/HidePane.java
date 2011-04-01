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
    private boolean horizontalDivider;

    public HidePane(Component contentComponentLocal, String labelStringLocal, String borderPositionLocal) {
        borderPosition = borderPositionLocal;
        horizontalDivider = (!borderPosition.equals(BorderLayout.LINE_END) && !borderPosition.equals(BorderLayout.LINE_START));
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
                if (hiddenState) {
                    // if the user drags when closed then open to allow the drag
                    HidePane.this.add(removeButton, borderPosition);
                    HidePane.this.add(contentComponent, BorderLayout.CENTER);
                    removeButton.setText(closeLabel);
                    hiddenState = !hiddenState;
                    shownWidth = 0;
                }
                if (!hiddenState) {
                    if (horizontalDivider) {
                        if (borderPosition.equals(BorderLayout.PAGE_END)) {
                            shownWidth = shownWidth - lastXpos + e.getY();
                        } else {
                            shownWidth = shownWidth - lastXpos - e.getY();
                        }
                        if (shownWidth < removeButton.getPreferredSize().height * 2) {
                            shownWidth = removeButton.getPreferredSize().height * 2;
                        } else if (shownWidth > HidePane.this.getParent().getHeight()) {
                            shownWidth = HidePane.this.getParent().getHeight() - removeButton.getPreferredSize().height;
                        }
                        HidePane.this.setPreferredSize(new Dimension(HidePane.this.getPreferredSize().width, shownWidth));
                    } else {
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
                    }
                    HidePane.this.revalidate();
                    HidePane.this.repaint();
                    blockNextMouseUp = true;
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (horizontalDivider) {
                    lastXpos = e.getY();
                } else {
                    lastXpos = e.getX();
                }
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
            if (horizontalDivider) {
                HidePane.this.setPreferredSize(new Dimension(HidePane.this.getPreferredSize().width, removeButton.getPreferredSize().height));
            } else {
                HidePane.this.setPreferredSize(new Dimension(removeButton.getPreferredSize().width, HidePane.this.getPreferredSize().height));
            }
        } else {
            HidePane.this.add(removeButton, borderPosition);
            HidePane.this.add(contentComponent, BorderLayout.CENTER);
            removeButton.setText(closeLabel);
            if (horizontalDivider) {
                HidePane.this.setPreferredSize(new Dimension(HidePane.this.getPreferredSize().width, shownWidth));
            } else {
                HidePane.this.setPreferredSize(new Dimension(shownWidth, HidePane.this.getPreferredSize().height));
            }
        }
        hiddenState = !hiddenState;
        HidePane.this.revalidate();
        HidePane.this.repaint();
    }

    public boolean isHidden() {
        return hiddenState;
    }
}
