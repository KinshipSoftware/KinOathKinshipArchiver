<?xml version="1.0" encoding="UTF-8"?>

<!--
    this is a modified copy of: http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema-header.xsl
    modified by Peter Wither for use in KinOath 2011/11/17
    
    $Rev: 484 $ 
    $Date: 2011-05-13 17:08:40 +0200 (Fri, 13 May 2011) $ 
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0">

    <xsl:template name="PrintHeaderType">
        <xs:simpleType name="simpletype-UniqueIdentifier">
            <xs:restriction base="xs:string">
                <xs:pattern value="[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{9}-[0-9A-F]{12}"/>
            </xs:restriction>
        </xs:simpleType>
        <xs:complexType name="complextype-UniqueIdentifier">
            <xs:simpleContent>
                <xs:extension base="cmd:simpletype-UniqueIdentifier">
                    <xs:attribute name="type">
                        <xs:simpleType>
                            <xs:restriction base="xs:string">
                                <xs:pattern value="[a-z]{3}"/>
                            </xs:restriction>
                        </xs:simpleType>
                    </xs:attribute>
                </xs:extension>
            </xs:simpleContent>
        </xs:complexType>
    </xsl:template>


    <xsl:template name="PrintHeader">
        
        <xs:element name="Header">
            <xs:complexType>
                <xs:sequence>
                    <xs:element name="Identifier" type="cmd:complextype-UniqueIdentifier" minOccurs="1" maxOccurs="1" />
                    <xs:element maxOccurs="1" minOccurs="0" type="xs:boolean" name="Ego"/>
                    <xs:element name="Resources">
                        <xs:complexType>
                            <xs:sequence>
                            </xs:sequence>
                        </xs:complexType>
                    </xs:element>
                    <xs:element maxOccurs="unbounded" minOccurs="0" type="xs:anyURI" name="ArchiveLink"/>
                    <xs:element maxOccurs="1" minOccurs="0" type="xs:boolean" name="Visible"/>
                </xs:sequence>
            </xs:complexType>
        </xs:element>
     
    </xsl:template>
</xsl:stylesheet>
