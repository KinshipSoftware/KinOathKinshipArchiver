package nl.mpi.kinnate.svg;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.entityindexer.IndexerParameters;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.kindata.RelationTypeDefinition.CurveLineOrientation;
import nl.mpi.kinnate.kindata.VisiblePanelSetting;
import nl.mpi.kinnate.kintypestrings.KinTermGroup;
import nl.mpi.kinnate.kintypestrings.KinType;
import nl.mpi.kinnate.ui.entityprofiles.ProfileRecord;
import nl.mpi.kinnate.uniqueidentifiers.IdentifierException;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

/**
 * Document : DataStoreSvg
 * Created on : Mar 10, 2011, 8:37:26 AM
 * Author : Peter Withers
 */
@XmlRootElement(name = "KinDiagramData", namespace = "http://mpi.nl/tla/kin")
public class DataStoreSvg {

    static protected String kinDataNameSpace = "kin";
    static protected String kinDataNameSpaceLocation = "http://mpi.nl/tla/kin";
//    @XmlElement(name = "EgoIdList", namespace = "http://mpi.nl/tla/kin")
//    @XmlElementWrapper(name = "kin:EgoIdList")
    @XmlElement(name = "EgoId", namespace = "http://mpi.nl/tla/kin")
    public HashSet<UniqueIdentifier> egoEntities = new HashSet<UniqueIdentifier>();
    @XmlElement(name = "RequiredEntities", namespace = "http://mpi.nl/tla/kin")
    public HashSet<UniqueIdentifier> requiredEntities = new HashSet<UniqueIdentifier>();
    @XmlElementWrapper(name = "RelationTypeDefinitions")
    @XmlElement(name = "RelationType", namespace = "http://mpi.nl/tla/kin")
    private RelationTypeDefinition[] relationTypeDefinitions = null;
    @XmlElementWrapper(name = "KinTypeDefinitions")
    @XmlElement(name = "KinType", namespace = "http://mpi.nl/tla/kin")
    protected KinType[] kinTypeDefinitions = null;
    @XmlElement(name = "KinTypeString", namespace = "http://mpi.nl/tla/kin")
    protected String[] kinTypeStrings = new String[]{};
    @XmlElement(name = "IndexParameters", namespace = "http://mpi.nl/tla/kin")
    protected IndexerParameters indexParameters;
    @XmlElement(name = "KinTermGroup", namespace = "http://mpi.nl/tla/kin")
    protected KinTermGroup[] kinTermGroups = new KinTermGroup[]{};
    @XmlElementWrapper(name = "SelectedProfiles")
    @XmlElement(name = "Profile", namespace = "http://mpi.nl/tla/kin")
    // these profiles are set here so that old format diagrams have something in the add menu
    public ProfileRecord[] selectedProfiles = ProfileRecord.getDefaultProfiles();
    @XmlElement(name = "ShowLabels", namespace = "http://mpi.nl/tla/kin")
    public boolean showLabels = true;
    @XmlElement(name = "ShowKinTypeLabels", namespace = "http://mpi.nl/tla/kin")
    public boolean showKinTypeLabels = false;
//    @XmlElement(name = "ShowKinTermLabels", namespace = "http://mpi.nl/tla/kin")
//    public boolean showKinTermLabels = false;
    @XmlElement(name = "ShowIdLabels", namespace = "http://mpi.nl/tla/kin")
    public boolean showIdLabels = false;
    @XmlElement(name = "ShowDateLabels", namespace = "http://mpi.nl/tla/kin")
    public boolean showDateLabels = false;
    @XmlElement(name = "ShowKinTermLines", namespace = "http://mpi.nl/tla/kin")
    public boolean showKinTermLines = true; // todo: assess if this is required otherwise remove
    @XmlElement(name = "SnapToGrid", namespace = "http://mpi.nl/tla/kin")
    public boolean snapToGrid = true;
    @XmlElement(name = "ShowSanguineLines", namespace = "http://mpi.nl/tla/kin")
    public boolean showSanguineLines = true;
    @XmlElement(name = "ShowArchiveLinks", namespace = "http://mpi.nl/tla/kin")
    public boolean showArchiveLinks = true;
//    @XmlElement(name = "ShowResourceLinks", namespace = "http://mpi.nl/tla/kin")
//    public boolean showResourceLinks = true;
    @XmlElement(name = "HighlightRelationLines", namespace = "http://mpi.nl/tla/kin")
    public boolean highlightRelationLines = true;
    @XmlElement(name = "ShowDiagramBorder", namespace = "http://mpi.nl/tla/kin")
    public boolean showDiagramBorder = true;
    // keeping the entity data in the svg allows fast response but also invites stale data and can make the svg quite large
    // refs: #1883 #1973
    @XmlTransient // @XmlElement(name = "EntityData", namespace = "http://mpi.nl/tla/kin")
    public GraphSorter graphData = null;

    public enum DiagramMode {

        FreeForm, KinTypeQuery, /* RequiredEntities, */ Undefined
    }
    @XmlElement(name = "DiagramMode", namespace = "http://mpi.nl/tla/kin")
    public DiagramMode diagramMode = DiagramMode.Undefined;
    @XmlElement(name = "DiagramPanel", namespace = "http://mpi.nl/tla/kin")
    private HashSet<VisiblePanelSetting> visiblePanels;
//    @XmlElement(name = "WindowX", namespace = "http://mpi.nl/tla/kin")
//    public int windowX = 800;
//    @XmlElement(name = "WindowY", namespace = "http://mpi.nl/tla/kin")
//    public int windowY = 600;

    public class GraphRelationData {

        public UniqueIdentifier egoNodeId;
        public UniqueIdentifier alterNodeId;
        public DataTypes.RelationType relationType;
        public CurveLineOrientation curveLineOrientation;
    }

    public DataStoreSvg() {
    }

    public void setDefaults() {
        // todo: it might be better not to add any kin group until the user explicitly adds one from the menu
        kinTermGroups = new KinTermGroup[]{}; //new KinTermGroup(0), new KinTermGroup(1)};
        indexParameters = new IndexerParameters();
    }

    @XmlTransient
    public RelationTypeDefinition[] getRelationTypeDefinitions() {
        if (relationTypeDefinitions != null) {
            return relationTypeDefinitions;
        } else {
            // make sure that we do not set RelationTypeDefinition unless the user has changed the default kin types, otherwise it will be stored in the svg
            return new DataTypes().getReferenceRelations();
        }
    }

    public void setRelationTypeDefinitions(RelationTypeDefinition[] relationTypeDefinitions) {
        this.relationTypeDefinitions = relationTypeDefinitions;
    }

    class RelationTypeHashKey {

        String displayName;
        String dataCategory;

        public RelationTypeHashKey(String displayName, String dataCategory) {
            this.displayName = displayName;
            this.dataCategory = dataCategory;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RelationTypeHashKey other = (RelationTypeHashKey) obj;
            if ((this.displayName == null) ? (other.displayName != null) : !this.displayName.equals(other.displayName)) {
                return false;
            }
            if ((this.dataCategory == null) ? (other.dataCategory != null) : !this.dataCategory.equals(other.dataCategory)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 53 * hash + (this.displayName != null ? this.displayName.hashCode() : 0);
            hash = 53 * hash + (this.dataCategory != null ? this.dataCategory.hashCode() : 0);
            return hash;
        }
    }

    public void addRelationTypeDefinition(RelationTypeDefinition relationTypeDefinitionToAdd) {
        HashMap<RelationTypeHashKey, RelationTypeDefinition> kinTypesList = new HashMap<RelationTypeHashKey, RelationTypeDefinition>();
        kinTypesList.put(new RelationTypeHashKey(relationTypeDefinitionToAdd.getDisplayName(), relationTypeDefinitionToAdd.getDataCategory()), relationTypeDefinitionToAdd);
        for (RelationTypeDefinition relationTypeDefinition : Arrays.asList(getRelationTypeDefinitions())) {
            final RelationTypeHashKey relationTypeHashKey = new RelationTypeHashKey(relationTypeDefinition.getDisplayName(), relationTypeDefinition.getDataCategory());
            final RelationTypeDefinition foundType = kinTypesList.get(relationTypeHashKey);
            if (foundType == null) {
                // add the type to the list
                kinTypesList.put(relationTypeHashKey, relationTypeDefinition);
            } else {
                // merge found type with the existing type
                HashSet<RelationType> relationTypes = new HashSet<RelationType>(Arrays.asList(foundType.getRelationType()));
                relationTypes.addAll(Arrays.asList(relationTypeDefinition.getRelationType()));
                // because added RelationTypeDefinition is added first, we will keep the properties of the existing RelationTypeDefinition here
                kinTypesList.put(relationTypeHashKey, new RelationTypeDefinition(relationTypeDefinition.getDisplayName(), relationTypeDefinition.getDataCategory(), relationTypes.toArray(new RelationType[]{}), relationTypeDefinition.getLineColour(), relationTypeDefinition.getLineWidth(), relationTypeDefinition.getLineDash(), relationTypeDefinition.getCurveLineOrientation()));
            }
        }
        this.relationTypeDefinitions = kinTypesList.values().toArray(new RelationTypeDefinition[]{});
    }

    @XmlTransient
    public KinType[] getKinTypeDefinitions() {
        KinType[] returnArray;
        if (kinTypeDefinitions != null) {
            returnArray = kinTypeDefinitions;
        } else {
            // make sure that we do not set kinTypeDefinitions unless the user has changed the default kin types, otherwise it will be stored in the svg
            returnArray = KinType.getReferenceKinTypes();
        }
        Collections.sort(Arrays.asList(returnArray));
        return returnArray;
    }

    public void setKinTypeDefinitions(KinType[] kinTypeDefinitions) {
        this.kinTypeDefinitions = kinTypeDefinitions;
    }

    public VisiblePanelSetting[] getVisiblePanels() {
        if (visiblePanels == null) {
            return null;
        }
        return visiblePanels.toArray(new VisiblePanelSetting[]{});
    }

    public void setPanelState(VisiblePanelSetting.PanelType panelType, int panelWidth, boolean panelVisible) {
        if (visiblePanels == null) {
            visiblePanels = new HashSet<VisiblePanelSetting>();
        }
        visiblePanels.add(new VisiblePanelSetting(panelType, panelVisible, panelWidth));
    }

    public GraphRelationData getEntitiesForRelations(Node relationGroup) throws IdentifierException {
        for (Node currentChild = relationGroup.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
            if ("RelationEntities".equals(currentChild.getLocalName())) {
                GraphRelationData graphRelationData = new GraphRelationData();
                graphRelationData.egoNodeId = new UniqueIdentifier(currentChild.getAttributes().getNamedItemNS(kinDataNameSpace, "ego").getNodeValue());
                graphRelationData.alterNodeId = new UniqueIdentifier(currentChild.getAttributes().getNamedItemNS(kinDataNameSpace, "alter").getNodeValue());
                graphRelationData.relationType = DataTypes.RelationType.valueOf(currentChild.getAttributes().getNamedItemNS(kinDataNameSpace, "relationType").getNodeValue());
                graphRelationData.curveLineOrientation = CurveLineOrientation.valueOf(currentChild.getAttributes().getNamedItemNS(kinDataNameSpace, "curveLineOrientation").getNodeValue());
                return graphRelationData;
            }
        }
        return null;
    }

    public void storeRelationParameters(SVGDocument doc, Element relationGroup, DataTypes.RelationType relationType, CurveLineOrientation curveLineOrientation, UniqueIdentifier egoEntity, UniqueIdentifier alterEntity) {
        Element dataRecordNode = doc.createElementNS(kinDataNameSpace, "kin:RelationEntities");
        dataRecordNode.setAttributeNS(kinDataNameSpace, "kin:relationType", relationType.name());
        dataRecordNode.setAttributeNS(kinDataNameSpace, "kin:ego", egoEntity.getAttributeIdentifier());
        dataRecordNode.setAttributeNS(kinDataNameSpace, "kin:alter", alterEntity.getAttributeIdentifier());
        dataRecordNode.setAttributeNS(kinDataNameSpace, "kin:curveLineOrientation", curveLineOrientation.name());
        relationGroup.appendChild(dataRecordNode);
    }

//    private void storeParameter(SVGDocument doc, Element dataStoreElement, String parameterName, String[] ParameterValues) {
//        for (String currentKinType : ParameterValues) {
//            Element dataRecordNode = doc.createElementNS(kinDataNameSpace, "kin:" + parameterName);
//            //            Element dataRecordNode = doc.createElement(kinDataNameSpace + ":" + parameterName);
//            dataRecordNode.setAttributeNS(kinDataNameSpace, "value", currentKinType);
//            dataStoreElement.appendChild(dataRecordNode);
//        }
//    }
//    private void storeParameter(SVGDocument doc, Element dataStoreElement, String parameterName, String[][] ParameterValues) {
//        for (String[] currentKinType : ParameterValues) {
//            Element dataRecordNode = doc.createElementNS(kinDataNameSpace, "kin:" + parameterName);
//            //            Element dataRecordNode = doc.createElement(kinDataNameSpace + ":" + parameterName);
//            if (currentKinType.length == 1) {
//                dataRecordNode.setAttributeNS(kinDataNameSpace, "value", currentKinType[0]);
//            } else if (currentKinType.length == 2) {
//                dataRecordNode.setAttributeNS(kinDataNameSpace, "path", currentKinType[0]);
//                dataRecordNode.setAttributeNS(kinDataNameSpace, "value", currentKinType[1]);
//            } else {
//                // todo: add any other datatypes if required
//                throw new UnsupportedOperationException();
//            }
//            dataStoreElement.appendChild(dataRecordNode);
//        }
//    }
    protected void storeAllData(SVGDocument doc) {
        // remove old kin diagram data
        System.out.println("Removing old KinDiagramData nodes from the SVG before updating");
        NodeList dataNodes = doc.getElementsByTagNameNS("http://mpi.nl/tla/kin", "KinDiagramData");
        while (dataNodes.getLength() > 0) {
            dataNodes.item(0).getParentNode().removeChild(dataNodes.item(0));
        }
        // create string array to store the selected ego nodes in the dom
//        ArrayList<String> egoStringArray = new ArrayList<String>();
//        for (URI currentEgoUri : egoPathSet) {
//            egoStringArray.add(currentEgoUri.toASCIIString());
//        }
        // store the selected kin type strings and other data in the dom
        //        Namespace sNS = Namespace.getNamespace("someNS", "someNamespace");
        //        Element element = new Element("SomeElement", sNS);
//        Element kinTypesRecordNode = doc.createElementNS(kinDataNameSpaceLocation, "kin:KinDiagramData");
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DataStoreSvg.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(this, doc.getRootElement());
        } catch (JAXBException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
//        } catch (BaseXException exception) {
//            new ArbilBugCatcher().logError(exception);
        }


//        Element kinTypesRecordNode = doc.createElement(kinDataNameSpace + ":KinDiagramData");
//        kinTypesRecordNode.setAttribute("xmlns:" + kinDataNameSpace, kinDataNameSpaceLocation); // todo: this surely is not the only nor the best way to st the namespace
//        storeParameter(doc, kinTypesRecordNode, "EgoPathList", egoStringArray.toArray(new String[]{}));
//        storeParameter(doc, kinTypesRecordNode, "EgoIdList", egoIdentifierSet.toArray(new String[]{}));
//        storeParameter(doc, kinTypesRecordNode, "KinTypeStrings", kinTypeStrings);
//        storeParameter(doc, kinTypesRecordNode, "AncestorFields", indexParameters.ancestorFields.getValues());
//        storeParameter(doc, kinTypesRecordNode, "DecendantFields", indexParameters.decendantFields.getValues());
//        storeParameter(doc, kinTypesRecordNode, "LabelFields", indexParameters.labelFields.getValues());
//        storeParameter(doc, kinTypesRecordNode, "SymbolFieldsFields", indexParameters.symbolFieldsFields.getValues());
//        doc.getRootElement().appendChild(kinTypesRecordNode);
        // end store the selected kin type strings and other data in the dom
    }

//    private String[] getSingleParametersFromDom(SVGDocument doc, String parameterName) {
//        ArrayList<String> parameterList = new ArrayList<String>();
//        if (doc != null) {
//            //            printNodeNames(doc);
//            try {
//                // todo: resolve names space issue
//                // todo: try setting the XPath namespaces
//                NodeList parameterNodeList = org.apache.xpath.XPathAPI.selectNodeList(doc, "/svg:svg/kin:KinDiagramData/kin:" + parameterName);
//                for (int nodeCounter = 0; nodeCounter < parameterNodeList.getLength(); nodeCounter++) {
//                    Node parameterNode = parameterNodeList.item(nodeCounter);
//                    if (parameterNode != null) {
//                        parameterList.add(parameterNode.getAttributes().getNamedItem("value").getNodeValue());
//                    }
//                }
//            } catch (TransformerException transformerException) {
//                new ArbilBugCatcher().logError(transformerException);
//            }
// done: populate the avaiable symbols indexParameters.symbolFieldsFields.setAvailableValues(new String[]{"circle", "triangle", "square", "union"});
//        }
//        return parameterList.toArray(new String[]{});
//    }
//    private String[][] getDoubleParametersFromDom(SVGDocument doc, String parameterName) {
//        ArrayList<String[]> parameterList = new ArrayList<String[]>();
//        if (doc != null) {
//            try {
//                // todo: resolve names space issue
//                NodeList parameterNodeList = org.apache.xpath.XPathAPI.selectNodeList(doc, "/svg/KinDiagramData/" + parameterName);
//                for (int nodeCounter = 0; nodeCounter < parameterNodeList.getLength(); nodeCounter++) {
//                    Node parameterNode = parameterNodeList.item(nodeCounter);
//                    if (parameterNode != null) {
//                        parameterList.add(new String[]{parameterNode.getAttributes().getNamedItem("path").getNodeValue(), parameterNode.getAttributes().getNamedItem("value").getNodeValue()});
//                    }
//                }
//            } catch (TransformerException transformerException) {
//                new ArbilBugCatcher().logError(transformerException);
//            }
//            //            // todo: populate the avaiable symbols indexParameters.symbolFieldsFields.setAvailableValues(new String[]{"circle", "triangle", "square", "union"});
//        }
//        return parameterList.toArray(new String[][]{});
//    }
    static protected DataStoreSvg loadDataFromSvg(SVGDocument doc) {
        DataStoreSvg dataStoreSvg = new DataStoreSvg();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DataStoreSvg.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            NodeList dataStoreNodeList = doc.getElementsByTagNameNS("http://mpi.nl/tla/kin", "KinDiagramData");
            if (dataStoreNodeList.getLength() > 0) {
                dataStoreSvg = (DataStoreSvg) unmarshaller.unmarshal(dataStoreNodeList.item(0), DataStoreSvg.class).getValue();
            }
            // todo: Ticket #1105 When a document is reloaded there are some issues linking entities until the diagram is redrawn.
//            // set the alter entity for each relation if not already set (based on the known unique identifier)
//            for (EntityData entityData : dataStoreSvg.graphData.getDataNodes()) {
//                for (EntityRelation nodeRelation : entityData.getRelatedNodesToBeLoaded()) {
//                    for (EntityData alterData : dataStoreSvg.graphData.getDataNodes()) {
//                        if (nodeRelation.alterUniqueIdentifier.equals(alterData.getUniqueIdentifier())) {
//                            nodeRelation.setAlterNode(alterData);
//                        }
//                    }
//                }
//            }
        } catch (JAXBException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        }
        return dataStoreSvg;
    }
}
