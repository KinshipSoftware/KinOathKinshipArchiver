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
package nl.mpi.kinnate.kintypestrings;

import java.net.URI;

/**
 * Document : ImportRequiredException
 * Created on : Apr 20, 2012, 11:29:39 AM
 * Author : Peter Withers
 */
public class ImportRequiredException extends Exception {

    String messageString;
    URI importURI;

    public ImportRequiredException(String messageString, URI importURI) {
        this.messageString = messageString;
        this.importURI = importURI;
    }

    public URI getImportURI() {
        return importURI;
    }

    public String getMessageString() {
        return messageString;
    }
}
