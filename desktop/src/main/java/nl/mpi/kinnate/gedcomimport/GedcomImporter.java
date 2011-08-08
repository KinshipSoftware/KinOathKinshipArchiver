package nl.mpi.kinnate.gedcomimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : GedcomImporter
 *  Created on : Aug 24, 2010, 2:40:21 PM
 *  Author     : Peter Withers
 */
public class GedcomImporter extends EntityImporter implements GenericImporter {

    public GedcomImporter(JProgressBar progressBarLocal, JTextArea importTextAreaLocal, boolean overwriteExistingLocal) {
        super(progressBarLocal, importTextAreaLocal, overwriteExistingLocal);
    }

    @Override
    public boolean canImport(String inputFileString) {
        return (inputFileString.toLowerCase().endsWith(".ged"));
    }

    @Override
    public URI[] importFile(InputStreamReader inputStreamReader) {
        ArrayList<URI> createdNodes = new ArrayList<URI>();
        createdNodeIds = new HashMap<String, ArrayList<UniqueIdentifier>>();
//        Hashtable<String, URI> createdNodesTable = new Hashtable<String, URI>();
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//        ArrayList<ImdiTreeObject> linkNodes = new ArrayList<ImdiTreeObject>();
        // really should close the file properly but this is only for testing at this stage

//        URI targetFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getCacheDirectory(), gedcomXsdLocation);
//        try {
//            targetFileURI = componentBuilder.createComponentFile(targetFileURI, this.getClass().getResource(gedcomXsdLocation).toURI(), false);
//        } catch (URISyntaxException ex) {
//            new ArbilBugCatcher().logError(ex);
//            return;
//        }

//        metadataBuilder.addChildNode(gedcomImdiObject, ".Gedcom.Relation", null, null, null);
//        gedcomImdiObject.loadImdiDom();
//        gedcomImdiObject.waitTillLoaded();

        try {
            String strLine;
            int gedcomLevel = 0;
            String xsdString = ""; // temp string to create the xsd
            ArrayList<String> xsdTagsDone = new ArrayList<String>(); // temp array to create the xsd
            ArrayList<String> gedcomLevelStrings = new ArrayList<String>();
            ArrayList<String> xsdLevelStrings = new ArrayList<String>(); // temp array to create the xsd

//            ImdiTreeObject gedcomImdiObject = null;
            File entityFile = null;
            EntityDocument currentEntity = null;
            ArrayList<String> metadataPath = new ArrayList<String>();
//            Element previousField = null;
//            Node currentDomNode = null;

            File destinationDirectory = super.getDestinationDirectory();

//            String gedcomPreviousPath = "";
            int currntLineCounter = 0;
            boolean skipFileEntity = false;
            while ((strLine = bufferedReader.readLine()) != null) {
                if (skipFileEntity) {
                    skipFileEntity = false;
                    while ((strLine = bufferedReader.readLine()) != null) {
                        if (strLine.startsWith("0")) {
                            break;
                        }
                    }
                }
                String[] lineParts = strLine.split(" ", 3);
                gedcomLevel = Integer.parseInt(lineParts[0]);
                while (gedcomLevelStrings.size() > gedcomLevel) {
                    gedcomLevelStrings.remove(gedcomLevelStrings.size() - 1);
                }
                while (xsdLevelStrings.size() > gedcomLevel) {
                    xsdLevelStrings.remove(xsdLevelStrings.size() - 1);
                    xsdString += "</xs:sequence>\n</xs:complexType>\n</xs:element>\n";
                    //currentDomNode = currentDomNode.getParentNode();
                }
                gedcomLevelStrings.add(lineParts[1]);
                System.out.println(strLine);
                System.out.println("gedcomLevelString: " + gedcomLevelStrings);
//                appendToTaskOutput(importTextArea, strLine);
                boolean lastFieldContinued = false;
                if (lineParts[1].equals("CONT")) {
                    // todo: if the previous field is null this should be caught and handled as an error in the source file
                    String lineContents = "";
                    if (lineParts.length > 2) {
                        lineContents = lineParts[2];
                    }
                    currentEntity.appendValueToLast("\n" + lineContents);
                    lastFieldContinued = true;
                } else if (lineParts[1].equals("CONC")) {
                    // todo: if the previous field is null this should be caught and handled as an error in the source file
                    String lineContents = "";
                    if (lineParts.length > 2) {
                        lineContents = lineParts[2];
                    }
                    currentEntity.appendValueToLast(lineContents);
                    lastFieldContinued = true;
                }
                if (lastFieldContinued == false) {
                    if (gedcomLevel == 0) {
//                        if (createdNodes.size() > 20) {
//                            appendToTaskOutput(importTextArea, "stopped import at node count: " + createdNodes.size());
//                            break;
//                        }
//                        if (metadataDom != null) {
//                            ArbilComponentBuilder.savePrettyFormatting(metadataDom, entityFile);
//                            metadataDom = null;
//                        }
                        if (lineParts[1].equals("TRLR")) {
                            appendToTaskOutput("End of file found");
                        } else {
//                        String gedcomXsdLocation = "/xsd/gedcom-import.xsd";
//                            String gedcomXsdLocation = "/xsd/gedcom-autogenerated.xsd";
                            String typeString;
                            if (lineParts.length > 2) {
                                typeString = lineParts[2];
                            } else {
                                typeString = lineParts[1];
                            }
                            // todo: the type string needs to determine if this is an entity or a metadata file
                            currentEntity = getEntityDocument(destinationDirectory, createdNodes, lineParts[1]);
                            if (lineParts[1].equals("HEAD")) {
                                // because the schema specifies 1:1 of both head and entity we find rather than create the head and entity nodes
                                appendToTaskOutput("Reading Gedcom Header");
                                currentEntity.appendValue(lineParts[1], null, gedcomLevel);
                            } else {
                                // because the schema specifies 1:1 of both head and entity we find rather than create the head and entity nodes                                
                                if (lineParts.length > 2) {
                                    currentEntity.appendValue(lineParts[2], lineParts[1], gedcomLevel);
                                    appendToTaskOutput(lineParts[2]);
//                                    currentEntity.insertValue("gedcom-type", lineParts[2]);
//                                    if (lineParts[2].equals("NOTE")) {
//                                        currentEntity.insertValue("NoteText", lineParts[2]);
//                                        Element addedNoteElement = metadataDom.createElement("NoteText");
//                                        currentDomNode.appendChild(addedNoteElement);
//                                        previousField = addedNoteElement;
//                                    }
//                                }
                                } else {
                                    currentEntity.appendValue("gedcom-id", lineParts[1], gedcomLevel);
                                }
//                                System.out.println("currentDomElement: " + currentDomNode + " value: " + currentDomNode.getTextContent());
                            }
                        } // end skip overwrite
                    } else {
//                        if (lineParts.length > 2) {
                        // todo: move this into an array to be processed after all the fields have been insterted


////                            gedcomImdiObject.saveChangesToCache(true);
//                                try {
//                                    URI linkUri = metadataBuilder.addChildNode(gedcomImdiObject, ".Gedcom.Relation", null, null, null);
//                                    ImdiTreeObject linkImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, linkUri);
//                                    appendToTaskOutput(importTextArea, "--> gedcomImdiObject.getChildCount: " + gedcomImdiObject.getChildCount());
//                                    gedcomImdiObject.loadImdiDom();
//                                    gedcomImdiObject.clearChildIcons();
//                                    gedcomImdiObject.clearIcon();
////                            gedcomImdiObject.waitTillLoaded();
//                                    appendToTaskOutput(importTextArea, "--> link url: " + linkImdiObject.getUrlString());
////                            appendToTaskOutput(importTextArea, "--> InternalNameT2" + lineParts[2] + " : " + linkImdiObject.getUrlString());
////                            createdNodesTable.put(lineParts[2], linkImdiObject.getUrlString());
////                            createdNodes.add(linkImdiObject.getUrlString());
////                            System.out.println("keys: " + linkImdiObject.getFields().keys().nextElement());
//                                    ImdiField[] currentField = linkImdiObject.getFields().get("Link");
//                                    if (currentField != null && currentField.length > 0) {
//                                        appendToTaskOutput(importTextArea, "--> Link" + lineParts[2]);
//                                        // the target of this link might not be read in at this point so lets store the fields for updateing later
//                                        //createdNodesTable.get(lineParts[2])
//                                        currentField[0].setFieldValue(lineParts[2], false, true);
//                                        linkNodes.add(linkImdiObject);
////                                appendToTaskOutput(importTextArea, "--> link count: " + linkFields.size());
//                                    }
//                                    ImdiField[] currentField1 = linkImdiObject.getFields().get("Type");
//                                    if (currentField1 != null && currentField1.length > 0) {
//                                        appendToTaskOutput(importTextArea, "--> Type" + lineParts[1]);
//                                        currentField1[0].setFieldValue(lineParts[1], false, true);
//                                    }
//                                    ImdiField[] currentField2 = linkImdiObject.getFields().get("TargetName");
//                                    if (currentField2 != null && currentField2.length > 0) {
//                                        appendToTaskOutput(importTextArea, "--> TargetName" + lineParts[2]);
//                                        currentField2[0].setFieldValue(lineParts[2], false, true);
//                                    }
//                                } catch (ArbilMetadataException arbilMetadataException) {
//                                    System.err.println(arbilMetadataException.getMessage());
//                                }
//                            }
//                        }
//                        }
                        // trim the nodes to the current gedcom level
//                        int parentNodeCount = 0;
//                        for (Node countingDomNode = currentDomNode; countingDomNode != null; countingDomNode = countingDomNode.getParentNode()) {
//                            parentNodeCount++;
//                        }
//                        for (int nodeCount = parentNodeCount; nodeCount > gedcomLevel + 3; nodeCount--) {
//                            System.out.println("gedcomLevel: " + gedcomLevel + " parentNodeCount: " + parentNodeCount + " nodeCount: " + nodeCount + " exiting from node: " + currentDomNode);
//                            currentDomNode = currentDomNode.getParentNode();
//                        }
//                        if (lineParts[1].equals("NAME") && currentDomNode.getNodeName().equals("Entity")) {
//                            // find the existing node if only one should exist
//                            System.out.println("Found Name Node easching: " + currentDomNode.getNodeName());
//                            for (Node childNode = currentDomNode.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
//                                System.out.println(childNode.getNodeName());
//                                if (childNode.getNodeName().equals("NAME")) {
//                                    System.out.println("Using found node");
//                                    currentDomNode = childNode;
//                                    break;
//                                }
//                            }
//                            appendToTaskOutput("Name: " + lineParts[2]);
//                        } else {
//                            System.out.println("Creating Node: " + lineParts[1]);
//                            // otherwise add the current gedcom node
//                            currentEntity.appendNode(lineParts[1]);
////                            Element addedElement = metadataDom.createElement(lineParts[1]);
////                            currentDomNode.appendChild(addedElement);
////                            currentDomNode = addedElement;
//                        }
                        // if the current line has a value then enter it into the node
                        if (lineParts.length > 2) {
//                        if (lineParts[1].equals("NAME")) {
//                            ImdiField[] currentField = gedcomImdiObject.getFields().get("Gedcom.Name");
//                            if (currentField != null && currentField.length > 0) {
//                                currentField[0].setFieldValue(lineParts[2], false, true);
//                                previousField = currentField;
//                            } else {
//                                System.err.println("missing field for: " + lineParts[1]);
//                                previousField = null;
//                            }
//                        } else {
//                            String gedcomPath = "Kinnate.Gedcom";
//                            int loopLevelCount = 0;
//                            int nodeLevelCount = 0;
//                            Node nodeLevelCountNode = currentDomNode;
//                            while (nodeLevelCountNode != null) {
//                                nodeLevelCountNode = nodeLevelCountNode.getParentNode();
//                                nodeLevelCount++;
//                            }
//                            for (String levelString : gedcomLevelStrings) {
//                                if (levelString.startsWith("@")) {
                            // this could be handled better
                            // this occurs at level 0 where the element type is named eg "0 @I9@ INDI"
//                                    levelString = "Entity";
//                                }
//                                gedcomPath = gedcomPath + "." + levelString;
//                                loopLevelCount++;
//                                if (loopLevelCount > nodeLevelCount) {
//                                    Element addedElement = metadataDom.createElement(levelString);
//                                    currentDomNode.appendChild(addedElement);
//                                    currentDomNode = addedElement;
//                                }
//                            }
//                            List<String> swapList = Arrays.asList(new String[]{
//                                        "Kinnate.Gedcom.HEAD.SOUR",
//                                        "Kinnate.Gedcom.HEAD.CORP",
//                                        "Kinnate.Gedcom.HEAD.CORP.ADDR",
//                                        "Kinnate.Gedcom.HEAD.SOUR.DATA",
//                                        "Kinnate.Gedcom.HEAD.CHAN.DATE",
//                                        "Kinnate.Gedcom.HEAD.DATE",
//                                        "Kinnate.Gedcom.HEAD.CHAR",
//                                        "Kinnate.Gedcom.Entity.NAME",
//                                        "Kinnate.Gedcom.Entity.REFN",
//                                        "Kinnate.Gedcom.Entity.REPO",
//                                        "Kinnate.Gedcom.Entity.DATA",
//                                        "Kinnate.Gedcom.Entity.ENGA",
//                                        "Kinnate.Gedcom.Entity.ENGA.SOUR",
//                                        "Kinnate.Gedcom.Entity.MARB",
//                                        "Kinnate.Gedcom.Entity.MARB.SOUR",
//                                        "Kinnate.Gedcom.Entity.MARC",
//                                        "Kinnate.Gedcom.Entity.MARC.SOUR",
//                                        "Kinnate.Gedcom.Entity.MARL",
//                                        "Kinnate.Gedcom.Entity.MARL.SOUR",
//                                        "Kinnate.Gedcom.Entity.MARS",
//                                        "Kinnate.Gedcom.Entity.MARS.SOUR",
//                                        "Kinnate.Gedcom.Entity.DIV",
//                                        "Kinnate.Gedcom.Entity.DIV.SOUR",
//                                        "Kinnate.Gedcom.Entity.DIVF",
//                                        "Kinnate.Gedcom.Entity.DIVF.SOUR",
//                                        "Kinnate.Gedcom.Entity.DATA.EVEN",
//                                        "Kinnate.Gedcom.Entity.REPO.CALN",
//                                        "Kinnate.Gedcom.Entity.NAME.SOUR",
//                                        "Kinnate.Gedcom.Entity.ADDR",
//                                        "Kinnate.Gedcom.Entity.CHAN.DATE",
//                                        "Kinnate.Gedcom.Entity.DEAT",
//                                        "Kinnate.Gedcom.Entity.OBJE",
//                                        "Kinnate.Gedcom.HEAD.SOUR.CORP",
//                                        "Kinnate.Gedcom.HEAD.SOUR.CORP.ADDR",
//                                        "Kinnate.Gedcom.Entity.ANUL"});
//                            Element addedExtraElement = null;
//                            if (swapList.contains(gedcomPath)) {
//                                gedcomPath += "." + lineParts[1];
//                                currentEntity.appendNode(lineParts[1]);
////                                addedExtraElement = metadataDom.createElement(lineParts[1]);
////                                currentDomNode.appendChild(addedExtraElement);
////                                currentDomNode = addedExtraElement;
//                            }


                            // todo: filter the links found and handled below from being added here as metadata
                            currentEntity.appendValue(lineParts[1], lineParts[2], gedcomLevel);


//                            currentDomNode.setTextContent(/*gedcomPath + " : " +*/lineParts[2]);
//                            if (addedExtraElement != null) {
//                                addedExtraElement = null;
//                                currentDomNode = currentDomNode.getParentNode();
//                            }
//                            currentDomNode = currentDomNode.getParentNode();

//                        System.out.println("is template: " + gedcomImdiObject.nodeTemplate.pathIsChildNode(gedcomPath));

//                            if (!xsdTagsDone.contains(gedcomPath)) {
//                                while (gedcomLevelStrings.size() > xsdLevelStrings.size() + 1) {
//                                    String xsdLevelString = gedcomLevelStrings.get(xsdLevelStrings.size());
//                                    if (xsdLevelString.startsWith("@")) {
//                                        // this occurs at level 0 where the element type is named eg "0 @I9@ INDI"
//                                        xsdLevelString = "NamedElement";
//                                    }
//                                    xsdLevelStrings.add(xsdLevelString);
//                                    xsdString += "   <xs:element name=\"" + xsdLevelString + "\">\n";
//                                    xsdString += "<xs:complexType>\n<xs:sequence>\n";
//                                }
////                            while (gedcomLevelStrings.size() < xsdLevelStrings.size()) {
////                                xsdLevelStrings.remove(xsdLevelStrings.size() - 1);
////                                xsdString += "</xs:sequence>\n</xs:complexType>\n";
////                            }
//                                String xsdElementString = lineParts[1];
//                                if (xsdElementString.startsWith("@")) {
//                                    // this occurs at level 0 where the element type is named eg "0 @I9@ INDI"
//                                    xsdElementString = "NamedElement";
//                                }
//                                xsdString += "   <xs:element name=\"" + xsdElementString + "\" />\n";// + gedcomPath + "\n" + strLine + "\n";
//                                xsdTagsDone.add(gedcomPath);
//                            }
                            // create the link node when required
                            if (lineParts[2].startsWith("@") && lineParts[2].endsWith("@")) {
                                appendToTaskOutput("--> adding relation");
                                RelationType targetRelation = RelationType.none;
                                if (lineParts[2].equals("FAMS")) {
                                    targetRelation = RelationType.union;
                                } else if (lineParts[2].equals("FAMC")) {
                                    targetRelation = RelationType.ancestor;
                                } else {
                                    appendToTaskOutput("Unknown relation type: " + lineParts[2]);
                                    targetRelation = RelationType.ancestor;
                                }
                                // todo: modify the fam relations to consist of associations and direct sanuine links to the related entities
                                currentEntity.insertRelation(getEntityDocument(destinationDirectory, createdNodes, lineParts[2]).entityData, targetRelation, lineParts[2]);
                            }
                        }
                    }
                }
                currntLineCounter++;
                int currentProgressPercent = (int) ((double) currntLineCounter / (double) inputLineCount * 100);
                if (progressBar != null) {
                    progressBar.setValue(currentProgressPercent);
                }
            }

//            if (metadataDom != null) {
//                ArbilComponentBuilder.savePrettyFormatting(metadataDom, entityFile);
//                metadataDom = null;
//            }
//            ImdiLoader.getSingleInstance().saveNodesNeedingSave(true);
//            appendToTaskOutput(importTextArea, "--> link count: " + linkFields.size());
            // update all the links now we have the urls for each internal name

//            appendToTaskOutput(importTextArea, "xsdString:\n" + xsdString);

//            int linkNodesUpdated = 0;
//            for (URI currentUri : createdNodes) {
//                appendToTaskOutput(importTextArea, "linkParent: " + currentUri.toASCIIString());
//                try {
//                    String linkXpath = "/Kinnate/Relation/Link";
//                    Document linksDom = new CmdiComponentBuilder().getDocument(currentUri);
//                    NodeList relationLinkNodeList = org.apache.xpath.XPathAPI.selectNodeList(linksDom, linkXpath);
//                    for (int nodeCounter = 0; nodeCounter < relationLinkNodeList.getLength(); nodeCounter++) {
//                        Node relationLinkNode = relationLinkNodeList.item(nodeCounter);
//                        if (relationLinkNode != null) {
//                            // todo: update the links
//                            // todo: create links in ego and alter but but the type info such as famc only in the relevant entity
//                            String linkValue = createdNodesTable.get(relationLinkNode.getTextContent());
//                            if (linkValue != null) {
//                                relationLinkNode.setTextContent(linkValue);
//                                appendToTaskOutput(importTextArea, "linkValue: " + linkValue);
//                            }
//                        }
//                    }
//                    new CmdiComponentBuilder().savePrettyFormatting(linksDom, currentImdiObject.getFile());
//                } catch (TransformerException exception) {
//                    new ArbilBugCatcher().logError(exception);
//                }
//                linkNodesUpdated++;
//                if (progressBar != null) {
//                    progressBar.setValue((int) ((double) linkNodesUpdated / (double) createdNodes.size() * 100 / 2 + 50));
//                }
//            }
            saveAllDocuments();
            appendToTaskOutput("Import finished with a node count of: " + createdNodes.size());

//            gedcomImdiObject.saveChangesToCache(true);
//            gedcomImdiObject.loadImdiDom();
//            gedcomImdiObject.clearChildIcons();
//            gedcomImdiObject.clearIcon();
            ArbilDataNodeLoader.getSingleInstance().saveNodesNeedingSave(true);
        } catch (IOException exception) {
            new ArbilBugCatcher().logError(exception);
            appendToTaskOutput("Error: " + exception.getMessage());
//        } catch (ParserConfigurationExceptionparserConfigurationException) {
//            new ArbilBugCatcher().logError(parserConfigurationException);
//            appendToTaskOutput("Error: " + parserConfigurationException.getMessage());
//        } catch (DOMExceptiondOMException) {
//            new ArbilBugCatcher().logError(dOMException);
//            appendToTaskOutput("Error: " + dOMException.getMessage());
//        } catch (SAXExceptionsAXException) {
//            new ArbilBugCatcher().logError(sAXException);
//            appendToTaskOutput("Error: " + sAXException.getMessage());
//        }
        } catch (ImportException exception) {
            new ArbilBugCatcher().logError(exception);
            appendToTaskOutput("Error: " + exception.getMessage());
        }
//        LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree");
//        String[] createdNodePaths = new String[createdNodes.size()];
//        int createdNodeCounter = 0;
//        for (URI currentUri : createdNodes) {
//            createdNodePaths[createdNodeCounter] = currentUri.toASCIIString();
////            createdNodeCounter++;
//        }
//        LinorgSessionStorage.getSingleInstance().saveStringArray("KinGraphTree", createdNodePaths);
        return createdNodes.toArray(new URI[]{});
    }
}
