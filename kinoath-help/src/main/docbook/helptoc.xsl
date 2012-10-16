<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xhtml="http://www.w3.org/1999/xhtml" exclude-result-prefixes="xhtml">

    <!-- 
        This stylesheet processes the ToC result of an XHTML transform of a DocBook manual into a
        list suitable for fast processing in the help system of a desktop application. The result will
        have a structure like the following:
        
        <helpToc>
            <item>
                <file>page1.html</file>
                <name>First page with subitems</name>
                <subitems>
                    <item>
                        <file>page1a.html</file>
                        <name>Subpage of first page</name>
                        <subitems>
                            ...
                        </subitems>
                    </item>
                </subitems>
            </item>
            <item>
                <file>page2.html</file>
                <name>Second page without subitems</name>
            </item>
        </helpToc>
    -->

    <xsl:template match="/">
        <helpToc>
            <!-- Root of the TOC list -->
            <xsl:apply-templates
                select="xhtml:html/xhtml:body/xhtml:div/xhtml:div[@class='toc']/xhtml:dl"/>
        </helpToc>
    </xsl:template>

    <xsl:template match="xhtml:dl">
        <!-- Items list, create items for all <dt> children -->
        <xsl:apply-templates select="xhtml:dt"/>
    </xsl:template>

    <xsl:template match="xhtml:dt">
        <!-- First element of TOC item -->
        <item>
            <xsl:apply-templates select="xhtml:span/xhtml:a"/>
            <!-- If directly following sibling is <dd>, add subitems -->
            <xsl:apply-templates select="following-sibling::*[1][self::xhtml:dd]"/>
        </item>
    </xsl:template>

    <xsl:template match="xhtml:a">
        <!-- Create item elements -->
        <file>
            <xsl:value-of select="@href"/>
        </file>
        <name>
            <xsl:value-of select="normalize-space(.)"/>
        </name>
    </xsl:template>

    <xsl:template match="xhtml:dd">
        <!-- Create subitem list -->
        <subitems>
            <xsl:apply-templates select="xhtml:dl"/>
        </subitems>
    </xsl:template>

    <xsl:template match="*"/>

</xsl:stylesheet>
