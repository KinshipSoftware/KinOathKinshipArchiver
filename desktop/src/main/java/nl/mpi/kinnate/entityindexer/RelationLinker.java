package nl.mpi.kinnate.entityindexer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.RelationArray;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import nl.mpi.kinnate.svg.GraphPanel;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *  Document   : RelationLinker
 *  Created on : Apr 12, 2011, 1:45:01 PM
 *  Author     : Peter Withers
 */
public class RelationLinker {

    private Node getEntityNode(Document entityDocument) {
        //org.apache.xpath.XPathAPI.selectSingleNode(metadataDom.getDocumentElement(), "//:Entity/:Relations");
        Element roodNode = entityDocument.getDocumentElement();
        NodeList entityNodeList = roodNode.getElementsByTagNameNS("http://mpi.nl/tla/kin", "Entity");
        Element entityNode = ((Element) entityNodeList.item(0));
        NodeList relationsNodeList = entityNode.getElementsByTagNameNS("http://mpi.nl/tla/kin", "Relations");
        Node relationsNode = relationsNodeList.item(0);
        if (relationsNode != null) {
            // remove the old relations
            entityNode.removeChild(relationsNode);
        }
        return entityNode;
    }

    public void linkEntities(GraphPanel graphPanel, UniqueIdentifier[] selectedIdentifiers, DataTypes.RelationType relationType) {
        HashMap<UniqueIdentifier, EntityData> selectedEntityMap = graphPanel.getEntitiesById(selectedIdentifiers);
        EntityData leadSelectionEntity = selectedEntityMap.get(selectedIdentifiers[0]);
        for (EntityData alterEntity : selectedEntityMap.values()) {
            if (!alterEntity.equals(leadSelectionEntity)) {
                // add the new relation
                leadSelectionEntity.addRelatedNode(alterEntity, 0, relationType, DataTypes.RelationLineType.sanguineLine, null, null);
            }
        }
        for (EntityData saveEntity : selectedEntityMap.values()) {
            String targetPath = saveEntity.getEntityPath();
            try {
                URI targetUri = new URI(targetPath);
                Document metadataDom = ArbilComponentBuilder.getDocument(targetUri);
                Node entityNode = getEntityNode(metadataDom);

                RelationArray relationArray = new RelationArray(saveEntity.getAllRelations());
                // add all the current relaions
                JAXBContext jaxbContext = JAXBContext.newInstance(RelationArray.class);
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.marshal(relationArray, entityNode);
                // save the xml file
                ArbilComponentBuilder.savePrettyFormatting(metadataDom, new File(targetUri));
                // update the database
                new EntityCollection().updateDatabase(targetUri);
            } catch (JAXBException exception) {
                // todo: inform the user if there is an error
                new ArbilBugCatcher().logError(exception);
//                throw new ImportException("Error: " + exception.getMessage());
//            } catch (TransformerException exception) {
//                new ArbilBugCatcher().logError(exception);
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
}
