package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashSet;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import nl.mpi.kinnate.kindata.VisiblePanelSetting;

/**
 *  Document   : HidePane
 *  Created on : Mar 11, 2011, 9:03:55 AM
 *  Author     : Peter Withers
 */
public class HidePane extends JPanel {

    public enum HidePanePosition {

        left, right, top, bottom
    }
    private JTabbedPane tabbedPane = new JTabbedPane();
    private boolean hiddenState = true;
    private int lastSelectedTab = -1;
    private int defaultShownWidth = 300;
    private int shownWidth;
    private int hiddenWidth = 30;
    private HidePanePosition borderPosition;
    private boolean horizontalDivider;
    private int dragStartPosition = 0;
    private boolean lastWasDrag = false;
    private HashSet<VisiblePanelSetting> registeredPanelSettings;

    public HidePane(HidePanePosition borderPositionLocal, int startWidth) {
        this.setLayout(new BorderLayout());
        JPanel separatorBar = new JPanel();
        separatorBar.setPreferredSize(new Dimension(5, 5));
        separatorBar.setMaximumSize(new Dimension(5, 5));
        separatorBar.setMinimumSize(new Dimension(5, 5));
        registeredPanelSettings = new HashSet<VisiblePanelSetting>();
        shownWidth = startWidth;
        borderPosition = borderPositionLocal;
        horizontalDivider = (!borderPosition.equals(HidePanePosition.left) && !borderPosition.equals(HidePanePosition.right));
        switch (borderPosition) {
            case left:
//                separatorBar = new JSeparator(JSeparator.VERTICAL);
                separatorBar.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                this.add(separatorBar, BorderLayout.LINE_END);
                tabbedPane.setTabPlacement(javax.swing.JTabbedPane.TOP); // changed from RIGHT because only mac supports rotated tabs and rotated text is debatable usability wise anyway
                break;
            case right:
//                separatorBar = new JSeparator(JSeparator.VERTICAL);
                separatorBar.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                this.add(separatorBar, BorderLayout.LINE_START);
                tabbedPane.setTabPlacement(javax.swing.JTabbedPane.TOP); // changed from LEFT because only mac supports rotated tabs and rotated text is debatable usability wise anyway
                break;
            case top:
//                separatorBar = new JSeparator(JSeparator.HORIZONTAL);
                separatorBar.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                this.add(separatorBar, BorderLayout.PAGE_END);
                tabbedPane.setTabPlacement(javax.swing.JTabbedPane.TOP);
                break;
            case bottom:
            default:
//                separatorBar = new JSeparator(JSeparator.HORIZONTAL);
                separatorBar.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                this.add(separatorBar, BorderLayout.PAGE_START);
                tabbedPane.setTabPlacement(javax.swing.JTabbedPane.TOP);
                break;
        }
        separatorBar.setBackground(Color.LIGHT_GRAY);
        this.add(tabbedPane, BorderLayout.CENTER);
//        this.add(contentComponent, labelStringLocal);
        separatorBar.addMouseMotionListener(new MouseMotionAdapter() {

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
        separatorBar.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (!hiddenState && lastSelectedTab != tabbedPane.getSelectedIndex()) {
                    // skip hide action when the selected tab changes 
                    lastSelectedTab = tabbedPane.getSelectedIndex();
                    return;
                }
                lastSelectedTab = tabbedPane.getSelectedIndex();
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
                for (VisiblePanelSetting panelSetting : registeredPanelSettings) {
                    panelSetting.setPanelWidth(shownWidth);
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

    public void addTab(String tabString, Component tabComponent) {
        int insertIndex = 0;
        for (int tabCounter = 0; tabCounter < tabbedPane.getTabCount(); tabCounter++) {
            if (tabString.compareToIgnoreCase(tabbedPane.getTitleAt(tabCounter)) < 0) {
                break;
            }
            insertIndex++;
        }
        tabbedPane.insertTab(tabString, null, tabComponent, tabString, insertIndex);
    }

    public void addTab(VisiblePanelSetting panelSetting, String tabString, Component tabComponent) {
        addTab(tabString, tabComponent);
        shownWidth = panelSetting.getPanelWidth();
        hiddenState = false;
        if (horizontalDivider) {
            HidePane.this.setPreferredSize(new Dimension(HidePane.this.getPreferredSize().width, shownWidth));
        } else {
            HidePane.this.setPreferredSize(new Dimension(shownWidth, HidePane.this.getPreferredSize().height));
        }
        this.setVisible(true);
        HidePane.this.revalidate();
        HidePane.this.repaint();
        registeredPanelSettings.add(panelSetting);
    }

    @Override
    public Component[] getComponents() {
        return tabbedPane.getComponents();
    }

    public Component getSelectedComponent() {
        return tabbedPane.getSelectedComponent();
    }

    public void setSelectedComponent(Component component) {
        tabbedPane.setSelectedComponent(component);
    }

    public void removeTab(Component comp) {
        tabbedPane.remove(comp);
//        this.setVisible(tabbedPane.getComponentCount() > 0);
    }

    public void remove(VisiblePanelSetting panelSetting) {
        for (Component currentPanel : panelSetting.getTargetPanels()) {
            tabbedPane.remove(currentPanel);
        }
        this.setVisible(tabbedPane.getComponentCount() > 0);
        registeredPanelSettings.remove(panelSetting);
    }

    public void setHiddeState() {
        boolean showEditor = tabbedPane.getComponentCount() > 0;
        if (hiddenState == showEditor) {
            toggleHiddenState();
        }
        this.setVisible(showEditor);
    }

    private void toggleHiddenState() {
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
}
