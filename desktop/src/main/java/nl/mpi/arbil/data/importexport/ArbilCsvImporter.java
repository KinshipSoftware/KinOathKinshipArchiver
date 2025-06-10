/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.data.importexport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ArbilCsvImporter
 * Created on : Nov 16, 2009, 10:34:47 PM
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilCsvImporter {
    private final static Logger logger = LoggerFactory.getLogger(ArbilCsvImporter.class);

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
        messageDialogHandler = handler;
    }
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
        dataNodeLoader = dataNodeLoaderInstance;
    }
    private ArbilDataNode destinationCorpusNode;

    public ArbilCsvImporter(ArbilDataNode destinationCorpusNodeLocal) {
        destinationCorpusNode = destinationCorpusNodeLocal;
    }

    public void doImport() {
        File[] selectedFiles = messageDialogHandler.showFileSelectBox("Import CSV", false, true, null, MessageDialogHandler.DialogueType.open, null);
        if (selectedFiles != null && selectedFiles.length > 0) {
//                return "CSV File (comma or tab separated values)";
//                return selectedFile.getName().toLowerCase().endsWith(".csv");
            for (File currentFile : selectedFiles) {
                processCsvFile(currentFile);
            }
        }
    }

    private void cleanQuotes(String[] arrayToClean, String fileType) {
        if (arrayToClean.length > 0) {
            if (fileType.indexOf("\"") != -1) {
                arrayToClean[0] = arrayToClean[0].replaceAll("^\"", "");
                arrayToClean[arrayToClean.length - 1] = arrayToClean[arrayToClean.length - 1].replaceAll("\"$", "");
            }
        }
    }

    private void processCsvFile(File inputFile) {
        String csvHeaders[] = null;
        String fileType = ",";
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(inputFile));
            String currentLine = "";
            String remainderOfLastLine = "";
            StringTokenizer stringTokeniser = null;
            while ((currentLine = bufferedReader.readLine()) != null) {
                if (csvHeaders == null) {
                    fileType = "\"\\t\"";
                    csvHeaders = currentLine.split(fileType);
                    if (csvHeaders.length == 1) {
                        fileType = "\t";
                        csvHeaders = currentLine.split(fileType);
                    }
                    if (csvHeaders.length == 1) {
                        fileType = "\",\"";
                        csvHeaders = currentLine.split(fileType);
                    }
                    if (csvHeaders.length == 1) {
                        fileType = ",";
                        csvHeaders = currentLine.split(fileType);
                    }
                    cleanQuotes(csvHeaders, fileType);
                } else {
                    boolean skipLine = false;
                    if (fileType.contains("\"")) {
                        // some fields will contain line breaks and they, must be reassembled here
                        // but this can only be done if the file contains quotes around each feild
                        if (!currentLine.endsWith("\"")) {
                            remainderOfLastLine = remainderOfLastLine + "\n" + currentLine;
                            skipLine = true;
                        } else if (remainderOfLastLine.length() > 0) {
                            currentLine = remainderOfLastLine + "\n" + currentLine;
                            remainderOfLastLine = "";
                        }
                    }
                    if (!skipLine) {
                        String nodeType = MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session";
                        ArbilDataNode addedImdiObject = dataNodeLoader.getArbilDataNode(null, new MetadataBuilder().addChildNode(destinationCorpusNode, nodeType, null, null, null));
                        addedImdiObject.waitTillLoaded();
                        Map<String, ArbilField[]> addedNodesFields = addedImdiObject.getFields();
                        String[] currentLineArray = currentLine.split(fileType);
                        cleanQuotes(currentLineArray, fileType);
                        for (int columnCounter = 0; columnCounter < csvHeaders.length && columnCounter < currentLineArray.length; columnCounter++) {
                            logger.debug(csvHeaders[columnCounter] + " : " + currentLineArray[columnCounter]);
                            ArbilField[] currentFieldArray = addedNodesFields.get(csvHeaders[columnCounter]);
                            if (currentFieldArray != null) {
                                // TODO: check that the field does not already have a value and act accordingly (add new description?) if it does
                                currentFieldArray[0].setFieldValue(currentLineArray[columnCounter], false, true);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        } catch (ArbilMetadataException ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        } catch (RuntimeException ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioe) {
                    BugCatcherManager.getBugCatcher().logError(ioe);
                }
            }
        }
    }
}
