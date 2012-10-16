<?xml version='1.0'?> 
<xsl:stylesheet  
       xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0"> 
<!--  <xsl:import href="../html/chunk.xsl"/>  -->
  <xsl:import href="http://docbook.sourceforge.net/release/xsl/current/xhtml/chunkfast.xsl"/> 
  <xsl:param name="chunker.output.encoding" select="'UTF-8'"/>
  <xsl:include href="common-customizations.xsl" />
  <xsl:param name="ignore.image.scaling" select="1" />
  <xsl:param name="chunk.section.depth" select="2"/>
  <xsl:param name="chunk.first.sections" select="1"/>
  <xsl:param name="chunker.output.doctype-public" select="'-//W3C//DTD XHTML 1.0 Transitional//EN'"/>
  <xsl:param name="chunker.output.doctype-system" select="'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'"/>
  <xsl:param name="saxon.character.representation" select="'entity;decimal'" />

  <xsl:param name="suppress.navigation" select="1" /> <!-- no navigation on pages -->
  <xsl:param name="chapter.autolabel" select="1" /> <!-- chapter labels -->
    
  <xsl:param name="base.dir" select="'nl/mpi/arbil/resources/html/help/'" />
  
  <xsl:param name="generate.toc">
  /appendix toc,title
  article/appendix  nop
  /article  toc,title
  book      toc,title<!--,figure,table,example,equation-->
  /chapter  toc,title
  part      toc,title
  /preface  toc,title
  reference toc,title
  /sect1    toc
  /sect2    toc
  /sect3    toc
  /sect4    toc
  /sect5    toc
  /section  toc
  set       toc,title
  </xsl:param>

  <!-- A template to place title between mediaobject and caption... -->  
<xsl:template match="mediaobject|mediaobjectco">

  <xsl:variable name="olist" select="imageobject|imageobjectco
                     |videoobject|audioobject
                     |textobject"/>

  <xsl:variable name="object.index">
    <xsl:call-template name="select.mediaobject.index">
      <xsl:with-param name="olist" select="$olist"/>
      <xsl:with-param name="count" select="1"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="object" select="$olist[position() = $object.index]"/>

  <xsl:variable name="align">
    <xsl:value-of select="$object/descendant::imagedata[@align][1]/@align"/>
  </xsl:variable>

  <div>
    <xsl:apply-templates select="." mode="class.attribute"/>
    <xsl:if test="$align != '' ">
      <xsl:attribute name="align">
        <xsl:value-of select="$align"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:call-template name="anchor"/>

    <xsl:apply-templates select="$object"/>
    <xsl:if test="not(parent::screenshot) and not(parent::figure)">
		<xsl:apply-templates select="caption"/>
	</xsl:if>
  </div>
</xsl:template>
  
<xsl:template name="formal.object.heading">
  <xsl:param name="object" select="."/>
  <xsl:param name="title">
    <xsl:apply-templates select="$object" mode="object.title.markup">
      <xsl:with-param name="allow-anchors" select="1"/>
    </xsl:apply-templates>
  </xsl:param>

  <p class="title">
    <b>
      <xsl:copy-of select="$title"/>
    </b>
  </p>
    <!-- Add mediaobject caption below title -->
  <xsl:if test="self::figure/screenshot/mediaobject/caption">
    <xsl:apply-templates select="screenshot/mediaobject/caption"/>
  </xsl:if>
  <xsl:if test="self::figure/mediaobject/caption">
    <xsl:apply-templates select="mediaobject/caption"/>
  </xsl:if>
</xsl:template>  
  
<xsl:template match="caption">
  <i>
    <xsl:apply-templates/>
  </i>
</xsl:template>  
    
  
  <!-- Additional meta tag info for IE (not needed when using xsltproc) -->
  <xsl:template name="user.head.content">
    <xsl:if test="contains(system-property('xsl:vendor'), 'SAXON') or 
      contains(system-property('xsl:vendor'), 'Apache')">
      <meta xmlns="http://www.w3.org/1999/xhtml" http-equiv="Content-Type"
        content="text/html; charset=UTF-8"/>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>
