<?xml version="1.0" encoding="UTF-8"?>
<!--
  XSL stylesheet for transforming Elasticsearch XML result into MODS
  Jörg Prante, 4 Feb 2012
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
                xmlns:bib="info:srw/cql-context-set/1/bib-v1/"
                xmlns:xbib="http://xbib.org/elements/"
                xmlns:es="http://elasticsearch.org/"
                >

    <xsl:variable name="lcletters">abcdefghijklmnopqrstuvwxyz</xsl:variable>
    <xsl:variable name="ucletters">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
    <xsl:template match="es:source">
        <mods:recordInfo>
            <mods:recordIdentifier>
                <xsl:apply-templates select="dc:identifier/xbib:identifierAuthorityMARC"/>
                <xsl:apply-templates select="dc:identifier/xbib:identifierAuthorityMAB"/>
            </mods:recordIdentifier>
        </mods:recordInfo>
        <xsl:apply-templates select="dc:contributor/bib:namePersonal"/>
        <xsl:apply-templates select="dc:contributor/bib:nameCorporate"/>
        <mods:titleInfo>
            <xsl:for-each select="dc:title">
                <xsl:apply-templates select="xbib:title"/>
                <xsl:apply-templates select="xbib:titleSub"/>
                <xsl:apply-templates select="xbib:titlePart"/>
            </xsl:for-each>
        </mods:titleInfo>
        <xsl:apply-templates select="dc:title/bib:titleUniform"/>
        <xsl:apply-templates select="dc:title/bib:titleAlternative"/>
        <xsl:apply-templates select="dc:title/dcterms:alternative"/>
        <mods:originInfo>
            <xsl:apply-templates select="dc:publisher/xbib:publisherName"/>
            <xsl:apply-templates select="dc:publisher/xbib:publisherPlace"/>
            <xsl:apply-templates select="dc:date/dcterms:issued"/>
            <xsl:apply-templates select="dc:description/bib:edition"/>
            <xsl:apply-templates select="dc:type/bib:issuance"/>
        </mods:originInfo>
        <xsl:for-each select="dc:identifier">
            <xsl:apply-templates/>
        </xsl:for-each>           
        <xsl:apply-templates select="../es:score"/>
        <xsl:apply-templates select="dc:language/xbib:languageISO6392b"/>
        <xsl:apply-templates select="dc:format/dcterms:format"/>
        <xsl:apply-templates select="dc:format/dcterms:medium"/>
        <xsl:apply-templates select="dc:type/dcterms:mediaType"/>
        <xsl:apply-templates select="dc:type/bib:genre"/>
        <xsl:apply-templates select="dc:type/xbib:recordType"/>
        <xsl:apply-templates select="dc:description/dcterms:extent"/>
        <xsl:apply-templates select="dc:description/xbib:creatorDescription"/>
        <xsl:for-each select="dc:subject">
            <xsl:choose>
                <xsl:when test="name(*[1])='xbib:rswk'">
                    <mods:subject authority="rswk">
                        <xsl:for-each select="child::*">
                            <xsl:apply-templates/>
                        </xsl:for-each>
                    </mods:subject>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
        <xsl:for-each select="dc:source">
            <xsl:variable name="footnote" select="xbib:source"/>
            <xsl:choose>
                <xsl:when test="string($footnote)">
                    <xsl:apply-templates select="xbib:source"/>
                </xsl:when>
                <xsl:otherwise>
                    <mods:relatedItem type="host">
                        <mods:titleInfo>
                            <xsl:apply-templates select="xbib:sourceTitleWhole"/>
                            <xsl:apply-templates select="xbib:sourceTitle"/>
                            <xsl:apply-templates select="xbib:sourceTitleSub"/>
                        </mods:titleInfo>
                        <mods:part>
                            <xsl:apply-templates select="xbib:sourceDescription"/>
                            <xsl:apply-templates select="xbib:sourceDescriptionVolume"/>
                            <xsl:apply-templates select="xbib:sourceEdition"/>
                            <xsl:apply-templates select="xbib:sourceDateIssued"/>
                            <xsl:apply-templates select="xbib:sourcePublisherPlace"/>
                        </mods:part>
                        <xsl:apply-templates select="xbib:sourceID"/>
                    </mods:relatedItem>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
        <xsl:for-each select="dc:relation/xbib:relationName">
            <xsl:variable name="name" select="."/>
            <xsl:variable name="value" select="following-sibling::*[name()='xbib:relationValue']"/>
            <xsl:variable name="label" select="following-sibling::*[name()='xbib:relationLabel']"/>
            <xsl:choose>
                <xsl:when test="starts-with($value, 'http')">
                    <mods:relatedItem>
                        <mods:location>
                            <xsl:element name="url" namespace="http://www.loc.gov/mods/v3">
                                <xsl:attribute name="usage">primary display</xsl:attribute>
                                <xsl:attribute name="access">raw object</xsl:attribute>
                                <xsl:if test="string($name)">
                                    <xsl:attribute name="note">
                                        <xsl:value-of select="string($name)"/>
                                    </xsl:attribute>
                                </xsl:if>
                                <xsl:if test="string($label)">
                                    <xsl:attribute name="displayLabel">
                                        <xsl:value-of select="string($label)"/>
                                    </xsl:attribute>
                                </xsl:if>
                                <xsl:value-of select="$value"/>
                            </xsl:element>
                        </mods:location>
                    </mods:relatedItem>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="dc:contributor/bib:namePersonal">
        <mods:name type="personal">
            <mods:namePart>
                <xsl:apply-templates/>
            </mods:namePart>
            <xsl:variable name="role" select="following-sibling::*[name()='xbib:namePersonalRole']"/>
            <xsl:if test="string($role)">
                <mods:role>
                    <xsl:choose>
                        <xsl:when test="starts-with($role, 'http://www.loc.gov/loc.terms/relators/')">
                            <mods:roleTerm type="code" authority="marcrelator">
                                <xsl:value-of select="substring-after($role, 'http://www.loc.gov/loc.terms/relators/')"/>
                            </mods:roleTerm>
                        </xsl:when>
                        <xsl:when test="starts-with($role, 'marcrel:')">
                            <mods:roleTerm type="code" authority="marcrelator">
                                <xsl:value-of select="substring-after($role, 'marcrel:')"/>
                            </mods:roleTerm>
                        </xsl:when>
                        <xsl:otherwise>
                            <mods:roleTerm type="text">
                                <xsl:value-of select="$role"/>
                            </mods:roleTerm>
                        </xsl:otherwise>
                    </xsl:choose>
                </mods:role>
            </xsl:if>
        </mods:name>
    </xsl:template>
    <xsl:template match="dc:contributor/bib:nameCorporate">
        <mods:name type="corporate">
            <mods:namePart>
                <xsl:apply-templates/>
            </mods:namePart>
        </mods:name>
    </xsl:template>
    <xsl:template match="dc:language/xbib:languageISO6392b">
        <mods:language>
            <mods:languageTerm type="code" authority="iso639-2b">
                <xsl:apply-templates/>
            </mods:languageTerm>
        </mods:language>
    </xsl:template>
    <xsl:template match="dc:type/xbib:recordType">
        <xsl:choose>
            <xsl:when test="text()='h'">
                <mods:recordInfo>
                    <mods:recordOrigin>MAB-Hauptsatz</mods:recordOrigin>
                </mods:recordInfo>
            </xsl:when>
            <xsl:when test="text()='u'">
                <mods:recordInfo>
                    <mods:recordOrigin>MAB-Untersatz</mods:recordOrigin>
                </mods:recordInfo>
            </xsl:when>
            <xsl:otherwise>
                <mods:recordInfo>
                    <mods:recordOrigin>MAB</mods:recordOrigin>
                </mods:recordInfo>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="dc:format/dcterms:format">
        <xsl:choose>
            <xsl:when test="text()='manuscript'">
                <mods:typeOfResource manuscript="yes">text</mods:typeOfResource>
                <mods:physicalDescription>
                    <mods:form authority="mab">
                        <xsl:value-of select="text()"/>
                    </mods:form>
                </mods:physicalDescription>                
            </xsl:when>
            <xsl:when test="text()='print'">
                <mods:typeOfResource>text</mods:typeOfResource>                    
                <mods:physicalDescription>
                    <mods:form authority="mab">
                        <xsl:value-of select="text()"/>
                    </mods:form>
                </mods:physicalDescription>                
            </xsl:when>
            <xsl:otherwise>
                <mods:physicalDescription>
                    <mods:form authority="mab">
                        <xsl:value-of select="text()"/>
                    </mods:form>
                </mods:physicalDescription>                
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="dc:type/dcterms:mediaType">
        <mods:physicalDescription>
            <mods:note type="mediaType">
                <xsl:value-of select="text()"/>
            </mods:note>
        </mods:physicalDescription>
    </xsl:template>
    <xsl:template match="dc:format/dcterms:medium">
        <mods:physicalDescription>
            <mods:note type="medium">
                <xsl:value-of select="text()"/>
            </mods:note>
        </mods:physicalDescription>                
    </xsl:template>
    <xsl:template match="dc:type/bib:genre">
        <mods:genre authority="mab">
            <xsl:value-of select="text()"/>
        </mods:genre>
    </xsl:template>
    <xsl:template match="dc:type/bib:issuance">
        <mods:issuance>
            <xsl:value-of select="text()"/>
        </mods:issuance>
    </xsl:template>
    <xsl:template match="dcterms:issued">
        <mods:dateIssued encoding="iso8601">
            <xsl:apply-templates/>
        </mods:dateIssued>
    </xsl:template>
    <xsl:template match="xbib:title">
        <mods:title>
            <xsl:apply-templates/>
        </mods:title>
    </xsl:template>
    <xsl:template match="xbib:titleSub">
        <mods:subTitle>
            <xsl:apply-templates/>
        </mods:subTitle>
    </xsl:template>
    <xsl:template match="xbib:titlePart">
        <mods:partName>
            <xsl:apply-templates/>
        </mods:partName>
    </xsl:template>
    <xsl:template match="bib:titleUniform">
        <mods:titleInfo type="uniform">
            <mods:title>
                <xsl:apply-templates/>
            </mods:title>
        </mods:titleInfo>
    </xsl:template>
    <xsl:template match="bib:titleAlternative|dcterms:alternative">
        <mods:titleInfo type="alternative">
            <mods:title>
                <xsl:apply-templates/>
            </mods:title>
        </mods:titleInfo>
    </xsl:template>
    <xsl:template match="bib:titleSeries">
        <mods:relatedItem type="series">
            <mods:title>
                <xsl:apply-templates/>
            </mods:title>
        </mods:relatedItem>
    </xsl:template>
    <xsl:template match="xbib:titleWhole">
        <mods:relatedItem type="host">
            <mods:titleInfo>
                <mods:title>
                    <xsl:apply-templates/>
                </mods:title>
            </mods:titleInfo>
        </mods:relatedItem>
    </xsl:template>
    <xsl:template match="dc:publisher/xbib:publisherName">
        <mods:publisher>
            <xsl:apply-templates/>
        </mods:publisher>
    </xsl:template>
    <xsl:template match="dc:publisher/xbib:publisherPlace">
        <mods:place>
            <mods:placeTerm type="text">
                <xsl:apply-templates/>
            </mods:placeTerm>
        </mods:place>
    </xsl:template>
    <xsl:template match="dc:description/dcterms:extent">
        <mods:physicalDescription>
            <mods:extent>
                <xsl:apply-templates/>
            </mods:extent>
        </mods:physicalDescription>
    </xsl:template>
    <xsl:template match="dc:description/xbib:creatorDescription">
        <mods:note type="triple of responsibility">
            <xsl:apply-templates/>
        </mods:note>
    </xsl:template>
    <xsl:template match="dc:description/bib:edition">
        <mods:edition>
            <xsl:apply-templates/>
        </mods:edition>
    </xsl:template>
    <xsl:template match="dc:identifier/xbib:sysID">
        <mods:identifier type="system">
            <xsl:value-of select="."/>
        </mods:identifier>
    </xsl:template>
    <xsl:template match="*[contains(local-name(),'identifierAuthority')]">
        <xsl:choose>
            <xsl:when test="local-name()='identifierAuthorityMABWhole'">            
                <mods:relatedItem type="host">
                    <mods:identifier type="mab">
                        <xsl:apply-templates/>
                    </mods:identifier>
                </mods:relatedItem>
            </xsl:when>            
            <xsl:when test="not(local-name()='identifierAuthorityISIL')">            
                <xsl:element name="identifier" namespace="http://www.loc.gov/mods/v3">
                    <xsl:attribute name="type">
                        <xsl:value-of select="translate(substring-after(local-name(),'identifierAuthority'),$ucletters,$lcletters)"/>
                    </xsl:attribute>
                    <xsl:apply-templates/>
                </xsl:element>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="dc:subject/xbib:subjectID">
        <mods:subject>
            <mods:name type="ID">
                <xsl:value-of select="."/>
            </mods:name>
        </mods:subject>
    </xsl:template>
    <xsl:template match="*[contains(local-name(),'subjectName')]">
        <xsl:element name="subject" namespace="http://www.loc.gov/mods/v3">
            <xsl:element name="topic" namespace="http://www.loc.gov/mods/v3">
                <xsl:apply-templates/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
    <xsl:template match="*[contains(local-name(),'subjectAuthority')]">
        <xsl:element name="subject" namespace="http://www.loc.gov/mods/v3">
            <xsl:attribute name="authority">
                <xsl:value-of select="translate(substring-after(local-name(),'subjectAuthority'),$ucletters,$lcletters)"/>
            </xsl:attribute>
            <xsl:element name="topic" namespace="http://www.loc.gov/mods/v3">
                <xsl:apply-templates/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
    <xsl:template match="dc:subject/xbib:rswk/xbib:subjectPerson">
        <xsl:variable name="gnd" select="following-sibling::*[name()='xbib:hasGND']"/>
        <xsl:variable name="subjid" select="following-sibling::*[name()='xbib:subjectID']"/>
        <xsl:element name="name" namespace="http://www.loc.gov/mods/v3">
            <xsl:attribute name="type">personal</xsl:attribute>
            <xsl:choose>
                <xsl:when test="string($gnd)">
                    <xsl:attribute name="authority">gnd</xsl:attribute>
                    <xsl:attribute name="authorityURI">http://d-nb.info/gnd/</xsl:attribute>
                    <xsl:attribute name="valueURI">
                        <xsl:value-of select="concat('http://d-nb.info/gnd/',$gnd)"/>
                    </xsl:attribute>
                </xsl:when>
            </xsl:choose>
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="dc:subject/xbib:rswk/xbib:subjectCorporate">
        <xsl:variable name="gnd" select="following-sibling::*[name()='xbib:hasGND']"/>
        <xsl:variable name="subjid" select="following-sibling::*[name()='xbib:subjectID']"/>
        <xsl:element name="name" namespace="http://www.loc.gov/mods/v3">
            <xsl:attribute name="type">corporate</xsl:attribute>
            <xsl:choose>
                <xsl:when test="string($gnd)">
                    <xsl:attribute name="authority">gnd</xsl:attribute>
                    <xsl:attribute name="authorityURI">http://d-nb.info/gnd/</xsl:attribute>
                    <xsl:attribute name="valueURI">
                        <xsl:value-of select="concat('http://d-nb.info/gnd/',$gnd)"/>
                    </xsl:attribute>
                </xsl:when>
            </xsl:choose>
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="dc:subject/xbib:rswk/xbib:subjectTopic">
        <xsl:variable name="gnd" select="following-sibling::*[name()='xbib:hasGND']"/>
        <xsl:variable name="subjid" select="following-sibling::*[name()='xbib:subjectID']"/>
        <xsl:element name="topic" namespace="http://www.loc.gov/mods/v3">
            <xsl:choose>
                <xsl:when test="string($gnd)">
                    <xsl:attribute name="authority">gnd</xsl:attribute>
                    <xsl:attribute name="authorityURI">http://d-nb.info/gnd/</xsl:attribute>
                    <xsl:attribute name="valueURI">
                        <xsl:value-of select="concat('http://d-nb.info/gnd/',$gnd)"/>
                    </xsl:attribute>
                </xsl:when>
            </xsl:choose>
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="dc:subject/xbib:rswk/xbib:subjectSub">
        <mods:topic>
            <xsl:value-of select="."/>
        </mods:topic>
    </xsl:template>
    <xsl:template match="dc:subject/xbib:rswk/xbib:subjectTitle">
        <mods:titleInfo>
            <xsl:value-of select="."/>
        </mods:titleInfo>
    </xsl:template>
    <xsl:template match="dc:subject/xbib:rswk/xbib:subjectSpatial">
        <mods:geographic>
            <xsl:value-of select="."/>
        </mods:geographic>
    </xsl:template>
    <xsl:template match="dc:subject/xbib:rswk/xbib:subjectTemporal">
        <mods:temporal>
            <xsl:value-of select="."/>
        </mods:temporal>
    </xsl:template>
    <xsl:template match="dc:subject/xbib:rswk/xbib:subjectGenre">
        <mods:genre>
            <xsl:value-of select="."/>
        </mods:genre>
    </xsl:template>
    <xsl:template match="dc:subject/xbib:rswk/xbib:subjectID">
        <mods:name type="ID">
            <xsl:value-of select="."/>
        </mods:name>
    </xsl:template>
    <xsl:template match="dc:subject/xbib:rswk/xbib:hasGND">
    </xsl:template>
    <xsl:template match="dc:source/xbib:source">
        <mods:physicalDescription>
            <mods:note type="general note">
                <xsl:apply-templates/>
            </mods:note>
        </mods:physicalDescription>
    </xsl:template>
    <xsl:template match="dc:source/xbib:sourceTitleWhole">
        <mods:title>
            <xsl:apply-templates/>
        </mods:title>
    </xsl:template>
    <xsl:template match="dc:source/xbib:sourceTitle">
        <mods:title>
            <xsl:apply-templates/>
        </mods:title>
    </xsl:template>
    <xsl:template match="dc:source/xbib:sourceTitleSub">
        <mods:subtitle>
            <xsl:apply-templates/>
        </mods:subtitle>
    </xsl:template>
    <xsl:template match="dc:source/xbib:sourceDateIssued">
        <mods:date>
            <xsl:apply-templates/>
        </mods:date>
    </xsl:template>
    <xsl:template match="dc:source/xbib:sourceDescription">
        <mods:text>
            <xsl:apply-templates/>
        </mods:text>
    </xsl:template>
    <xsl:template match="dc:source/xbib:sourceDescriptionVolume">
        <mods:detail type="volume">
            <xsl:apply-templates/>
        </mods:detail>
    </xsl:template>
    <xsl:template match="dc:source/xbib:sourceEdition">
        <mods:detail type="issue">
            <xsl:apply-templates/>
        </mods:detail>
    </xsl:template>
    <xsl:template match="dc:source/xbib:sourceID">
        <mods:identifier>
            <xsl:apply-templates/>
        </mods:identifier>
    </xsl:template>
    <xsl:template match="dc:source/xbib:sourcePublisherPlace">
        <mods:text>
            <xsl:apply-templates/>
        </mods:text>
    </xsl:template>
    <xsl:template match="es:score">
        <xsl:if test="text()!='null'">
            <mods:identifier authority="es-score">
                <xsl:value-of select="."/>    
            </mods:identifier>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
