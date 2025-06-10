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
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.favourites.ArbilFavourites;

/**
 * Document : FavouriteSelectBox
 * Created on : May 10, 2012, 5:28:11 PM
 * Author : Peter Withers
 */
public class FavouriteSelectBox extends JPanel {
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");

    final ArbilNode targetNode;
    final JCheckBox metadataFilePerResourceCheckBox;
    final JCheckBox copyDirectoryStructureCheckBox;
    final JList favouriteList;

    public FavouriteSelectBox(ArbilNode targetNode) {
        this.targetNode = targetNode;
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder(widgets.getString("SUITABLE FAVOURITES")));
        favouriteList = new JList(ArbilFavourites.getSingleInstance().listFavouritesFor(targetNode));
        favouriteList.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                final JLabel listCellRendererComponent = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String labelString = value.toString();
                if (labelString.length() > 50) {
                    labelString = labelString.substring(0, 48) + widgets.getString("ELIPSIS");
                }
                listCellRendererComponent.setText(labelString);
                listCellRendererComponent.setIcon(((ArbilDataNode) value).getIcon());
                return listCellRendererComponent;
            }
        });
        this.add(new JScrollPane(favouriteList), BorderLayout.CENTER);
//        this.add(new JLabel(targetNode.toString(), targetNode.getIcon(), JLabel.LEFT), BorderLayout.PAGE_END);
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
        metadataFilePerResourceCheckBox = new JCheckBox(widgets.getString("METADATA FOR EACH RESOURCE"), false);
        optionsPanel.add(metadataFilePerResourceCheckBox);
        copyDirectoryStructureCheckBox = new JCheckBox(widgets.getString("COPY DIRECTORY STRUCTURE"), false);
        optionsPanel.add(copyDirectoryStructureCheckBox);
        this.add(optionsPanel, BorderLayout.PAGE_END);
    }

    public ArbilDataNode getSelectedFavouriteNode() {
        return (ArbilDataNode) favouriteList.getSelectedValue();
    }

    public ArbilNode getTargetNode() {
        return targetNode;
    }

    public boolean getCopyDirectoryStructure() {
        return this.copyDirectoryStructureCheckBox.isSelected();
    }

    public boolean getMetadataFilePerResource() {
        return this.metadataFilePerResourceCheckBox.isSelected();
    }
}
