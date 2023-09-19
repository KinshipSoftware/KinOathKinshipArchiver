<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics

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
<!--
    $Rev: 484 $
    $Date: 2011-05-13 17:08:40 +0200 (Fri, 13 May 2011) $
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0">

    <xsl:template name="PrintHeaderType">
        <xs:simpleType name="Resourcetype_simple">
            <xs:restriction base="xs:string">
                <xs:enumeration value="Metadata">
                    <xs:annotation>
                        <xs:documentation>The ResourceProxy
                            refers to another component
                            metadata instance (e.g. for
                            grouping metadata descriptions
                            into
                            collections)</xs:documentation>
                    </xs:annotation>
                </xs:enumeration>
                <xs:enumeration value="Resource">
                    <xs:annotation>
                        <xs:documentation>The ResourceProxy
                            refers to a file that is not a
                            metadata instance (e.g. a text
                            document)</xs:documentation>
                    </xs:annotation>
                </xs:enumeration>
            </xs:restriction>
        </xs:simpleType>
    </xsl:template>


    <xsl:template name="PrintHeader">

        <xs:element name="Header">
            <xs:complexType>
                <xs:sequence>
                    <xs:element name="MdCreator" type="xs:string" minOccurs="0"/>
                    <xs:element name="MdCreationDate" type="xs:date" minOccurs="0"/>
                    <xs:element name="MdSelfLink" type="xs:anyURI" minOccurs="0"/>
                    <xs:element name="MdProfile" type="xs:anyURI" minOccurs="0"/>
                    <xs:element name="MdCollectionDisplayName" type="xs:string" minOccurs="0"/>
                </xs:sequence>
            </xs:complexType>
        </xs:element>
        <xs:element name="Resources">
            <xs:complexType>
                <xs:sequence>
                    <xs:element name="ResourceProxyList">
                        <xs:complexType>
                            <xs:sequence>
                                <xs:element maxOccurs="unbounded" minOccurs="0" name="ResourceProxy">
                                    <xs:complexType>
                                        <xs:sequence>
                                            <xs:element maxOccurs="1" minOccurs="1"
                                                name="ResourceType">
                                                <xs:complexType>
                                                    <xs:simpleContent>
                                                        <xs:extension base="cmd:Resourcetype_simple">
                                                            <xs:attribute name="mimetype" type="xs:string"/>
                                                        </xs:extension>
                                                    </xs:simpleContent>
                                                </xs:complexType>
                                            </xs:element>
                                            <xs:element maxOccurs="1" minOccurs="1"
                                                name="ResourceRef" type="xs:anyURI"/>
                                        </xs:sequence>
                                        <xs:attribute name="id" type="xs:ID" use="required"/>
                                    </xs:complexType>
                                </xs:element>
                            </xs:sequence>
                        </xs:complexType>
                    </xs:element>
                    <xs:element name="JournalFileProxyList">
                        <xs:complexType>
                            <xs:sequence>
                                <xs:element maxOccurs="unbounded" minOccurs="0"
                                    name="JournalFileProxy">
                                    <xs:complexType>
                                        <xs:sequence>
                                            <xs:element maxOccurs="1" minOccurs="1"
                                                name="JournalFileRef" type="xs:anyURI"/>
                                        </xs:sequence>
                                    </xs:complexType>
                                </xs:element>
                            </xs:sequence>
                        </xs:complexType>
                    </xs:element>
                    <xs:element name="ResourceRelationList">
                        <xs:complexType>
                            <xs:sequence>
                                <xs:element maxOccurs="unbounded" minOccurs="0"
                                    name="ResourceRelation">
                                    <xs:complexType>
                                        <xs:sequence>
                                            <xs:element maxOccurs="1" minOccurs="1"
                                                name="RelationType"/>
                                            <xs:element maxOccurs="1" minOccurs="1" name="Res1">
                                                <xs:complexType>
                                                  <xs:attribute name="ref" type="xs:IDREF"/>
                                                </xs:complexType>
                                            </xs:element>
                                            <xs:element maxOccurs="1" minOccurs="1" name="Res2">
                                                <xs:complexType>
                                                  <xs:attribute name="ref" type="xs:IDREF"/>
                                                </xs:complexType>
                                            </xs:element>
                                        </xs:sequence>
                                    </xs:complexType>
                                </xs:element>
                            </xs:sequence>
                        </xs:complexType>
                    </xs:element>

                    <xs:element minOccurs="0" name="IsPartOfList">
                        <xs:complexType>
                            <xs:sequence>
                                <xs:element maxOccurs="unbounded" minOccurs="0"
                                    name="IsPartOf" type="xs:anyURI"/>
                            </xs:sequence>
                        </xs:complexType>
                    </xs:element>

                </xs:sequence>
            </xs:complexType>
        </xs:element>
    </xsl:template>
</xsl:stylesheet>
