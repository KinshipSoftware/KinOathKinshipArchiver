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

import nl.mpi.kinnate.kindata.GraphSorter;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.svg.SVGDocument;

/**
 * @since Nov 9, 2014 2:51:21 PM (creation date)
 * @author petwit
 */
public class SvgDiagram {

    final DiagramSettings diagramSettings;
    final public EntitySvg entitySvg;

    protected String svgNameSpace = SVGDOMImplementation.SVG_NAMESPACE_URI;

    static public String kinDataNameSpace = "kin";
    static public String kinDataNameSpaceLocation = "http://mpi.nl/tla/kin";

    protected SVGDocument doc;
    public GraphPanelSize graphPanelSize;
    public GraphSorter graphData; // this is tested for null to determine if the diagram has been recalculated 

    public SvgDiagram(DiagramSettings diagramSettings, EntitySvg entitySvg) {
        this.diagramSettings = diagramSettings;
        this.entitySvg = entitySvg;
        graphPanelSize = new GraphPanelSize();
    }

    public DiagramSettings getDiagramSettings() {
        return diagramSettings;
    }
}
