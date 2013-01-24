<?xml version="1.0" encoding="UTF-8"?>
<!--
  XSL stylesheet for transforming Elasticsearch into SRU MODS
  Version 1, Jörg Prante, 4 Feb 2012
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:atom="http://www.w3.org/2005/Atom" 
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:dcam="http://purl.org/dc/dcam/" 
                xmlns:dcmitype="http://purl.org/dc/dcmitype/" 
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:marcrel="http://www.loc.gov/loc.terms/relators/"
                xmlns:srw="http://www.loc.gov/zing/srw/"
                xmlns:srw_mods="info:srw/schema/1/mods-v3.4"                
                xmlns:bib="info:srw/cql-context-set/1/bib-v1/"
                xmlns:xbib="http://xbib.org/elements/"
                xmlns:es="http://elasticsearch.org/"
>
    <xsl:output method="xml" omit-xml-declaration="no"/>
    <xsl:strip-space elements="*"/>
    <xsl:include href="es-mods.xsl"/>
    <xsl:template match="/">
        <srw:searchRetrieveResponse
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
            xsi:schemaLocation="http://www.loc.gov/zing/srw/ http://index.hbz-nrw.de/query/schemas/srw-types.xsd" 
            xmlns:srw="http://www.loc.gov/zing/srw/"
            xmlns:srw_mods="info:srw/schema/1/mods-v3.4">
            <srw:version>1.2</srw:version>
            <xsl:choose>
                <xsl:when test="es:source">
                    <srw:numberOfRecords>1</srw:numberOfRecords>
                    <srw:records>
                        <srw:record>
                            <srw:recordSchema>info:srw/schema/1/mods-v3.4</srw:recordSchema>
                            <srw:recordPacking>xml</srw:recordPacking>
                            <srw:recordData>
                                <srw_mods:mods>
                                    <mods:mods version="3.4"
                                               xmlns:mods="http://www.loc.gov/mods/v3"
                                               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                               xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd">
                                        <xsl:apply-templates select="es:source"/>
                                    </mods:mods>                               
                                </srw_mods:mods>
                            </srw:recordData>
                            <srw:recordPosition>1</srw:recordPosition>
                            <srw:recordIdentifier>
                                <xsl:value-of select="es:index"/>
                                <xsl:value-of select="string('/')"/>
                                <xsl:value-of select="es:type"/>
                                <xsl:value-of select="string('/')"/>
                                <xsl:value-of select="es:id"/>
                            </srw:recordIdentifier>
                        </srw:record>
                    </srw:records>
                </xsl:when>
                <xsl:otherwise>
                    <srw:numberOfRecords>
                        <xsl:value-of select="/es:result/es:hits/es:total"/>    
                    </srw:numberOfRecords>
                    <xsl:apply-templates select="/es:result/es:hits/es:total"/>
                </xsl:otherwise>
            </xsl:choose>
        </srw:searchRetrieveResponse>
    </xsl:template>
    <xsl:template match="/es:result/es:hits/es:total">
        <srw:records>
            <xsl:for-each select="../es:hits">
                <srw:record>
                    <srw:recordSchema>info:srw/schema/1/mods-v3.4</srw:recordSchema>
                    <srw:recordPacking>xml</srw:recordPacking>
                    <srw:recordData>
                        <srw_mods:mods>
                            <mods:mods version="3.4"
                                   xmlns:mods="http://www.loc.gov/mods/v3"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                   xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd">
                                <xsl:apply-templates select="es:source"/>
                            </mods:mods>
                        </srw_mods:mods>
                    </srw:recordData>
                    <srw:recordPosition>
                        <xsl:value-of select="position()"/>
                    </srw:recordPosition>
                    <srw:recordIdentifier>
                        <xsl:value-of select="es:index"/><xsl:value-of select="string('/')"/><xsl:value-of select="es:type"/><xsl:value-of select="string('/')"/><xsl:value-of select="es:id"/>
                    </srw:recordIdentifier>
                </srw:record>
            </xsl:for-each>
        </srw:records>
    </xsl:template>        
</xsl:stylesheet>
