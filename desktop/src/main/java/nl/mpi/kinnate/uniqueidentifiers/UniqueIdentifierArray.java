package nl.mpi.kinnate.uniqueidentifiers;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import nl.mpi.arbil.util.ArbilBugCatcher;

/**
 *  Document   : UniqueIdentifierArray
 *  Created on : Jul 27, 2011, 3:27:19 PM
 *  Author     : Peter Withers
 */
@XmlRootElement(name = "IdentifierArray", namespace = "http://mpi.nl/tla/kin")
public class UniqueIdentifierArray {

    @XmlElement(name = "Identifier", namespace = "http://mpi.nl/tla/kin")
    public UniqueIdentifier[] testIdentifiers;

    public static void main(String[] Args) {
        try {
            UniqueIdentifierArray identifierArray = new UniqueIdentifierArray();
            identifierArray.testIdentifiers = new UniqueIdentifier[]{new UniqueIdentifier(UniqueIdentifier.IdentifierType.tid), new UniqueIdentifier(UniqueIdentifier.IdentifierType.tid)};

            JAXBContext jaxbContext = JAXBContext.newInstance(UniqueIdentifierArray.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(identifierArray, stringWriter);
            System.out.println(stringWriter);
        } catch (JAXBException exception) {
            new ArbilBugCatcher().logError(exception);
        }
    }
}
