<?xml version="1.0" encoding="UTF-8"?>
<!--
  XSL stylesheet for wrapping MarcXChange into SRU response
  Version 1, Jörg Prante, 14 Feb 2012
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:srw="http://www.loc.gov/zing/srw/"
                xmlns:srw_mx="info:srw/schema/9/marcxchange"
                xmlns:mx="info:lc/xmlns/marcxchange-v1">
    <xsl:output method="xml" version="1.0" encoding="UTF-8"
                omit-xml-declaration="no" media-type="application/xhtml+xml"        
                doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"
                doctype-public="-//W3C//DTD XHTML 1.1//EN"
                indent="yes"/>
    <xsl:strip-space elements="*"/>
    <xsl:param name="version"/>
    <xsl:param name="numberOfRecords"/>
    <xsl:param name="format"/>
    <xsl:param name="type"/>
    <xsl:template match="/">
        <html>
            <head>MarcXchange Datensatzsicht</head>
            <body>
                <xsl:apply-templates/>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="mx:collection">
        <div>Version 
            <xsl:value-of select="$version"/>
        </div>
        <div>Anzahl Datensätze 
            <xsl:value-of select="$numberOfRecords"/>
        </div>
        <dl>
            <apply-templates/>                    
        </dl>
    </xsl:template>    
    <xsl:template match="mx:record">
        <dt>
            <div class="mx-pos">
                <xsl:value-of select="position()"/>
            </div>
        </dt>
        <dd>
            <table class="mx-table">
                <thead>
                    <tr>
                        <td>Format 
                            <xsl:value-of select="$format"/>
                        </td>
                        <td>Typ 
                            <xsl:value-of select="$type"/>
                        </td>
                    </tr>
                </thead>
                <tbody>
                    <xsl:apply-templates/>
                </tbody>
            </table>            
        </dd>
    </xsl:template>
    <xsl:template match="mx:controlfield">
        <tr class="mx-controlfield">
            <th class="mx-controlfield-header">
                <xsl:value-of select="@tag"/>
            </th>
            <td colspan="2"></td>
            <td>
                <xsl:value-of select="."/>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="mx:datafield">
        <tr class="mx-datafield">
            <th class="mx-datafield-tag">
                <xsl:value-of select="@tag"/>
            </th>
            <td class="mx-datafield-ind1">
                <xsl:value-of select="@ind1"/>
            </td>
            <td class="mx-datafield-ind1">
                <xsl:value-of select="@ind2"/>
            </td>
            <td class="mx-datafield-subfield">
                <xsl:apply-templates select="."/>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="mx:subfield">
        <div>
            <span class="mx-subfield-code">
                <xsl:value-of select="@code"/>             
            </span>
            <span class="mx-subfield-value">
                <xsl:value-of select="."/>
            </span>
        </div>
    </xsl:template>
    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>    
</xsl:stylesheet>
