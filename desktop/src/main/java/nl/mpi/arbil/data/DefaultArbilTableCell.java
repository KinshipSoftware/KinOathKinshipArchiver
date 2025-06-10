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
package nl.mpi.arbil.data;

/**
 * Generic implementation for ArbilTableCell
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @see ArbilDataNodeTableCell
 * @see ArbilDataNodeArrayTableCell
 */
public class DefaultArbilTableCell<T> implements ArbilTableCell<T> {

    private T content;

    public DefaultArbilTableCell(T content) {
	this.content = content;
    }

    /**
     * @return the content
     */
    @Override
    public T getContent() {
	return content;
    }

    /**
     * @param content the content to set
     */
    @Override
    public void setContent(T content) {
	this.content = content;
    }

    @Override
    public String toString() {
	// Contents of types ArbilDataNode and ArbilDataNode[] should be in their respective implementations of ArbilTableCell
	if (content instanceof ArbilField[]) {
	    return "<multiple values>";
	} else if (content instanceof ArbilField && ((ArbilField) content).isRequiredField() && ((ArbilField) content).getFieldValue().length() == 0) {
	    //super.setForeground(Color.RED);
	    return "<required field>";
	} else if (content instanceof ArbilField && !((ArbilField) content).fieldValueValidates()) {
	    //super.setForeground(Color.RED);
	    return content.toString();
	} else {
	    return content.toString();
	}
    }
}
