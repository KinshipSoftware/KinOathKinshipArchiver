package nl.mpi.kinnate.uniqueidentifiers;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.util.ArbilBugCatcher;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *  Document   : LocalIdentifier
 *  Created on : Mar 22, 2011, 2:25:22 PM
 *  Author     : Peter Withers
 */
public class LocalIdentifier {

    public String getUniqueIdentifier(File entityFile) {
        StringBuilder hexString = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(entityFile.toString().getBytes());
            byte[] md5sum = digest.digest();
            for (int byteCounter = 0; byteCounter < md5sum.length; ++byteCounter) {
                hexString.append(Integer.toHexString(0x0100 + (md5sum[byteCounter] & 0x00FF)).substring(1));
            }
        } catch (NoSuchAlgorithmException algorithmException) {
            GuiHelper.linorgBugCatcher.logError(algorithmException);
        }
        return hexString.toString();
    }

    public String setLocalIdentifier(File entityFile) {
        String localIdentifier = getUniqueIdentifier(entityFile);
        try {
            Document metadataDom = ArbilComponentBuilder.getDocument(entityFile.toURI());
            // add a unique identifier to the entity node
            Element localIdentifierElement = metadataDom.createElement("LocalIdentifier");
            localIdentifierElement.setTextContent(localIdentifier);
//            Node uniqueIdentifierNode = metadataDom.getElementsByTagName("UniqueIdentifier").item(0); // todo: this assumes there will only be one and always one, generally this will be fine though
            Node uniqueIdentifierNode = org.apache.xpath.XPathAPI.selectSingleNode(metadataDom, "/:Kinnate/:Entity/:UniqueIdentifier");
            uniqueIdentifierNode.appendChild(localIdentifierElement);
            ArbilComponentBuilder.savePrettyFormatting(metadataDom, entityFile);
        } catch (DOMException exception) {
            new ArbilBugCatcher().logError(exception);
        } catch (TransformerException exception) {
            new ArbilBugCatcher().logError(exception);
        } catch (IOException exception) {
            new ArbilBugCatcher().logError(exception);
        } catch (ParserConfigurationException exception) {
            new ArbilBugCatcher().logError(exception);
        } catch (SAXException exception) {
            new ArbilBugCatcher().logError(exception);
        }
        return localIdentifier;
    }
}
