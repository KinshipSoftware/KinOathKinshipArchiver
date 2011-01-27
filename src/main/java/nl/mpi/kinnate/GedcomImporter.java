/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.kinnate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JTextArea;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.GuiHelper;
//import nl.mpi.arbil.ImdiField;
import nl.mpi.arbil.ImdiField;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.data.ImdiTreeObject;
import nl.mpi.arbil.data.MetadataBuilder;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *  Document   : GedcomImporter
 *  Created on : Aug 24, 2010, 2:40:21 PM
 *  Author     : Peter Withers
 */
public class GedcomImporter {

    private void appendToTaskOutput(JTextArea importTextArea, String lineOfText) {
        importTextArea.append(lineOfText + "\n");
        importTextArea.setCaretPosition(importTextArea.getText().length());
    }

    public void importTestFile(JTextArea importTextArea, String testFileString) {
        ArrayList<String> createdNodes = new ArrayList<String>();
        Hashtable<String, String> createdNodesTable = new Hashtable<String, String>();
        ArrayList<ImdiTreeObject> linkNodes = new ArrayList<ImdiTreeObject>();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(testFileString)));
        // really should close the file properly but this is only for testing at this stage

//        URI targetFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getCacheDirectory(), gedcomXsdLocation);
        CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
//        try {
//            targetFileURI = componentBuilder.createComponentFile(targetFileURI, this.getClass().getResource(gedcomXsdLocation).toURI(), false);
//        } catch (URISyntaxException ex) {
//            GuiHelper.linorgBugCatcher.logError(ex);
//            return;
//        }

        MetadataBuilder metadataBuilder = new MetadataBuilder();
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

            ImdiTreeObject gedcomImdiObject = null;
            Document metadataDom = null;
            Element previousField = null;
            Node currentDomNode = null;

            String gedcomPreviousPath = "";
            while ((strLine = bufferedReader.readLine()) != null) {
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
                appendToTaskOutput(importTextArea, strLine);
                boolean lastFieldContinued = false;
                if (lineParts[1].equals("CONT")) {
                    if (previousField != null) {
                        // todo: if the previous field is null this should be caught and handled as an error in the source file
                        previousField.setTextContent(previousField.getTextContent() + "\n" + lineParts[2]);
                    }
                    lastFieldContinued = true;
                } else if (lineParts[1].equals("CONC")) {
                    if (previousField != null) {
                        // todo: if the previous field is null this should be caught and handled as an error in the source file
                        previousField.setTextContent(previousField.getTextContent() + lineParts[2]);
                    }
                    lastFieldContinued = true;
                }
                if (lastFieldContinued == false) {
                    previousField = null;
                    if (gedcomLevel == 0) {
//                        if (createdNodes.size() > 20) {
//                            appendToTaskOutput(importTextArea, "stopped import at node count: " + createdNodes.size());
//                            break;
//                        }
                        if (metadataDom != null) {
                            new CmdiComponentBuilder().savePrettyFormatting(metadataDom, gedcomImdiObject.getFile());
                            metadataDom = null;
                        }
                        if (lineParts[1].equals("TRLR")) {
                            appendToTaskOutput(importTextArea, "--> end of file found");
                        } else {
//                        String gedcomXsdLocation = "/xsd/gedcom-import.xsd";
                            String gedcomXsdLocation = "/xsd/gedcom-autogenerated.xsd";
                            URI eniryFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getCacheDirectory(), gedcomXsdLocation);
                            try {
                                eniryFileURI = componentBuilder.createComponentFile(eniryFileURI, this.getClass().getResource(gedcomXsdLocation).toURI(), false);
                            } catch (URISyntaxException ex) {
                                GuiHelper.linorgBugCatcher.logError(ex);
                                appendToTaskOutput(importTextArea, "error: " + ex.getMessage());
                                return;
                            }

                            gedcomImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, eniryFileURI);
                            gedcomImdiObject.waitTillLoaded();
                            appendToTaskOutput(importTextArea, "--> InternalNameT1" + lineParts[1] + " : " + gedcomImdiObject.getUrlString());
                            createdNodesTable.put(lineParts[1], gedcomImdiObject.getUrlString());
                            createdNodes.add(gedcomImdiObject.getUrlString());
                            metadataDom = new CmdiComponentBuilder().getDocument(gedcomImdiObject.getURI());
                            currentDomNode = metadataDom.getDocumentElement();
                            // find the deepest element node to start adding child nodes to
                            for (Node childNode = currentDomNode.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
                                System.out.println("childNode: " + childNode);
                                System.out.println("childNodeType: " + childNode.getNodeType());
                                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                                    System.out.println("entering node");
                                    currentDomNode = childNode;
                                    childNode = childNode.getFirstChild();
                                    if (childNode == null) {
                                        break;
                                    }
                                }
                            }
                            if (lineParts[1].equals("HEAD")) {
                                Element headElement = metadataDom.createElement("HEAD");
                                currentDomNode.appendChild(headElement);
                                currentDomNode = headElement;
                            } else {
                                Element entityElement = metadataDom.createElement("Entity");
                                currentDomNode.appendChild(entityElement);
                                currentDomNode = entityElement;
                                System.out.println("currentDomElement: " + currentDomNode);
                                Element addedElement = metadataDom.createElement("InternalName");
                                addedElement.setTextContent(lineParts[1]);
                                currentDomNode.appendChild(addedElement);
                                if (lineParts.length > 2) {
                                    Element addedElement2 = metadataDom.createElement("Type");
                                    addedElement2.setTextContent(lineParts[2]);
                                    currentDomNode.appendChild(addedElement2);
                                }
                            }
                            System.out.println("currentDomElement: " + currentDomNode + " value: " + currentDomNode.getTextContent());
                            appendToTaskOutput(importTextArea, "--> new node started");
                        }
                    } else {
                        if (lineParts.length > 2) {
                            // todo: move this into an array to be processed after all the fields have been insterted
//                            if (lineParts[2].startsWith("@") && lineParts[2].endsWith("@")) {
//                                appendToTaskOutput(importTextArea, "--> link adding");
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
                        }
                        // trim the nodes to the current gedcom level
                        int parentNodeCount = 0;
                        for (Node countingDomNode = currentDomNode; countingDomNode != null; countingDomNode = countingDomNode.getParentNode()) {
                            parentNodeCount++;
                        }
                        for (int nodeCount = parentNodeCount; nodeCount > gedcomLevel + 3; nodeCount--) {
                            System.out.println("gedcomLevel: " + gedcomLevel + " parentNodeCount: " + parentNodeCount + " nodeCount: " + nodeCount + " exiting from node: " + currentDomNode);
                            currentDomNode = currentDomNode.getParentNode();
                        }
                        // add the current gedcom node
                        Element addedElement = metadataDom.createElement(lineParts[1]);
                        currentDomNode.appendChild(addedElement);
                        currentDomNode = addedElement;
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
                            String gedcomPath = "Kinnate.Gedcom";
//                            int loopLevelCount = 0;
//                            int nodeLevelCount = 0;
//                            Node nodeLevelCountNode = currentDomNode;
//                            while (nodeLevelCountNode != null) {
//                                nodeLevelCountNode = nodeLevelCountNode.getParentNode();
//                                nodeLevelCount++;
//                            }
                            for (String levelString : gedcomLevelStrings) {
                                if (levelString.startsWith("@")) {
                                    // this could be handled better
                                    // this occurs at level 0 where the element type is named eg "0 @I9@ INDI"
                                    levelString = "NamedElement";
                                }
                                gedcomPath = gedcomPath + "." + levelString;
//                                loopLevelCount++;
//                                if (loopLevelCount > nodeLevelCount) {
//                                    Element addedElement = metadataDom.createElement(levelString);
//                                    currentDomNode.appendChild(addedElement);
//                                    currentDomNode = addedElement;
//                                }
                            }
                            List<String> swapList = Arrays.asList(new String[]{"Kinnate.Gedcom.HEAD.SOUR", "Kinnate.Gedcom.HEAD.CORP", "Kinnate.Gedcom.HEAD.CORP.ADDR", "Kinnate.Gedcom.HEAD.SOUR.DATA", "Kinnate.Gedcom.HEAD.DATE", "Kinnate.Gedcom.HEAD.CHAR", "Kinnate.Gedcom.ADDR", "Kinnate.Gedcom.CHAN.DATE", "Kinnate.Gedcom.HEAD.SOUR.CORP", "Kinnate.Gedcom.HEAD.SOUR.CORP.ADDR"});
                            Element addedExtraElement = null;
                            if (swapList.contains(gedcomPath)) {
                                gedcomPath += "." + lineParts[1];
                                addedExtraElement = metadataDom.createElement(lineParts[1]);
                                currentDomNode.appendChild(addedExtraElement);
                                currentDomNode = addedExtraElement;
                            }
                            currentDomNode.setTextContent(gedcomPath + " : " + lineParts[2]);
                            if (addedExtraElement != null) {
                                addedExtraElement = null;
                                currentDomNode = currentDomNode.getParentNode();
                            }
//                            currentDomNode = currentDomNode.getParentNode();

//                        System.out.println("is template: " + gedcomImdiObject.nodeTemplate.pathIsChildNode(gedcomPath));

                            if (!xsdTagsDone.contains(gedcomPath)) {
                                while (gedcomLevelStrings.size() > xsdLevelStrings.size() + 1) {
                                    String xsdLevelString = gedcomLevelStrings.get(xsdLevelStrings.size());
                                    if (xsdLevelString.startsWith("@")) {
                                        // this occurs at level 0 where the element type is named eg "0 @I9@ INDI"
                                        xsdLevelString = "NamedElement";
                                    }
                                    xsdLevelStrings.add(xsdLevelString);
                                    xsdString += "   <xs:element name=\"" + xsdLevelString + "\">\n";
                                    xsdString += "<xs:complexType>\n<xs:sequence>\n";
                                }
//                            while (gedcomLevelStrings.size() < xsdLevelStrings.size()) {
//                                xsdLevelStrings.remove(xsdLevelStrings.size() - 1);
//                                xsdString += "</xs:sequence>\n</xs:complexType>\n";
//                            }
                                String xsdElementString = lineParts[1];
                                if (xsdElementString.startsWith("@")) {
                                    // this occurs at level 0 where the element type is named eg "0 @I9@ INDI"
                                    xsdElementString = "NamedElement";
                                }
                                xsdString += "   <xs:element name=\"" + xsdElementString + "\" />\n";// + gedcomPath + "\n" + strLine + "\n";
                                xsdTagsDone.add(gedcomPath);
                            }
                        }
                    }
                }
//                1 NAME John A. Nairn
            }

            if (metadataDom != null) {
                new CmdiComponentBuilder().savePrettyFormatting(metadataDom, gedcomImdiObject.getFile());
                metadataDom = null;
            }
//            ImdiLoader.getSingleInstance().saveNodesNeedingSave(true);
//            appendToTaskOutput(importTextArea, "--> link count: " + linkFields.size());
            // update all the links now we have the urls for each internal name
            for (ImdiTreeObject linkImdiObject : linkNodes) {
                linkImdiObject.waitTillLoaded();
                appendToTaskOutput(importTextArea, "linkParent: " + linkImdiObject.getParentDomNode());
                ImdiField[] currentField = linkImdiObject.getFields().get("Link");
                if (currentField != null && currentField.length > 0) {
                    appendToTaskOutput(importTextArea, "linkA: " + currentField[0].getFieldValue());
                    appendToTaskOutput(importTextArea, "linkB: " + createdNodesTable.get(currentField[0].getFieldValue()));
                    String linkValue = createdNodesTable.get(currentField[0].getFieldValue());
                    if (linkValue != null) {
                        currentField[0].setFieldValue(linkValue, false, true);
                    }
                    appendToTaskOutput(importTextArea, "linkC: " + currentField[0].getFieldValue());
//                    linkImdiObject.saveChangesToCache(true);
                }
            }
            System.out.println("xsdString: " + xsdString);
            appendToTaskOutput(importTextArea, "xsdString:\n" + xsdString);
            appendToTaskOutput(importTextArea, "import finished with a node count of: " + createdNodes.size());

//            gedcomImdiObject.saveChangesToCache(true);
//            gedcomImdiObject.loadImdiDom();
//            gedcomImdiObject.clearChildIcons();
//            gedcomImdiObject.clearIcon();
            ImdiLoader.getSingleInstance().saveNodesNeedingSave(true);
        } catch (IOException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            appendToTaskOutput(importTextArea, "error: " + exception.getMessage());
        } catch (ParserConfigurationException parserConfigurationException) {
            GuiHelper.linorgBugCatcher.logError(parserConfigurationException);
            appendToTaskOutput(importTextArea, "error: " + parserConfigurationException.getMessage());
        } catch (DOMException dOMException) {
            GuiHelper.linorgBugCatcher.logError(dOMException);
            appendToTaskOutput(importTextArea, "error: " + dOMException.getMessage());
        } catch (SAXException sAXException) {
            GuiHelper.linorgBugCatcher.logError(sAXException);
            appendToTaskOutput(importTextArea, "error: " + sAXException.getMessage());
        }
//        LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree");
        LinorgSessionStorage.getSingleInstance().saveStringArray("KinGraphTree", createdNodes.toArray(new String[]{}));
    }
}
