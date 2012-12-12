/**
 * Copyright (C) 2012 The Language Archive
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
package nl.mpi.kinnate.data;

import javax.swing.ImageIcon;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ContainerNode;

/**
 * Document : FilteredNodeContainer
 * Created on : Apr 26, 2012, 4:39:32 PM
 * Author : Peter Withers
 */
public class FilteredNodeContainer extends ContainerNode {

    public FilteredNodeContainer(String labelString, ImageIcon imageIcon, KinTreeFilteredNode[] childNodes) {
        super(labelString, imageIcon, childNodes);
    }

    @Override
    public ArbilNode[] getChildArray() {
        final ArbilNode[] childArray = super.getChildArray();
        new Thread() {

            @Override
            public void run() {
                for (ArbilNode childNode : childArray) {
                    ((KinTreeFilteredNode) childNode).loadEntityIfNotLoaded();
                }
            }
        }.start();
        return childArray;
    }
}
