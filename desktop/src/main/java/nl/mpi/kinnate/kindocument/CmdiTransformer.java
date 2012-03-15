package nl.mpi.kinnate.kindocument;

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
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.KinOathVersion;
import nl.mpi.kinnate.userstorage.KinSessionStorage;

/**
 *  Document   : CmdiTransformer
 *  Created on : Nov 17, 2011, 3:23:27 PM
 *  Author     : Peter Withers
 */
public class CmdiTransformer {

    private URL component2SchemaXsl = this.getClass().getResource("/xsd/comp2schema.xsl"); // "http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema.xsl";
    private URL component2SchemaXslHeader = this.getClass().getResource("/xsd/comp2schema-header.xsl");
    private URL component2SchemaXslCleanup = this.getClass().getResource("/xsd/cleanup-xsd.xsl");
    private URL cmdi2kmdiXsl = this.getClass().getResource("/xsd/cmdi2kmdi.xsl");
    private SessionStorage sessionStorage;

    public CmdiTransformer(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public URI getXsdUrlString(String entityType) throws KinXsdException {
//        String profileId = "clarin.eu:cr1:p_1320657629627";
        String profileId = "clarin.eu:cr1:p_1320657629627";
        File xsdFile = new File(sessionStorage.getCacheDirectory(), entityType + "-" + profileId + ".xsd");
        try {
            if (!xsdFile.exists()) {
                transformProfileXmlToXsd(xsdFile, profileId);
            }
            return xsdFile.toURI();
        } catch (IOException exception) {
            System.out.println("exception: " + exception.getMessage());
            throw new KinXsdException();
        } catch (TransformerException exception) {
            System.out.println("exception: " + exception.getMessage());
            throw new KinXsdException();
        }
    }

    public File transformProfileXmlToXsd(File outputFile, String profileId) throws IOException, TransformerException {
        String cmdiProfileXmlUrl = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId + "/xml";
        System.out.println("outputFile: " + outputFile.getAbsolutePath());

//        System.out.println(KinSessionStorage.getSingleInstance().updateCache("http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema-header.xsl", 1));
//        System.out.println(KinSessionStorage.getSingleInstance().updateCache("http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema.xsl", 1));
//        System.out.println(KinSessionStorage.getSingleInstance().updateCache("http://www.clarin.eu/cmd/xslt/comp2schema-v2/cleanup-xsd.xsl", 1));

        // todo: it might be nicer to put these files into a specific directory or into a temp directory
        File xlsFile = sessionStorage.updateCache(component2SchemaXsl.toExternalForm(), 1, false);
        System.out.println(sessionStorage.updateCache(component2SchemaXslHeader.toExternalForm(), 1, false));
        System.out.println(sessionStorage.updateCache(component2SchemaXslCleanup.toExternalForm(), 1, false));
        File intermediateFile = new File(outputFile.getParentFile(), profileId + "-cmdi.xsd");
        System.out.println(intermediateFile);
        System.out.println(outputFile);
        generateXsd(xlsFile, cmdiProfileXmlUrl, intermediateFile);
        File cmdi2kmdiFile = sessionStorage.updateCache(cmdi2kmdiXsl.toExternalForm(), 1, false);
        generateXsd(cmdi2kmdiFile, intermediateFile.toURI().toString(), outputFile);
        return outputFile;
    }

    private void generateXsd(File xlsFile, String cmdiProfileXmlUrl, File outputFile) {
        Templates componentToSchemaTemplates;
        try {
            System.setProperty("javax.xml.transform.TransformerFactory", net.sf.saxon.TransformerFactoryImpl.class.getName());
            componentToSchemaTemplates = TransformerFactory.newInstance().newTemplates(new StreamSource(xlsFile));
        } catch (TransformerConfigurationException e) {
            BugCatcherManager.getBugCatcher().logError("Cannot create Template", e);
            return;
        }
        try {
            Transformer transformer = componentToSchemaTemplates.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(SaxonOutputKeys.INDENT_SPACES, "1"); //Keeps the downloads a lot smaller.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            transformer.transform(new StreamSource(cmdiProfileXmlUrl), new StreamResult(new java.io.FileOutputStream(outputFile)));
        } catch (TransformerConfigurationException e) {
            BugCatcherManager.getBugCatcher().logError("Cannot create Transformer", e);
        } catch (TransformerException e) {
            BugCatcherManager.getBugCatcher().logError("Cannot transform xml file: ", e);
        } catch (FileNotFoundException e) {
            BugCatcherManager.getBugCatcher().logError(e);
        }
    }

    static public void main(String[] args) {
        try {
            String profileId = "clarin.eu:cr1:p_1320657629627";
            final KinSessionStorage kinSessionStorage = new KinSessionStorage(new ApplicationVersionManager(new KinOathVersion()));
            File xsdFile = new File(kinSessionStorage.getCacheDirectory(), "individual" + "-" + profileId + ".xsd");
            new CmdiTransformer(kinSessionStorage).transformProfileXmlToXsd(xsdFile, profileId);
        } catch (IOException exception) {
            System.out.println("exception: " + exception.getMessage());
        } catch (TransformerException exception) {
            System.out.println("exception: " + exception.getMessage());
        }

    }
}
