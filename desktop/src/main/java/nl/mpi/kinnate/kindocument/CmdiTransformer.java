package nl.mpi.kinnate.kindocument;

import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.event.SaxonOutputKeys;
import nl.mpi.arbil.util.ArbilBugCatcher;

/**
 *  Document   : CmdiTransformer
 *  Created on : Nov 17, 2011, 3:23:27 PM
 *  Author     : Peter Withers
 */
public class CmdiTransformer {

    private URL component2SchemaXsl = this.getClass().getResource("/xsd/comp2schema.xsl"); // "http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema.xsl";

    public URI getXsdUrlString(String entityType) throws KinXsdException {
        try {
            File xsdFile = transformProfileXmlToXsd("clarin.eu:cr1:p_1320657629627", entityType);
            return xsdFile.toURI();
        } catch (IOException exception) {
            System.out.println("exception: " + exception.getMessage());
            throw new KinXsdException();
        } catch (TransformerException exception) {
            System.out.println("exception: " + exception.getMessage());
            throw new KinXsdException();
        }
    }

    private File transformProfileXmlToXsd(String profileId, String entityType) throws IOException, TransformerException {
        String cmdiProfileXmlUrl = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId + "/xml";
        File outputFile = new File(ArbilSessionStorage.getSingleInstance().getCacheDirectory(), entityType + "-" + profileId + ".xsd");
        System.out.println("outputFile: " + outputFile.getAbsolutePath());

//        System.out.println(ArbilSessionStorage.getSingleInstance().updateCache("http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema-header.xsl", 1));
//        System.out.println(ArbilSessionStorage.getSingleInstance().updateCache("http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema.xsl", 1));
//        System.out.println(ArbilSessionStorage.getSingleInstance().updateCache("http://www.clarin.eu/cmd/xslt/comp2schema-v2/cleanup-xsd.xsl", 1));

        generateXsd(cmdiProfileXmlUrl, outputFile);
        return outputFile;
    }

    private void generateXsd(String cmdiProfileXmlUrl, File outputFile) {
        Templates componentToSchemaTemplates;
        try {
            System.setProperty("javax.xml.transform.TransformerFactory", net.sf.saxon.TransformerFactoryImpl.class.getName());
            componentToSchemaTemplates = TransformerFactory.newInstance().newTemplates(new StreamSource(component2SchemaXsl.toString()));
        } catch (TransformerConfigurationException e) {
            new ArbilBugCatcher().logError("Cannot create Template", e);
            return;
        }
        try {
            Transformer transformer = componentToSchemaTemplates.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(SaxonOutputKeys.INDENT_SPACES, "1"); //Keeps the downloads a lot smaller.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            transformer.transform(new StreamSource(cmdiProfileXmlUrl), new StreamResult(new java.io.FileOutputStream(outputFile)));
        } catch (TransformerConfigurationException e) {
            new ArbilBugCatcher().logError("Cannot create Transformer", e);
        } catch (TransformerException e) {
            new ArbilBugCatcher().logError("Cannot transform xml file: ", e);
        } catch (FileNotFoundException e) {
            new ArbilBugCatcher().logError(e);
        }
    }

    static public void main(String[] args) {
        try {
            new CmdiTransformer().transformProfileXmlToXsd("clarin.eu:cr1:p_1320657629627", "individual");
        } catch (IOException exception) {
            System.out.println("exception: " + exception.getMessage());
        } catch (TransformerException exception) {
            System.out.println("exception: " + exception.getMessage());
        }

    }
}
