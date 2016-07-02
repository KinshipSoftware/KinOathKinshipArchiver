/*
 * Copyright (C) 2016 The Language Archive, Max Planck Institute for Psycholinguistics
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
package nl.mpi.kinnate.svg;

import java.io.IOException;
import nl.mpi.kinnate.kindata.KinPoint;
import nl.mpi.kinnate.kindata.KinRectangle;

/**
 * @since Jun 17, 2016 18:29:13 PM (creation date)
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public interface KinDocument {

    public void readDocument(String uri, String templateXml) throws IOException;

    public void createDocument(String uri) throws IOException;

    public KinElement getElementById(String elementId);

    public KinElement getDocumentElement();

    public KinElement createElementNS(String namespaceURI, String qualifiedName) throws KinElementException;

    public KinElement createTextNode(String data);

    public void addEventListener(final KinElement targetNode);

    public KinPoint getPointOnDocument(final KinPoint screenLocation, KinElement targetGroupElement);

    public KinPoint getPointOnScreen(final KinPoint documentLocation, KinElement targetGroupElement);

    public KinRectangle getRectOnDocument(final KinRectangle screenRectangle, KinElement targetGroupElement);

    public KinRectangle getBoundingBox(KinElement selectedGroup);

    public float getDragScale(KinElement kinElement);

    public String getUUID();
}
