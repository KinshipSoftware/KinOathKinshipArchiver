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

        URI targetFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getCacheDirectory(), "/xsd/gedcom-import.xsd");
        CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
        try {
            targetFileURI = componentBuilder.createComponentFile(targetFileURI, this.getClass().getResource("/xsd/gedcom-import.xsd").toURI(), false);
        } catch (URISyntaxException ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
            return;
        }

        ImdiTreeObject gedcomImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, targetFileURI);
        gedcomImdiObject.waitTillLoaded();
        createdNodes.add(gedcomImdiObject.getUrlString());


        MetadataBuilder metadataBuilder = new MetadataBuilder();
//        metadataBuilder.addChildNode(gedcomImdiObject, ".Gedcom.Relation", null, null, null);
//        gedcomImdiObject.loadImdiDom();
//        gedcomImdiObject.waitTillLoaded();

        try {
            String strLine;
            int levelCounter = 0;
            while ((strLine = bufferedReader.readLine()) != null) {
                String[] lineParts = strLine.split(" ", 3);
                System.out.println(strLine);
                appendToTaskOutput(importTextArea, strLine);
                if (lineParts[0].equals("0")) {
                    if (createdNodes.size() > 16) {
//                    if (!lineParts[1].equals("HEAD")) {
                        break;
                    }
                    if (!lineParts[1].equals("HEAD")) {
//                        gedcomImdiObject.saveChangesToCache(true);
//                        URI eniryFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getCacheDirectory(), "Entity");
//                        targetFileURI = MetadataReader.getSingleInstance().addFromTemplate(new File(eniryFileURI), "Entity");
//                        gedcomImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, targetFileURI);
//                        gedcomImdiObject.waitTillLoaded();
//                        createdNodes.add(gedcomImdiObject.getUrlString());
                        gedcomImdiObject.saveChangesToCache(true);
                        URI eniryFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getCacheDirectory(), "/xsd/gedcom-import.xsd");
                        try {
                            eniryFileURI = componentBuilder.createComponentFile(eniryFileURI, this.getClass().getResource("/xsd/gedcom-import.xsd").toURI(), false);
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

                    }
                } else {
                    if (lineParts.length > 2) {
                        if (lineParts[2].startsWith("@") && lineParts[2].endsWith("@")) {
                            appendToTaskOutput(importTextArea, "--> link adding");
//                            gedcomImdiObject.saveChangesToCache(true);
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
                        }
                    }
                }
                if (strLine.length() >= 6) {
                    if (strLine.substring(2, 6).equals("NAME")) {
                        ImdiField[] currentField = gedcomImdiObject.getFields().get("Gedcom.Name");
                        if (currentField != null && currentField.length > 0) {
                            currentField[0].setFieldValue(strLine.substring(7), false, true);
                        } else {
                            System.err.println("missing field for: " + strLine);
                        }
                    } else {
                        String tagString = strLine.substring(2, 6);
                        ImdiField[] currentField = gedcomImdiObject.getFields().get("Gedcom." + tagString);
                        if (currentField != null && currentField.length > 0) {
                            currentField[currentField.length - 1].setFieldValue(strLine.substring(7), false, true);
                        } else {
                            System.err.println("missing field for: " + strLine);
                        }
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
