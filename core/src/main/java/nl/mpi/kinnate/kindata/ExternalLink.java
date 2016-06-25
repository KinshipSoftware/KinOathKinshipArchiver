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
package nl.mpi.kinnate.kindata;

import java.net.URI;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created on : Jun 10, 2013, 2:30:23 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class ExternalLink {

    private String linkUriString;
    private String pidString;

    public ExternalLink() {
    }

    public ExternalLink(String linkUriString, String pidString) {
        this.linkUriString = linkUriString;
        this.pidString = pidString;
    }

    public String getLinkUri() {
        return linkUriString;
    }

    @XmlAttribute(name = "url")
    public void setLinkUri(String linkUriString) {
        this.linkUriString = linkUriString;
    }

    public String getPidString() {
        return pidString;
    }

    @XmlAttribute(name = "pid")
    public void setPidString(String pidString) {
        this.pidString = pidString;
    }
}
