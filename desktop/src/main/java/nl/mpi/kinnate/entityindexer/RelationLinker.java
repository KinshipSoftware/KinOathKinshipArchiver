package nl.mpi.kinnate.entityindexer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.svg.GraphPanel;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *  Document   : RelationLinker
 *  Created on : Apr 12, 2011, 1:45:01 PM
 *  Author     : Peter Withers
 */
public class RelationLinker {

    public void linkEntities(GraphPanel graphPanel, String[] selectedIdentifiers, DataTypes.RelationType relationType) {
        String targetPath = graphPanel.getPathForElementId(selectedIdentifiers[0]);
        try {
            // todo: any changes to the ArbilDateNode should be saved here, although this should not be an issue because nodes are only appended to the end
            URI targetUri = new URI(targetPath);
            Document metadataDom = ArbilComponentBuilder.getDocument(targetUri);
            for (int relationCounter = 1 /* skip the first because that is the target */; relationCounter < selectedIdentifiers.length; relationCounter++) {
                Element relationElement = metadataDom.createElement("Relation");
                metadataDom.getDocumentElement().appendChild(relationElement);

                Element linkElement = metadataDom.createElement("Link");
                linkElement.setTextContent(graphPanel.getPathForElementId(selectedIdentifiers[relationCounter]));
                relationElement.appendChild(linkElement);

                // add a unique identifier of the target entity to the link
                Element uniqueIdentifierElement = metadataDom.createElement("UniqueIdentifier");
                Element localIdentifierElement = metadataDom.createElement("LocalIdentifier");
                localIdentifierElement.setTextContent(selectedIdentifiers[relationCounter]);
                uniqueIdentifierElement.appendChild(localIdentifierElement);
                relationElement.appendChild(uniqueIdentifierElement);

                Element typeElement = metadataDom.createElement("Type");
                typeElement.setTextContent(relationType.name());
                relationElement.appendChild(typeElement);

                Element targetNameElement = metadataDom.createElement("TargetName");
//                targetNameElement.setTextContent(lineParts[2]);
                relationElement.appendChild(targetNameElement);
            }
            ArbilComponentBuilder.savePrettyFormatting(metadataDom, new File(targetUri));
            new EntityCollection().updateDatabase(targetUri);
            //ArbilDataNodeLoader.getSingleInstance().
            // todo: the ArbilDateNode will need to be reloaded here
        } catch (URISyntaxException exception) {
            new ArbilBugCatcher().logError(exception);
        } catch (DOMException exception) {
            new ArbilBugCatcher().logError(exception);
        } catch (IOException exception) {
            new ArbilBugCatcher().logError(exception);
        } catch (ParserConfigurationException exception) {
            new ArbilBugCatcher().logError(exception);
        } catch (SAXException exception) {
            new ArbilBugCatcher().logError(exception);
        }
    }
}
