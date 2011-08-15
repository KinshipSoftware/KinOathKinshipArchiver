package nl.mpi.kinnate.ui;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JTabbedPane;

/**
 *  Document   : HidePane
 *  Created on : Mar 11, 2011, 9:03:55 AM
 *  Author     : Peter Withers
 */
public class HidePane extends JTabbedPane {

    public enum HidePanePosition {

        left, right, top, bottom
    }
    private boolean hiddenState = true;
    private int lastSelectedTab = -1;
    private int defaultShownWidth = 300;
    private int shownWidth;
    private int hiddenWidth = 30;
    private HidePanePosition borderPosition;
    private boolean horizontalDivider;
    private int dragStartPosition = 0;
    private boolean lastWasDrag = false;

    public HidePane(HidePanePosition borderPositionLocal, int startWidth) {
        shownWidth = startWidth;
        borderPosition = borderPositionLocal;
        horizontalDivider = (!borderPosition.equals(HidePanePosition.left) && !borderPosition.equals(HidePanePosition.right));
        switch (borderPosition) {
            case left:
                this.setTabPlacement(javax.swing.JTabbedPane.RIGHT);
                break;
            case right:
                this.setTabPlacement(javax.swing.JTabbedPane.LEFT);
                break;
            case top:
                this.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
                break;
            case bottom:
                this.setTabPlacement(javax.swing.JTabbedPane.TOP);
                break;
        }
//        this.add(contentComponent, labelStringLocal);
        this.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                // todo: check the max space and prevent oversizing
                lastWasDrag = true;
                if (hiddenState) {
                    hiddenState = false;
                    shownWidth = hiddenWidth;
                }
                switch (borderPosition) {
                    case left:
                        shownWidth = shownWidth - dragStartPosition + e.getXOnScreen();
                        dragStartPosition = e.getXOnScreen();
                        break;
                    case right:
                        shownWidth = shownWidth + dragStartPosition - e.getXOnScreen();
                        dragStartPosition = e.getXOnScreen();
                        break;
                    case top:
                        shownWidth = shownWidth - dragStartPosition + e.getYOnScreen();
                        dragStartPosition = e.getYOnScreen();
                        break;
                    case bottom:
                        shownWidth = shownWidth + dragStartPosition - e.getYOnScreen();
                        dragStartPosition = e.getYOnScreen();
                        break;
                }
                if (shownWidth < hiddenWidth) {
                    shownWidth = hiddenWidth;
                    hiddenState = true;
                }
                if (horizontalDivider) {
                    HidePane.this.setPreferredSize(new Dimension(HidePane.this.getPreferredSize().width, shownWidth));
                } else {
                    HidePane.this.setPreferredSize(new Dimension(shownWidth, HidePane.this.getPreferredSize().height));
                }


//                    if (horizontalDivider) {
//                        if (borderPosition.equals(BorderLayout.PAGE_END)) {
//                            shownWidth = shownWidth - lastXpos + e.getY();
//                        } else {
//                            shownWidth = shownWidth - lastXpos - e.getY();
//                        }
////                        if (shownWidth < removeButton.getPreferredSize().height * 2) {
////                            shownWidth = removeButton.getPreferredSize().height * 2;
////                        } else if (shownWidth > HidePane.this.getParent().getHeight()) {
////                            shownWidth = HidePane.this.getParent().getHeight() - removeButton.getPreferredSize().height;
////                        }
//                        HidePane.this.setPreferredSize(new Dimension(HidePane.this.getPreferredSize().width, shownWidth));
//                    } else {
//                        if (borderPosition.equals(BorderLayout.LINE_END)) {
//                            shownWidth = shownWidth - lastXpos + e.getX();
//                        } else {
//                            shownWidth = shownWidth - lastXpos - e.getX();
//                        }
////                        if (shownWidth < removeButton.getPreferredSize().width * 2) {
////                            shownWidth = removeButton.getPreferredSize().width * 2;
////                        } else if (shownWidth > HidePane.this.getParent().getWidth()) {
////                            shownWidth = HidePane.this.getParent().getWidth() - removeButton.getPreferredSize().width;
////                        }
//                        HidePane.this.setPreferredSize(new Dimension(shownWidth, HidePane.this.getPreferredSize().height));
//                    }
                HidePane.this.revalidate();
                HidePane.this.repaint();
            }
        });
        this.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (!hiddenState && lastSelectedTab != HidePane.this.getSelectedIndex()) {
                    // skip hide action when the selected tab changes 
                    lastSelectedTab = HidePane.this.getSelectedIndex();
                    return;
                }
                lastSelectedTab = HidePane.this.getSelectedIndex();
                if (!lastWasDrag) {
                    toggleHiddenState();
                } else if (shownWidth < hiddenWidth * 2) {
                    shownWidth = hiddenWidth;
                    hiddenState = true;
                    if (horizontalDivider) {
                        HidePane.this.setPreferredSize(new Dimension(HidePane.this.getPreferredSize().width, shownWidth));
                    } else {
                        HidePane.this.setPreferredSize(new Dimension(shownWidth, HidePane.this.getPreferredSize().height));
                    }
                    HidePane.this.revalidate();
                    HidePane.this.repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                lastWasDrag = false;
                if (horizontalDivider) {
                    dragStartPosition = e.getYOnScreen();
                } else {
                    dragStartPosition = e.getXOnScreen();
                }
                super.mousePressed(e);
            }
        });
        if (horizontalDivider) {
            HidePane.this.setPreferredSize(new Dimension(HidePane.this.getPreferredSize().width, hiddenWidth));
        } else {
            HidePane.this.setPreferredSize(new Dimension(hiddenWidth, HidePane.this.getPreferredSize().height));
        }
    }

    public void toggleHiddenState() {
        if (!hiddenState) {
            if (horizontalDivider) {
                HidePane.this.setPreferredSize(new Dimension(HidePane.this.getPreferredSize().width, hiddenWidth));
            } else {
                HidePane.this.setPreferredSize(new Dimension(hiddenWidth, HidePane.this.getPreferredSize().height));
            }
        } else {
            if (shownWidth < hiddenWidth * 2) {
                shownWidth = defaultShownWidth;
            }
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
