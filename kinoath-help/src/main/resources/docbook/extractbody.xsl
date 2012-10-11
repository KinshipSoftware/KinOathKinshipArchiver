<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xhtml="http://www.w3.org/1999/xhtml" exclude-result-prefixes="xhtml"
		version="1.0">
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:template match="/xhtml:html">
	<html>
	    <head>
		<style type="text/css">
			h1, h2 { text-align: center; font-size:125%; background: #ede2cd; margin:0; padding: 5px; } 
			body { color:Black; font-family:Arial,Helvetica,sans-serif; font-size:100%; font-size-adjust:none; font-style:normal; font-variant:normal; font-weight:normal; line-height:1.25em; background: #F4F1EB; padding-bottom: 10px; } 
			p {padding-left: 5px; padding-right: 5px; }
		</style>
	    </head>
	    <xsl:copy-of select="xhtml:body" />
	</html>
    </xsl:template>
    <xsl:template match="*"/>
</xsl:stylesheet>