<?xml version="1.0" encoding="UTF-8"?>
<!--
  XSL stylesheet for transforming Elasticsearch into SearchRetrieve 1.0 MODS
  Version 2.00, Jörg Prante, 28 May 2013
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:sru="http://docs.oasis-open.org/ns/search-ws/sruResponse"
                xmlns:facet="http://docs.oasis-open.org/ns/search-ws/facetedResults"
                xmlns:sru_mods="info:srw/schema/1/mods-v3.4"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:bib="info:srw/cql-context-set/1/bib-v1/"
                xmlns:xbib="http://xbib.org/elements/"
                xmlns:es="http://elasticsearch.org/"
>
    <xsl:output method="xml" omit-xml-declaration="no"/>
    <xsl:strip-space elements="*"/>
    <xsl:param name="facets"/>
    <xsl:include href="es-mods.xsl"/>
    <xsl:template match="/">
        <sru:searchRetrieveResponse
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://docs.oasis-open.org/ns/search-ws/sruResponse http://docs.oasis-open.org/search-ws/searchRetrieve/v1.0/os/schemas/sruResponse.xsd"
            xmlns:sru="http://docs.oasis-open.org/ns/search-ws/sruResponse"
            xmlns:sru_mods="info:srw/schema/1/mods-v3.4">
            <sru:version>2.0</sru:version>
            <xsl:choose>
                <xsl:when test="es:source">
                    <sru:numberOfRecords>1</sru:numberOfRecords>
                    <sru:records>
                        <sru:record>
                            <sru:recordSchema>info:srw/schema/1/mods-v3.4</sru:recordSchema>
                            <sru:recordPacking>xml</sru:recordPacking>
                            <sru:recordData>
                                <sru_mods:mods>
                                    <mods:mods version="3.4"
                                               xmlns:mods="http://www.loc.gov/mods/v3"
                                               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                               xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd">
                                        <xsl:apply-templates select="es:source"/>
                                    </mods:mods>                               
                                </sru_mods:mods>
                            </sru:recordData>
                            <sru:recordPosition>1</sru:recordPosition>
                        </sru:record>
                    </sru:records>
                </xsl:when>
                <xsl:otherwise>
                    <sru:numberOfRecords>
                        <xsl:value-of select="/es:result/es:hits/es:total"/>    
                    </sru:numberOfRecords>
                    <xsl:apply-templates select="/es:result/es:hits/es:total"/>
                </xsl:otherwise>
            </xsl:choose>
        </sru:searchRetrieveResponse>
    </xsl:template>
    <xsl:template match="/es:result/es:hits/es:total">
        <sru:records>
            <xsl:for-each select="../es:hits/es:hits">
                <sru:record>
                    <sru:recordSchema>info:srw/schema/1/mods-v3.4</sru:recordSchema>
                    <sru:recordPacking>xml</sru:recordPacking>
                    <sru:recordData>
                        <sru_mods:mods>
                            <mods:mods version="3.4"
                                   xmlns:mods="http://www.loc.gov/mods/v3"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                   xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd">
                                <xsl:apply-templates select="es:source"/>
                            </mods:mods>
                        </sru_mods:mods>
                    </sru:recordData>
                    <sru:recordPosition>
                        <xsl:value-of select="position()"/>
                    </sru:recordPosition>
                </sru:record>
            </xsl:for-each>
        </sru:records>
        <facet:facetedResult xmlns:facet="http://docs.oasis-open.org/ns/search-ws/sru-facetedResults" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://docs.oasis-open.org/ns/search-ws/sru-facetedResults http://docs.oasis-open.org/search-ws/searchRetrieve/v1.0/os/schemas/facetedResults.xsd">
             <xsl:copy-of select="$facets"/>
        </facet:facetedResult>
    </xsl:template>
</xsl:stylesheet>
