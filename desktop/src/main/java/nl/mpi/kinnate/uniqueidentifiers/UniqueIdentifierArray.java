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
package nl.mpi.kinnate.uniqueidentifiers;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
//            new ArbilBugCatcher().logError(exception);
            System.out.println("JAXBException: " + exception.getMessage());
        }
    }
}
