<?xml version="1.0" encoding="ISO-8859-1"?>
<!--

    Copyright (C) 2012 The Language Archive

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

-->
<!-- Author: Peter Wither for use in KinOath 2012/03/14 -->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <xsl:strip-space elements="*"/>
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />

    <xsl:template match="@*|node()">
    <!-- by default copy all nodes and attributes -->
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="xs:element[@name='CMD']">
    <!-- remove the CMD element and insert the Kinnate and sub elements -->
        <xs:element name="Kinnate">
            <xs:complexType>
                <xs:sequence>
                    <xs:element name="Entity">
                        <xs:complexType>
                            <xs:sequence>
                                <xs:element name="Identifier" type="cmd:complextype-UniqueIdentifier" minOccurs="1" maxOccurs="1"/>
                                <xs:element maxOccurs="1" minOccurs="0" type="xs:boolean" name="Ego"/>
                                <xs:element maxOccurs="1" minOccurs="0" type="xs:boolean" name="Visible"/>
                                <xs:element maxOccurs="unbounded" minOccurs="0" type="xs:string" name="Label"/>
                                <xs:element name="Relations">
                                    <xs:complexType>
                                        <xs:sequence>
                                            <xs:element name="Relation" maxOccurs="unbounded" minOccurs="0">
                                                <xs:complexType>
                                                    <xs:sequence>
                                                        <xs:element name="Identifier" type="cmd:complextype-UniqueIdentifier" minOccurs="1" maxOccurs="1"/>
                                                    </xs:sequence>
                                                    <xs:attribute name="Line" type="xs:string" use="required"/>
                                                    <xs:attribute name="Type" type="xs:string" use="required"/>
                                                </xs:complexType>
                                            </xs:element>
                                        </xs:sequence>
                                    </xs:complexType>
                                </xs:element>
                                <xs:element maxOccurs="unbounded" minOccurs="0" type="xs:anyURI" name="ExternalLink"/>
                            </xs:sequence>
                        </xs:complexType>
                    </xs:element>
                    <xs:element name="CustomData">
                        <!-- insert the components subelements -->
                        <xsl:for-each select="*/*/xs:element[@name='Components']/*/*/*">
                            <xsl:apply-templates select="node()" />
                        </xsl:for-each>
                    </xs:element>
                </xs:sequence>
                <!-- insert the kimdi version attribute -->
                <xs:attribute name="KMDIVersion" fixed="1.1" use="required"/>
                <!-- <xsl:apply-templates select="xs:attribute[@name='CMDVersion']"/> -->
            </xs:complexType>
        </xs:element>
    </xsl:template>

    <xsl:template match="xs:simpleType[@name='Resourcetype_simple']">
    <!-- remove the unused types and insert the unique identifier type -->
        <xs:simpleType name="simpletype-UniqueIdentifier">
            <xs:restriction base="xs:string">
                <xs:pattern value="[0-9A-F]8-[0-9A-F]4-[0-9A-F]9-[0-9A-F]12"/>
            </xs:restriction>
        </xs:simpleType>
        <xs:complexType name="complextype-UniqueIdentifier">
            <xs:simpleContent>
                <xs:extension base="cmd:simpletype-UniqueIdentifier">
                    <xs:attribute name="type" use="required">
                        <xs:simpleType>
                            <xs:restriction base="xs:string">
                                <xs:pattern value="[a-z]3"/>
                            </xs:restriction>
                        </xs:simpleType>
                    </xs:attribute>
                </xs:extension>
            </xs:simpleContent>
        </xs:complexType>
    </xsl:template>
    
</xsl:stylesheet>