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
import java.util.Hashtable;
import javax.swing.JTextArea;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.ImdiField;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.clarin.ArbilMetadataException;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.data.ImdiTreeObject;
import nl.mpi.arbil.data.MetadataBuilder;

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

    public void importTestFile(JTextArea importTextArea) {
        ArrayList<String> createdNodes = new ArrayList<String>();
        Hashtable<String, String> createdNodesTable = new Hashtable<String, String>();
        ArrayList<ImdiTreeObject> linkNodes = new ArrayList<ImdiTreeObject>();

        System.out.println(getClass().getResource("/TestGED/TGC55C.ged"));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/TestGED/TGC55C.ged")));
        // really should close the file properly but this is only for testing at this stage

//        URI targetFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getCacheDirectory(), gedcomXsdLocation);
        CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
//        try {
//            targetFileURI = componentBuilder.createComponentFile(targetFileURI, this.getClass().getResource(gedcomXsdLocation).toURI(), false);
//        } catch (URISyntaxException ex) {
//            GuiHelper.linorgBugCatcher.logError(ex);
//            return;
//        }

        ImdiTreeObject gedcomImdiObject = null;
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
            ImdiField[] previousField = null;
            while ((strLine = bufferedReader.readLine()) != null) {
                String[] lineParts = strLine.split(" ", 3);
                gedcomLevel = Integer.parseInt(lineParts[0]);
                while (gedcomLevelStrings.size() > gedcomLevel) {
                    gedcomLevelStrings.remove(gedcomLevelStrings.size() - 1);
                }
                while (xsdLevelStrings.size() > gedcomLevel) {
                    xsdLevelStrings.remove(xsdLevelStrings.size() - 1);
                    xsdString += "</xs:sequence>\n</xs:complexType>\n</xs:element>\n";
                }
                gedcomLevelStrings.add(lineParts[1]);
                System.out.println(strLine);
                System.out.println("gedcomLevelString: " + gedcomLevelStrings);
                appendToTaskOutput(importTextArea, strLine);
                boolean lastFieldContinued = false;
                if (lineParts[1].equals("CONT")) {
                    if (previousField != null) {
                        // todo: if the previous field is null this should be caught and handled as an error in the source file
                        previousField[previousField.length - 1].setFieldValue(previousField[previousField.length - 1].getFieldValue() + "\n" + lineParts[2], false, true);
                    }
                    lastFieldContinued = true;
                } else if (lineParts[1].equals("CONC")) {
                    if (previousField != null) {
                        // todo: if the previous field is null this should be caught and handled as an error in the source file
                        previousField[previousField.length - 1].setFieldValue(previousField[previousField.length - 1].getFieldValue() + lineParts[2], false, true);
                    }
                    lastFieldContinued = true;
                }
                if (lastFieldContinued == false) {
                    previousField = null;
                    if (gedcomLevel == 0) {
                        if (createdNodes.size() > 2) {
                            appendToTaskOutput(importTextArea, "stopped import at node count: " + createdNodes.size());
                            break;
                        }

//                        gedcomImdiObject.saveChangesToCache(true);
//                        URI eniryFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getCacheDirectory(), "Entity");
//                        targetFileURI = MetadataReader.getSingleInstance().addFromTemplate(new File(eniryFileURI), "Entity");
//                        gedcomImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, targetFileURI);
//                        gedcomImdiObject.waitTillLoaded();
//                        createdNodes.add(gedcomImdiObject.getUrlString());
                        if (gedcomImdiObject != null) {
                            gedcomImdiObject.saveChangesToCache(true);
                        }
//                        String gedcomXsdLocation = "/xsd/gedcom-import.xsd";
                        String gedcomXsdLocation = "/xsd/gedcom-autogenerated.xsd";
                        URI eniryFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getCacheDirectory(), gedcomXsdLocation);
                        try {
                            eniryFileURI = componentBuilder.createComponentFile(eniryFileURI, this.getClass().getResource(gedcomXsdLocation).toURI(), false);
                        } catch (URISyntaxException ex) {
                            GuiHelper.linorgBugCatcher.logError(ex);
                            return;
                        }

                        gedcomImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, eniryFileURI);
                        gedcomImdiObject.waitTillLoaded();
                        appendToTaskOutput(importTextArea, "--> InternalNameT1" + lineParts[1] + " : " + gedcomImdiObject.getUrlString());
                        createdNodesTable.put(lineParts[1], gedcomImdiObject.getUrlString());
                        createdNodes.add(gedcomImdiObject.getUrlString());
                        ImdiField[] currentField = gedcomImdiObject.getFields().get("Gedcom.InternalName");
                        if (currentField != null && currentField.length > 0) {
                            currentField[0].setFieldValue(lineParts[1], false, true);
                        }
                        if (lineParts.length > 2) {
                            ImdiField[] currentField2 = gedcomImdiObject.getFields().get("Gedcom.Type");
                            if (currentField2 != null && currentField2.length > 0) {
                                currentField2[0].setFieldValue(lineParts[2], false, true);
                            }
                        }
                        appendToTaskOutput(importTextArea, "--> new node started");
//        MetadataBuilder metadataBuilder = new MetadataBuilder();
//        metadataBuilder.addChildNode(gedcomImdiObject, ".Gedcom.Relation", null, null, null);
//        gedcomImdiObject.loadImdiDom();
//        gedcomImdiObject.waitTillLoaded();

                    } else {
                        if (lineParts.length > 2) {
                            if (lineParts[2].startsWith("@") && lineParts[2].endsWith("@")) {
                                appendToTaskOutput(importTextArea, "--> link adding");
//                            gedcomImdiObject.saveChangesToCache(true);
                                try {
                                    URI linkUri = metadataBuilder.addChildNode(gedcomImdiObject, ".Gedcom.Relation", null, null, null);
                                    ImdiTreeObject linkImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, linkUri);
                                    appendToTaskOutput(importTextArea, "--> gedcomImdiObject.getChildCount: " + gedcomImdiObject.getChildCount());
                                    gedcomImdiObject.loadImdiDom();
                                    gedcomImdiObject.clearChildIcons();
                                    gedcomImdiObject.clearIcon();
//                            gedcomImdiObject.waitTillLoaded();
                                    appendToTaskOutput(importTextArea, "--> link url: " + linkImdiObject.getUrlString());
//                            appendToTaskOutput(importTextArea, "--> InternalNameT2" + lineParts[2] + " : " + linkImdiObject.getUrlString());
//                            createdNodesTable.put(lineParts[2], linkImdiObject.getUrlString());
//                            createdNodes.add(linkImdiObject.getUrlString());
//                            System.out.println("keys: " + linkImdiObject.getFields().keys().nextElement());
                                    ImdiField[] currentField = linkImdiObject.getFields().get("Link");
                                    if (currentField != null && currentField.length > 0) {
                                        appendToTaskOutput(importTextArea, "--> Link" + lineParts[2]);
                                        // the target of this link might not be read in at this point so lets store the fields for updateing later
                                        //createdNodesTable.get(lineParts[2])
                                        currentField[0].setFieldValue(lineParts[2], false, true);
                                        linkNodes.add(linkImdiObject);
//                                appendToTaskOutput(importTextArea, "--> link count: " + linkFields.size());
                                    }
                                    ImdiField[] currentField1 = linkImdiObject.getFields().get("Type");
                                    if (currentField1 != null && currentField1.length > 0) {
                                        appendToTaskOutput(importTextArea, "--> Type" + lineParts[1]);
                                        currentField1[0].setFieldValue(lineParts[1], false, true);
                                    }
                                    ImdiField[] currentField2 = linkImdiObject.getFields().get("TargetName");
                                    if (currentField2 != null && currentField2.length > 0) {
                                        appendToTaskOutput(importTextArea, "--> TargetName" + lineParts[2]);
                                        currentField2[0].setFieldValue(lineParts[2], false, true);
                                    }
                                } catch (ArbilMetadataException arbilMetadataException) {
                                    System.err.println(arbilMetadataException.getMessage());
                                }
                            }
                        }
                    }
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
                        String gedcomPath = "Gedcom";
                        for (String levelString : gedcomLevelStrings) {
                            if (levelString.startsWith("@")) {
                                // this could be handled better
                                // this occurs at level 0 where the element type is named eg "0 @I9@ INDI"
                                levelString = "NamedElement";
                            }
                            gedcomPath = gedcomPath + "." + levelString;
                        }
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
                        ImdiField[] currentField = gedcomImdiObject.getFields().get(gedcomPath);
                        if (currentField != null && currentField.length > 0) {
                            currentField[currentField.length - 1].setFieldValue(lineParts[2], false, true);
                            previousField = currentField;
                        } else {
                            System.err.println("missing field for: " + gedcomLevelStrings);
                        }
//                        }
                    }
                }
//                1 NAME John A. Nairn
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
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }
//        LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree");
        LinorgSessionStorage.getSingleInstance().saveStringArray("KinGraphTree", createdNodes.toArray(new String[]{}));
    }
}
