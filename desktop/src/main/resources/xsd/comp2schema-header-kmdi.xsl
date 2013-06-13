<?xml version="1.0" encoding="UTF-8"?>
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
<!--
    This file is based on:
    http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema-header.xsl
    $Rev: 484 $
    $Date: 2011-05-13 17:08:40 +0200 (Fri, 13 May 2011) $

    By Peter Wither for use in KinOath 2011/11/17
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
                    <xs:attribute name="type" use="required">
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
        <xs:element name="Entity">
            <xs:complexType>
                <xs:sequence>
                    <xs:element name="Identifier" type="cmd:complextype-UniqueIdentifier" minOccurs="1" maxOccurs="1" />
                    <xs:element maxOccurs="1" minOccurs="0" type="xs:date" name="DateOfBirth"/>
                    <xs:element maxOccurs="1" minOccurs="0" type="xs:date" name="DateOfDeath"/>
                    <xs:element maxOccurs="1" minOccurs="0" type="xs:boolean" name="Ego"/>
                    <xs:element maxOccurs="1" minOccurs="0" type="xs:boolean" name="Visible"/>
                    <xs:element maxOccurs="unbounded" minOccurs="0" type="xs:string" name="Label"/>
                    <xs:element name="Relations">
                        <xs:complexType>
                            <xs:sequence>
                                <xs:element name="Relation" maxOccurs="unbounded" minOccurs="0">
                                    <xs:complexType>
                                        <xs:sequence>
                                            <xs:element name="Identifier" type="cmd:complextype-UniqueIdentifier" minOccurs="1" maxOccurs="1" />
                                        </xs:sequence>
                                        <xs:attribute name="Line" type="xs:string" use="required" />
                                        <xs:attribute name="Type" type="xs:string" use="required" />
                                    </xs:complexType>
                                </xs:element>
                            </xs:sequence>
                        </xs:complexType>
                    </xs:element>
                    <xs:element maxOccurs="unbounded" minOccurs="0" type="xs:anyURI" name="ExternalLink"/>
                </xs:sequence>
            </xs:complexType>
        </xs:element>
    </xsl:template>
</xsl:stylesheet>
