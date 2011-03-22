package nl.mpi.kinnate.svg;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.kinnate.entityindexer.IndexerParameters;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

/**
 *  Document   : DataStoreSvg
 *  Created on : Mar 10, 2011, 8:37:26 AM
 *  Author     : Peter Withers
 */
public class DataStoreSvg {

    private String kinDataNameSpace = "kin";
    private String kinDataNameSpaceLocation = "http://mpi.nl/tla/kin";
    protected HashSet<URI> egoSet = new HashSet<URI>(); // todo: separate this into manditory entities and ego set
    protected HashSet<String> egoIdentifierSet = new HashSet<String>(); // todo: add this to the stored data in the svg
    protected String[] kinTypeStrings = new String[]{};
    protected IndexerParameters indexParameters;

    public class GraphRelationData {

        public String egoNodeId;
        public String alterNodeId;
        public GraphDataNode.RelationType relationType;
        public GraphDataNode.RelationLineType relationLineType;
    }

    public DataStoreSvg() {
        indexParameters = new IndexerParameters();
    }

    public GraphRelationData getEntitiesForRelations(Node relationGroup) {
        for (Node currentChild = relationGroup.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
            if ("RelationEntities".equals(currentChild.getLocalName())) {
                GraphRelationData graphRelationData = new GraphRelationData();
                graphRelationData.egoNodeId = currentChild.getAttributes().getNamedItemNS(kinDataNameSpace, "ego").getNodeValue();
                graphRelationData.alterNodeId = currentChild.getAttributes().getNamedItemNS(kinDataNameSpace, "alter").getNodeValue();
                graphRelationData.relationType = GraphDataNode.RelationType.valueOf(currentChild.getAttributes().getNamedItemNS(kinDataNameSpace, "relationType").getNodeValue());
                graphRelationData.relationLineType = GraphDataNode.RelationLineType.valueOf(currentChild.getAttributes().getNamedItemNS(kinDataNameSpace, "lineType").getNodeValue());
                return graphRelationData;
            }
        }
        return null;
    }

    public void storeRelationParameters(SVGDocument doc, Element relationGroup, GraphDataNode.RelationType relationType, GraphDataNode.RelationLineType relationLineType, String egoEntity, String alterEntity) {
        Element dataRecordNode = doc.createElementNS(kinDataNameSpace, "kin:RelationEntities");
        dataRecordNode.setAttributeNS(kinDataNameSpace, "lineType", relationLineType.name());
        dataRecordNode.setAttributeNS(kinDataNameSpace, "relationType", relationType.name());
        dataRecordNode.setAttributeNS(kinDataNameSpace, "ego", egoEntity);
        dataRecordNode.setAttributeNS(kinDataNameSpace, "alter", alterEntity);
        relationGroup.appendChild(dataRecordNode);
    }

    private void storeParameter(SVGDocument doc, Element dataStoreElement, String parameterName, String[] ParameterValues) {
        for (String currentKinType : ParameterValues) {
            Element dataRecordNode = doc.createElementNS(kinDataNameSpace, "kin:" + parameterName);
            //            Element dataRecordNode = doc.createElement(kinDataNameSpace + ":" + parameterName);
            dataRecordNode.setAttributeNS(kinDataNameSpace, "value", currentKinType);
            dataStoreElement.appendChild(dataRecordNode);
        }
    }

    private void storeParameter(SVGDocument doc, Element dataStoreElement, String parameterName, String[][] ParameterValues) {
        for (String[] currentKinType : ParameterValues) {
            Element dataRecordNode = doc.createElementNS(kinDataNameSpace, "kin:" + parameterName);
            //            Element dataRecordNode = doc.createElement(kinDataNameSpace + ":" + parameterName);
            if (currentKinType.length == 1) {
                dataRecordNode.setAttributeNS(kinDataNameSpace, "value", currentKinType[0]);
            } else if (currentKinType.length == 2) {
                dataRecordNode.setAttributeNS(kinDataNameSpace, "path", currentKinType[0]);
                dataRecordNode.setAttributeNS(kinDataNameSpace, "value", currentKinType[1]);
            } else {
                // todo: add any other datatypes if required
                throw new UnsupportedOperationException();
            }
            dataStoreElement.appendChild(dataRecordNode);
        }
    }

    protected void storeAllData(SVGDocument doc) {
        // create string array to store the selected ego nodes in the dom
        ArrayList<String> egoStringArray = new ArrayList<String>();
        for (URI currentEgoUri : egoSet) {
            egoStringArray.add(currentEgoUri.toASCIIString());
        }
        // store the selected kin type strings and other data in the dom
        //        Namespace sNS = Namespace.getNamespace("someNS", "someNamespace");
        //        Element element = new Element("SomeElement", sNS);
        Element kinTypesRecordNode = doc.createElementNS(kinDataNameSpace, "kin:KinDiagramData");
        //        Element kinTypesRecordNode = doc.createElement(kinDataNameSpace + ":KinDiagramData");
        kinTypesRecordNode.setAttribute("xmlns:" + kinDataNameSpace, kinDataNameSpaceLocation); // todo: this surely is not the only nor the best way to st the namespace
        storeParameter(doc, kinTypesRecordNode, "EgoList", egoStringArray.toArray(new String[]{}));
        storeParameter(doc, kinTypesRecordNode, "KinTypeStrings", kinTypeStrings);
        storeParameter(doc, kinTypesRecordNode, "AncestorFields", indexParameters.ancestorFields.getValues());
        storeParameter(doc, kinTypesRecordNode, "DecendantFields", indexParameters.decendantFields.getValues());
        storeParameter(doc, kinTypesRecordNode, "LabelFields", indexParameters.labelFields.getValues());
        storeParameter(doc, kinTypesRecordNode, "SymbolFieldsFields", indexParameters.symbolFieldsFields.getValues());
        doc.getRootElement().appendChild(kinTypesRecordNode);
        // end store the selected kin type strings and other data in the dom
    }

    private String[] getSingleParametersFromDom(SVGDocument doc, String parameterName) {
        ArrayList<String> parameterList = new ArrayList<String>();
        if (doc != null) {
            //            printNodeNames(doc);
            try {
                // todo: resolve names space issue
                // todo: try setting the XPath namespaces
                NodeList parameterNodeList = org.apache.xpath.XPathAPI.selectNodeList(doc, "/svg:svg/kin:KinDiagramData/kin:" + parameterName);
                for (int nodeCounter = 0; nodeCounter < parameterNodeList.getLength(); nodeCounter++) {
                    Node parameterNode = parameterNodeList.item(nodeCounter);
                    if (parameterNode != null) {
                        parameterList.add(parameterNode.getAttributes().getNamedItem("value").getNodeValue());
                    }
                }
            } catch (TransformerException transformerException) {
                GuiHelper.linorgBugCatcher.logError(transformerException);
            }
            //            // todo: populate the avaiable symbols indexParameters.symbolFieldsFields.setAvailableValues(new String[]{"circle", "triangle", "square", "union"});
        }
        return parameterList.toArray(new String[]{});
    }

    private String[][] getDoubleParametersFromDom(SVGDocument doc, String parameterName) {
        ArrayList<String[]> parameterList = new ArrayList<String[]>();
        if (doc != null) {
            try {
                // todo: resolve names space issue
                NodeList parameterNodeList = org.apache.xpath.XPathAPI.selectNodeList(doc, "/svg/KinDiagramData/" + parameterName);
                for (int nodeCounter = 0; nodeCounter < parameterNodeList.getLength(); nodeCounter++) {
                    Node parameterNode = parameterNodeList.item(nodeCounter);
                    if (parameterNode != null) {
                        parameterList.add(new String[]{parameterNode.getAttributes().getNamedItem("path").getNodeValue(), parameterNode.getAttributes().getNamedItem("value").getNodeValue()});
                    }
                }
            } catch (TransformerException transformerException) {
                GuiHelper.linorgBugCatcher.logError(transformerException);
            }
            //            // todo: populate the avaiable symbols indexParameters.symbolFieldsFields.setAvailableValues(new String[]{"circle", "triangle", "square", "union"});
        }
        return parameterList.toArray(new String[][]{});
    }

    protected void loadDataFromSvg(SVGDocument doc) {
//        ArrayList<String> egoStringArray = new ArrayList<String>();
        egoSet.clear();
        for (String currentEgoString : getSingleParametersFromDom(doc, "EgoList")) {
            try {
                egoSet.add(new URI(currentEgoString));
            } catch (URISyntaxException urise) {
                GuiHelper.linorgBugCatcher.logError(urise);
            }
        }
        kinTypeStrings = getSingleParametersFromDom(doc, "KinTypeStrings");
        indexParameters.ancestorFields.setValues(getDoubleParametersFromDom(doc, "AncestorFields"));
        indexParameters.decendantFields.setValues(getDoubleParametersFromDom(doc, "DecendantFields"));
        indexParameters.labelFields.setValues(getDoubleParametersFromDom(doc, "LabelFields"));
        indexParameters.symbolFieldsFields.setValues(getDoubleParametersFromDom(doc, "SymbolFieldsFields"));
    }
}
