package nl.mpi.kinnate.uniqueidentifiers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

        pid /*Persistent Identifier*/, lid /*Local Identifier*/, tid /*Transient Identifier produced by md5 summing a string */, gid /*Graphics Identifier*/

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

    public UniqueIdentifier(String userDefinedIdentifier, IdentifierType identifierTypeLocal) {
        // this is required so that transient entities have the same identifier on each redraw and on loading a saved document, otherwise the entity positions on the graph get lost
        // hash the string text so that it is valid in xml attributes but still reconstructable from the same user input
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digestbytes = messageDigest.digest(userDefinedIdentifier.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < digestbytes.length; ++i) {
                hexString.append(Integer.toHexString(0x0100 + (digestbytes[i] & 0x00FF)).substring(1));
            }
            switch (identifierTypeLocal) {
                case tid:
                    identifierString = hexString.toString();
                    identifierType = identifierTypeLocal;
                    break;
                case pid:
                case lid:
                case gid:
                    throw new UnsupportedOperationException("Unsupported user defined identifier, these must be transient identifiers");
            }
        } catch (NoSuchAlgorithmException exception) {
            throw new UnsupportedOperationException("Cannot hash the transient identifier");
        }
    }

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
