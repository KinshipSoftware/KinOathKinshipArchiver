/*
 * Copyright (C) 2014 The Language Archive, Max Planck Institute for Psycholinguistics
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

import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import org.w3c.dom.svg.SVGDocument;

/**
 * @since Nov 9, 2014 8:36:05 AM (creation date)
 * @author petwit
 */
public interface DiagramSettings {

    String defaultSymbol();

    boolean showIdLabels();

    boolean showLabels();

    boolean showKinTypeLabels();

    boolean showDateLabels();

    boolean showExternalLinks();

    boolean highlightRelationLines();

    boolean snapToGrid();

    boolean showDiagramBorder();

    boolean showSanguineLines();

    boolean showKinTermLines();

    RelationTypeDefinition[] getRelationTypeDefinitions();

    void storeAllData(SVGDocument doc);
}
