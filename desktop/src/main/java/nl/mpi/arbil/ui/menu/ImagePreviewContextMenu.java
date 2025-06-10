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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui.menu;

import javax.swing.JList;
import nl.mpi.arbil.data.ArbilDataNode;

/**
 *
 * @author twagoo
 */
public class ImagePreviewContextMenu extends ArbilContextMenu {

    public ImagePreviewContextMenu(JList imagePreview) {
        super();
        setInvoker(imagePreview);

        Object[] selectedObjects = imagePreview.getSelectedValues();
        selectedTreeNodes = new ArbilDataNode[selectedObjects.length];
        for (int objectCounter = 0; objectCounter < selectedObjects.length; objectCounter++) {
            selectedTreeNodes[objectCounter] = (ArbilDataNode) selectedObjects[objectCounter];
        }
        leadSelectedDataNode = (ArbilDataNode) imagePreview.getSelectedValue();
    }

    @Override
    protected void setUpMenu() {
        // Nothing to do
    }

    @Override
    protected void setAllInvisible() {
        // Nothing to do
    }
}
