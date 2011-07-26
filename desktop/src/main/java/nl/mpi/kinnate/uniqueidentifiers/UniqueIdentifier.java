package nl.mpi.kinnate.uniqueidentifiers;

import java.util.UUID;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import nl.mpi.arbil.data.ArbilField;

/**
 *  Document   : UniqueIdentifier
 *  Created on : Jul 21, 2011, 3:23:17 PM
 *  Author     : Peter Withers
 */
@XmlRootElement(name = "UniqueIdentifier")
public class UniqueIdentifier {

    public enum IdentifierType {

        pid /*Persistent Identifier*/, lid /*Local Identifier*/, tid /*Transient Identifier*/, gid /*Graphics Identifier*/

    }
    @XmlValue
    private String identifierString = null;
    @XmlAttribute(name = "type")
    private IdentifierType identifierType = null;

    private UniqueIdentifier() {
    }

    public UniqueIdentifier(IdentifierType identifierTypeLocal) {
        identifierType = identifierTypeLocal;
        identifierString = UUID.randomUUID().toString();
    }

    public UniqueIdentifier(ArbilField arbilField) {
        if (arbilField.getFullXmlPath().endsWith(".UniqueIdentifier.LocalIdentifier")) {
            identifierType = IdentifierType.lid;
        } else if (arbilField.getFullXmlPath().endsWith(".UniqueIdentifier.PersistantIdentifier")) {
            identifierType = IdentifierType.pid;
        } else {
            throw new UnsupportedOperationException("Incorrect ArbilField: " + arbilField.getFullXmlPath());
        }
        identifierString = arbilField.getFieldValue();
    }

    public UniqueIdentifier(String attributeIdentifier) {
        // reconstruct the identifier from an attribte string originally obtained by getAttributeIdentifier
        String[] attributeIdentifierParts = attributeIdentifier.split("\\:");
        if (attributeIdentifierParts.length != 2) {
            throw new UnsupportedOperationException("Incorrect identifier format: " + attributeIdentifier);
        }
        identifierType = IdentifierType.valueOf(attributeIdentifierParts[0]);
        identifierString = attributeIdentifierParts[1];
    }

//    public UniqueIdentifier(String userDefinedIdentifier, IdentifierType identifierTypeLocal) {
//        switch (identifierTypeLocal) {
//            case tid:
//                identifierString = userDefinedIdentifier;
//                identifierType = identifierTypeLocal;
//                break;
//            case pid:
//            case lid:
//            case gid:
//                throw new UnsupportedOperationException("Unsupported user defined identifier, these must be transient identifiers");
//        }
//    }

    public String getQueryIdentifier() {
        // todo: limit the query to local or persistent
        if (identifierString != null) {
            return identifierString;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String getAttributeIdentifier() {
        return identifierType.name() + ":" + identifierString;
    }

    public boolean isGraphicsIdentifier() {
        return identifierType == IdentifierType.gid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UniqueIdentifier other = (UniqueIdentifier) obj;
        if ((this.identifierString == null) ? (other.identifierString != null) : !this.identifierString.equals(other.identifierString)) {
            return false;
        }
        if (this.identifierType != other.identifierType) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + (this.identifierString != null ? this.identifierString.hashCode() : 0);
        hash = 19 * hash + (this.identifierType != null ? this.identifierType.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }
}
