<?xml version='1.0'?> 
<xsl:stylesheet  
       xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0"> 
 
  <!-- Borders and shading are added to all admonitions (note, caution, warning, tip, and important): -->
  <xsl:attribute-set name="admonition.properties">
	<xsl:attribute name="border">0.5pt solid black</xsl:attribute>
	<xsl:attribute name="background-color">#E0E0E0</xsl:attribute>
	<xsl:attribute name="padding">0.1in</xsl:attribute>
  </xsl:attribute-set>

  <xsl:param name="admon.graphics" select="1"/>
  <xsl:param name="admon.graphics.path">images/</xsl:param> 
  
  <!-- -->
  <xsl:param name="formal.title.placement">
	figure after
	example before
	equation after
	table before
	procedure before
  </xsl:param>

  <xsl:template name="next.itemsymbol">
	<xsl:param name="itemsymbol" select="'default'"/>
	<xsl:choose>
	  <!-- Change this list if you want to change the order of symbols -->
	  <xsl:when test="$itemsymbol = 'disc'">endash</xsl:when>
	  <xsl:when test="$itemsymbol = 'endash'">emdash</xsl:when>
	  <xsl:otherwise>disc</xsl:otherwise>
	</xsl:choose>
  </xsl:template>

  <xsl:param name="xref.with.number.and.title" select="0"/>
  

  <xsl:template match="section[@role = 'NotInToc']"  mode="toc" />

</xsl:stylesheet>
