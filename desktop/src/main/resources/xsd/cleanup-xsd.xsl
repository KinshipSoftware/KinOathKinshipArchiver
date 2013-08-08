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
    $Rev: 1772 $
    $Date: 2012-02-24 15:42:25 +0100 (Fri, 24 Feb 2012) $
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:dcr="http://www.isocat.org/ns/dcr"
    xmlns:ann="http://www.clarin.eu">
    <xsl:strip-space elements="*"/>
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no" />

    <xsl:template match="/xs:schema" mode="clean">
        <xs:schema xmlns:cmd="http://www.clarin.eu/cmd/">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="xs:import" mode="clean"/>
            <!-- Remove double entries for named simpleType and complexType definitions at the begin of the XSD.  -->
            <xsl:for-each-group select="./xs:simpleType" group-by="@name">
                <!-- only take the first item -->
                <xsl:copy-of select="current-group( )[1]"/>
            </xsl:for-each-group>

            <xsl:for-each-group select="./xs:complexType" group-by="@name">
                <!-- only take the first item -->
                <xsl:copy-of select="current-group( )[1]"/>
            </xsl:for-each-group>

            <xsl:apply-templates select="xs:element" mode="clean"/>
        </xs:schema>
    </xsl:template>


    <!-- identity copy -->
    <xsl:template match="@*|node()" mode="clean">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="clean"/>
        </xsl:copy>
    </xsl:template>


</xsl:stylesheet>
