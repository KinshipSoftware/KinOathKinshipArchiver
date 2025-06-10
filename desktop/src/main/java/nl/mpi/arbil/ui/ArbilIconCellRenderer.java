/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.awt.BorderLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Document : ArbilIconCellRenderer
 * Panel that wraps the table cell renderer allowing an icon to be added at either end of the label while complying to EDT requirements
 * Created on : Aug 8, 2012, 11:53:10 AM
 * Author : Peter Withers
 */
public class ArbilIconCellRenderer extends JPanel {

    // the this class overrides any events that might cause issues in the parent table: 
    // validate, invalidate, revalidate, repaint, and firePropertyChange methods are no-ops and isOpaque is overriden
    final private JLabel cellRendererComponent;
    Icon leftIcon = null;
    Icon rightIcon = null;

    public ArbilIconCellRenderer(JLabel cellRendererComponent) {
        this.cellRendererComponent = cellRendererComponent;
        this.setLayout(new BorderLayout());
        this.add(cellRendererComponent, BorderLayout.CENTER);
        this.add(rightIconLabel, BorderLayout.LINE_END);
    }

    public void setIcons(Icon leftIcon, Icon rightIcon) {
        this.leftIcon = leftIcon;
        this.rightIcon = rightIcon;
        cellRendererComponent.setIcon(leftIcon);
        cellRendererComponent.setHorizontalTextPosition(SwingConstants.TRAILING);
        rightIconLabel.setIcon(rightIcon);
        rightIconLabel.setHorizontalTextPosition(SwingConstants.LEADING);
        rightIconLabel.setBackground(cellRendererComponent.getBackground());
    }

    public Icon getRightIcon() {
        return rightIcon;
    }

    @Override
    public void doLayout() {
        // When layout is done, check whether the icon should be shown (depending on current width)
        rightIconLabel.setVisible(rightIcon != null && getWidth() >= ArbilIconCellPanel.minWidthForIcon + rightIcon.getIconWidth());
        super.doLayout();
    }
    JLabel rightIconLabel = new JLabel() {

        @Override
        public void validate() {
        }

        @Override
        public void invalidate() {
        }

        @Override
        public void revalidate() {
        }

        @Override
        public void repaint() {
        }

        @Override
        public void repaint(long tm) {
        }

        @Override
        public void repaint(int x, int y, int width, int height) {
        }

        @Override
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        }

        @Override
        public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        }

        @Override
        public void firePropertyChange(String propertyName, char oldValue, char newValue) {
        }

        @Override
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        }

        @Override
        public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
        }

        @Override
        public void firePropertyChange(String propertyName, short oldValue, short newValue) {
        }

        @Override
        public void firePropertyChange(String propertyName, long oldValue, long newValue) {
        }

        @Override
        public void firePropertyChange(String propertyName, float oldValue, float newValue) {
        }

        @Override
        public void firePropertyChange(String propertyName, double oldValue, double newValue) {
        }

        @Override
        public boolean isOpaque() {
            return cellRendererComponent.isOpaque();
        }
    };

//    @Override
//    public void validate() {
//    }
    @Override
    public void invalidate() {
    }

    @Override
    public void revalidate() {
    }

    @Override
    public void repaint() {
    }

    @Override
    public void repaint(long tm) {
    }

    @Override
    public void repaint(int x, int y, int width, int height) {
    }

    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
    }

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {
    }

    @Override
    public boolean isOpaque() {
        return cellRendererComponent.isOpaque();
    }
}
