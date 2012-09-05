package nl.mpi.kinnate.kindocument;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.KinOathVersion;
import nl.mpi.kinnate.userstorage.KinSessionStorage;

/**
 * Document : CmdiTransformer
 * Created on : Nov 17, 2011, 3:23:27 PM
 * Author : Peter Withers
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

    public URI getXsd(String profileId, boolean forceUpdate) throws KinXsdException {
        String profileFileName = profileId.replace(":", "_");
        final File profilesDirectory = new File(sessionStorage.getProjectDirectory(), "KmdiProfiles");
        if (!profilesDirectory.exists()) {
            profilesDirectory.mkdir();
        }
        File xsdFile = new File(profilesDirectory, profileFileName + "-kmdi.xsd");
        File intermediateFile = new File(profilesDirectory, profileFileName + "-cmdi.xsd");
        if (forceUpdate || !xsdFile.exists()) {
            transformProfileXmlToXsd(xsdFile, intermediateFile, profileId);
        }
        return xsdFile.toURI();
    }

    private File transformProfileXmlToXsd(File outputFile, File intermediateFile, String profileId) throws KinXsdException {
        String cmdiProfileXmlUrl = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId + "/xml";
        System.out.println("outputFile: " + outputFile.getAbsolutePath());

//        System.out.println(KinSessionStorage.getSingleInstance().updateCache("http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema-header.xsl", 1));
//        System.out.println(KinSessionStorage.getSingleInstance().updateCache("http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema.xsl", 1));
//        System.out.println(KinSessionStorage.getSingleInstance().updateCache("http://www.clarin.eu/cmd/xslt/comp2schema-v2/cleanup-xsd.xsl", 1));

        // todo: it might be nicer to put these files into a specific directory or into a temp directory
        File xlsFile = sessionStorage.updateCache(component2SchemaXsl.toExternalForm(), 1, false);
        File xslHeader = sessionStorage.updateCache(component2SchemaXslHeader.toExternalForm(), 1, false);
        File cleanUpXsl = sessionStorage.updateCache(component2SchemaXslCleanup.toExternalForm(), 1, false);
        System.out.println(intermediateFile);
        System.out.println(outputFile);
        try {
            generateXsd(xlsFile, cmdiProfileXmlUrl, intermediateFile);
            File cmdi2kmdiFile = sessionStorage.updateCache(cmdi2kmdiXsl.toExternalForm(), 1, false);
            generateXsd(cmdi2kmdiFile, intermediateFile.toURI().toString(), outputFile);
            return outputFile;
        } catch (IOException exception) {
            System.out.println("exception: " + exception.getMessage());
            intermediateFile.delete();
            xslHeader.delete();
            cleanUpXsl.delete();
            xlsFile.delete();
            throw new KinXsdException("Could not read the selected profile");
        } catch (TransformerException exception) {
            System.out.println("exception: " + exception.getMessage());
            intermediateFile.delete();
            xslHeader.delete();
            cleanUpXsl.delete();
            xlsFile.delete();
            throw new KinXsdException("Could not read the selected profile");
        }
    }

    private void generateXsd(File xlsFile, String cmdiProfileXmlUrl, File outputFile) throws IOException, TransformerException {
        Templates componentToSchemaTemplates;
        System.setProperty("javax.xml.transform.TransformerFactory", net.sf.saxon.TransformerFactoryImpl.class.getName());
        componentToSchemaTemplates = TransformerFactory.newInstance().newTemplates(new StreamSource(xlsFile));
        Transformer transformer = componentToSchemaTemplates.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//            transformer.setOutputProperty(SaxonOutputKeys.INDENT_SPACES, "1"); //Keeps the downloads a lot smaller.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transformer.transform(new StreamSource(cmdiProfileXmlUrl), new StreamResult(new java.io.FileOutputStream(outputFile)));
    }

    static public void main(String[] args) {
        try {
            String profileId = "clarin.eu:cr1:p_1320657629627";
            final KinSessionStorage kinSessionStorage = new KinSessionStorage(new ApplicationVersionManager(new KinOathVersion()));
            new CmdiTransformer(kinSessionStorage).getXsd(profileId, true);
        } catch (KinXsdException exception) {
            System.out.println("exception: " + exception.getMessage());
        }
    }
}
