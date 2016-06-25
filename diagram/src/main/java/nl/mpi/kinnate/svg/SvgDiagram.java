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

import java.io.IOException;
import nl.mpi.kinnate.kindata.GraphSorter;

/**
 * @since Nov 9, 2014 2:51:21 PM (creation date)
 * @author Peter Withers
 */
public class SvgDiagram extends KinDiagram {

    final DiagramSettings diagramSettings;
    final public EntitySvg entitySvg;

    protected String svgNameSpace = "http://www.w3.org/2000/svg";

    static public String kinDataNameSpace = "kin";
    static public String kinDataNameSpaceLocation = "http://mpi.nl/tla/kin";

    protected KinDocument doc;
    public GraphPanelSize graphPanelSize;
    public GraphSorter graphData; // this is tested for null to determine if the diagram has been recalculated 

    public SvgDiagram(DiagramSettings diagramSettings, EntitySvg entitySvg) {
        this.diagramSettings = diagramSettings;
        this.entitySvg = entitySvg;
        graphPanelSize = new GraphPanelSize();
    }

    public KinDocument getDoc() {
        return doc;
    }

    public DiagramSettings getDiagramSettings() {
        return diagramSettings;
    }

    public void readSvg(String svgFilePath) throws IOException, KinElementException {
        KinDocumentImpl tempDoc = new KinDocumentImpl();
        tempDoc.createDocument(svgFilePath); //(SVGDocument) documentFactory.createDocument(svgFilePath.toString());
        doc = tempDoc;
        entitySvg.readEntityPositions(doc.getElementById("EntityGroup"));
        entitySvg.readEntityPositions(doc.getElementById("LabelsGroup"));
        entitySvg.readEntityPositions(doc.getElementById("GraphicsGroup"));
        configureDiagramGroups();
    }

    private void configureDiagramGroups() throws KinElementException {
        KinElement svgRoot = doc.getDocumentElement();
        // make sure the diagram group exisits
        KinElement diagramGroup = doc.getElementById("DiagramGroup");
        if (diagramGroup == null) {
            diagramGroup = doc.createElementNS(svgNameSpace, "g");
            diagramGroup.setAttribute("id", "DiagramGroup");
            // add the diagram group to the root element (the 'svg' element)
            svgRoot.appendChild(diagramGroup);
        }
        KinElement previousElement = new KinElementImpl(null);
        // add the graphics group below the entities and relations
        // add the relation symbols in a group below the relation lines
        // add the entity symbols in a group on top of the relation lines
        // add the labels group on top, also added on svg load if missing
        for (String groupForMouseListener : new String[]{"LabelsGroup", "EntityGroup", "RelationGroup", "GraphicsGroup"}) {
            // add any groups that are required and add them in the required order
            KinElement parentElement = doc.getElementById(groupForMouseListener);
            if (parentElement == null) {
                parentElement = doc.createElementNS(svgNameSpace, "g");
                parentElement.setAttribute("id", groupForMouseListener);
                diagramGroup.insertBefore(parentElement, previousElement);
            } else {
                diagramGroup.insertBefore(parentElement, previousElement); // insert the node to make sure that it is in the diagram group and not in any other location
                // set up the mouse listeners that were lost in the save/re-open process
                if (!groupForMouseListener.equals("RelationGroup")) {
                    // do not add mouse listeners to the relation group
                    KinElement currentNode = parentElement.getFirstChild();
                    while (currentNode != null) {
                        doc.addEventListener(currentNode);
                        currentNode = currentNode.getNextSibling();
                    }
                }
            }
            previousElement = parentElement;
        }
    }

    public void generateDefaultSvg(GraphSorter graphSorter) throws IOException, KinElementException {
//        try {
        // set up a kinnate namespace so that the ego list and kin type strings can have more permanent storage places
        // in order to add the extra namespaces to the svg document we use a string and parse it
        // other methods have been tried but this is the most readable and the only one that actually works
        // I think this is mainly due to the way the svg dom would otherwise be constructed
        // others include:
        // doc.getDomConfig()
        // doc.getDocumentElement().setAttributeNS(DataStoreSvg.kinDataNameSpaceLocation, "kin:version", "");
        // doc.getDocumentElement().setAttribute("xmlns:" + DataStoreSvg.kinDataNameSpace, DataStoreSvg.kinDataNameSpaceLocation); // this method of declaring multiple namespaces looks to me to be wrong but it is the only method that does not get stripped out by the transformer on save
        //        Document doc = impl.createDocument(svgNS, "svg", null);
        //        SVGDocument doc = svgCanvas.getSVGDocument();
        String templateXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<svg xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:kin=\"http://mpi.nl/tla/kin\" "
                + "xmlns=\"http://www.w3.org/2000/svg\" contentScriptType=\"text/ecmascript\" "
                + " zoomAndPan=\"magnify\" contentStyleType=\"text/css\" "
                + "preserveAspectRatio=\"xMidYMid meet\" version=\"1.0\"/>";
        // DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        // doc = (SVGDocument) impl.createDocument(svgNameSpace, "svg", null);

        KinDocumentImpl tempDoc = new KinDocumentImpl();
        tempDoc.readDocument(svgNameSpace, templateXml);
        doc = tempDoc;

        entitySvg.updateSymbolsElement(doc, svgNameSpace);
        configureDiagramGroups();
        graphData = graphSorter;
    }
}
