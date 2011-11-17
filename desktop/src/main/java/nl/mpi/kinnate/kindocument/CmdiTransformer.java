/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.kinnate.kindocument;

import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    private String component2SchemaXsl = "http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema.xsl";

    private void transformProfileXmlToXsd(String profileId, String entityType) throws IOException, TransformerException {
        String cmdiProfileXmlUrl = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId + "/xml";
        File outputFile = new File(ArbilSessionStorage.getSingleInstance().getCacheDirectory(), entityType);
        System.out.println("outputFile: " + outputFile.getAbsolutePath());

//        System.out.println(ArbilSessionStorage.getSingleInstance().updateCache("http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema-header.xsl", 1));
//        System.out.println(ArbilSessionStorage.getSingleInstance().updateCache("http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema.xsl", 1));

        generateXsd(cmdiProfileXmlUrl, outputFile);
        return;

//        // 1. Instantiate a TransformerFactory.
//        javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
//        // 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
//        // URL xslUrl = this.getClass().getResource("/nl/mpi/arbil/resources/xsl/imdi-viewer.xsl");
//        URL xslUrl = new URL(component2SchemaXsl);
//        // look in the current template for a custom xsl
////	File xslFile = null;
////	xslFile = new File(inputNode.getNodeTemplate().getTemplateDirectory(), "format.xsl");
////	if (xslFile != null && xslFile.exists()) {
////	    xslUrl = xslFile.toURL();
////	}
//        javax.xml.transform.Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(xslUrl.toString()));
//        // 3. Use the Transformer to transform an XML Source and send the output to a Result object.
//        transformer.transform(new javax.xml.transform.stream.StreamSource(cmdiProfileXmlUrl), new javax.xml.transform.stream.StreamResult(new java.io.FileOutputStream(outputFile)));
    }
//    public static <T> void marshal(T marshallableObject, OutputStream out) throws JAXBException, UnsupportedEncodingException {
//	String packageName = marshallableObject.getClass().getPackage().getName();
//	JAXBContext jc = JAXBContext.newInstance(packageName);
//
//	Marshaller m = jc.createMarshaller();
//	m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//	String schemaLocation = Configuration.getInstance().getSchemaLocation(marshallableObject.getClass().getName());
//	if (schemaLocation != null) {
//	    m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation);
//	}
//	Writer writer = new OutputStreamWriter(out, "UTF-8");
//	m.marshal(marshallableObject, writer);
//    }

    public void generateXsd(String cmdiProfileXmlUrl, File outputFile) {
        Templates componentToSchemaTemplates;
        try {
            System.setProperty("javax.xml.transform.TransformerFactory", net.sf.saxon.TransformerFactoryImpl.class.getName());
            componentToSchemaTemplates = TransformerFactory.newInstance().newTemplates(new StreamSource(component2SchemaXsl));
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
//        } catch (UnsupportedEncodingException e) {
//            new ArbilBugCatcher().logError("Error in encoding: ", e);
        } catch (FileNotFoundException e) {
            new ArbilBugCatcher().logError(e);
        }
    }

    static public void main(String[] args) {
        try {
            new CmdiTransformer().transformProfileXmlToXsd("clarin.eu:cr1:p_1320657629627", "Individual");
        } catch (IOException exception) {
            System.out.println("exception: " + exception.getMessage());
        } catch (TransformerException exception) {
            System.out.println("exception: " + exception.getMessage());
        }

    }
}
