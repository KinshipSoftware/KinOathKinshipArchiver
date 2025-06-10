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
import java.awt.Component;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel that wraps any component and adds a specified icon
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilIconCellPanel extends JPanel {

    protected static int minWidthForIcon = 120;
    private Icon icon;
    private JLabel iconLabel;

    /**
     *
     * @param component Component to wrap
     * @param icon Icon to shown. It will be shown at the line end
     */
    public ArbilIconCellPanel(Component component, Icon icon) {
        this(component, icon, BorderLayout.LINE_END);
    }

    /**
     *
     * @param component Component to wrap
     * @param icon Icon to shown
     * @param iconLocation Location for the icon to appear relative to text. Should be a BorderLayout constant
     * 
     * @see javax.swing.BorderFactory
     */
    public ArbilIconCellPanel(Component component, Icon icon, String iconLocation) {
        super();
        
        setLayout(new BorderLayout());
        add(component, BorderLayout.CENTER);

        this.icon = icon;
        iconLabel = new JLabel(icon);
        add(iconLabel, iconLocation);
        setBackground(component.getBackground());
    }

    @Override
    public void doLayout() {
        // When layout is done, check whether the icon should be shown (depending on current width)
        iconLabel.setVisible(getWidth() >= minWidthForIcon + icon.getIconWidth());
        super.doLayout();
    }
    
    public void addIconMouseListener(MouseListener mouseListener){
	iconLabel.addMouseListener(mouseListener);
    }
    
    public void removeIconMouseListener(MouseListener mouseListener){
	iconLabel.removeMouseListener(mouseListener);
    }
}
