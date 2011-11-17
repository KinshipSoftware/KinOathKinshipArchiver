/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.kinnate.kindocument;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;

/**
 *  Document   : CmdiTransformer
 *  Created on : Nov 17, 2011, 3:23:27 PM
 *  Author     : Peter Withers
 */
public class CmdiTransformer {

    private String component2SchemaXsl = "http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema.xsl";

    private void transformProfileXmlToXsd(String profileId, String entityType) throws IOException, TransformerException {
        String cmdiProfileXml = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId + "/xml";
        File outputFile = new File(ArbilSessionStorage.getSingleInstance().getCacheDirectory(), entityType);
        System.out.println("outputFile: " + outputFile.getAbsolutePath());
        // 1. Instantiate a TransformerFactory.
        javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
        // 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
        // URL xslUrl = this.getClass().getResource("/nl/mpi/arbil/resources/xsl/imdi-viewer.xsl");
        URL xslUrl = new URL(component2SchemaXsl);
        // look in the current template for a custom xsl
//	File xslFile = null;
//	xslFile = new File(inputNode.getNodeTemplate().getTemplateDirectory(), "format.xsl");
//	if (xslFile != null && xslFile.exists()) {
//	    xslUrl = xslFile.toURL();
//	}
        javax.xml.transform.Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(xslUrl.toString()));
        // 3. Use the Transformer to transform an XML Source and send the output to a Result object.
        transformer.transform(new javax.xml.transform.stream.StreamSource(cmdiProfileXml), new javax.xml.transform.stream.StreamResult(new java.io.FileOutputStream(outputFile)));
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
