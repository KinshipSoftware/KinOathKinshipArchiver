/*
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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

    private URI LinkUri;
    private String pidString;

    public ExternalLink() {
    }

    public ExternalLink(URI LinkUri, String pidString) {
        this.LinkUri = LinkUri;
        this.pidString = pidString;
    }

    public URI getLinkUri() {
        return LinkUri;
    }

    @XmlAttribute(name = "url")
    public void setLinkUri(URI LinkUri) {
        this.LinkUri = LinkUri;
    }

    public String getPidString() {
        return pidString;
    }

    @XmlAttribute(name = "pid")
    public void setPidString(String pidString) {
        this.pidString = pidString;
    }
}
