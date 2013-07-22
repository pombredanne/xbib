/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.sru.elasticsearch;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.Writer;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.elasticsearch.search.facet.Facets;
import org.w3c.dom.Document;

import org.xbib.elasticsearch.xml.ES;
import org.xbib.elasticsearch.support.facet.FacetSupport;
import org.xbib.facet.Facet;
import org.xbib.facet.FacetListener;
import org.xbib.io.OutputFormat;
import org.xbib.common.io.stream.StreamByteBuffer;
import org.xbib.io.Streams;
import org.xbib.json.transform.JsonStylesheet;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.SRUVersion;
import org.xbib.sru.facet.FacetedResult;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Elasticsearch SRU response.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ElasticsearchSRUResponse extends SearchRetrieveResponse {

    private final Logger logger = LoggerFactory.getLogger(ElasticsearchSRUResponse.class.getName());

    // javax.xml.parsers.DocumentBuilderFactory
    private final static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    private final static QName root = new QName(ES.NS_URI, "result", ES.NS_PREFIX);

    private ElasticsearchSRURequest request;

    private StreamByteBuffer buffer;

    private Facets facets;

    public ElasticsearchSRUResponse(ElasticsearchSRURequest request) {
        super(request);
        this.request = request;
    }

    public ElasticsearchSRUResponse setBuffer(StreamByteBuffer buffer) {
        this.buffer = buffer;
        return this;
    }

    public ElasticsearchSRUResponse setFacets(Facets facets) {
        this.facets = facets;
        return this;
    }

    @Override
    public ElasticsearchSRUResponse to(Writer writer) throws IOException {

        if (facets != null) {
            final FacetedResult facetedResult = new FacetedResult();
            // parse facets to XML string
            FacetListener listener = new FacetListener() {
                @Override
                public void receive(Facet facet) {
                    facetedResult.add(facet);
                }
            };
            FacetSupport responseFacets = new FacetSupport(listener);
            responseFacets.parse(facets);
            String xmlFacets = facetedResult.toXML();

            logger.debug("facets built = {}", xmlFacets);

            // this does not work, but would be sooo nice
            //getTransformer().addParameter("facets", new StreamSource(new StringReader(xmlFacets)));

            // build DOM, pass it to XSL as parameter
            if (xmlFacets != null && xmlFacets.length() > 0) {
                try {
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(new InputSource(new StringReader(xmlFacets)));
                    getTransformer().addParameter("facets", doc.getDocumentElement());
                } catch (SAXException | ParserConfigurationException e) {
                    throw new IOException(e);
                }
            } else {
                logger.warn("no facet variable passed to XSLT");
            }
        } else {
            logger.warn("no facets in response");
        }

        // transport parameters into XSL transformer for style sheets
        if (request != null) {
            getTransformer().addParameter("operation", "searchRetrieve");
            getTransformer().addParameter("version", request.getVersion());
            getTransformer().addParameter("query", request.getQuery());
            getTransformer().addParameter("startRecord", request.getStartRecord());
            getTransformer().addParameter("maximumRecords", request.getMaximumRecords());
            getTransformer().addParameter("recordPacking", request.getRecordPacking());
            getTransformer().addParameter("recordSchema", request.getRecordSchema());
        }

        OutputFormat format = getOutputFormat();
        if (format == null || format.equals(OutputFormat.JSON)) {
            Streams.copy(new InputStreamReader(buffer.getInputStream(), "UTF-8"), writer);
        } else if (format.equals(OutputFormat.XML)) {
            JsonStylesheet js = new JsonStylesheet().root(root);
            js.toXML(buffer.getInputStream(), writer);
        } else {
            // application/sru+xml
            // application/x-xhtml+xml
            // application/x-mods+xml
            SRUVersion version = request != null ?
                    SRUVersion.fromString(request.getVersion()) : SRUVersion.VERSION_2_0;
            String[] stylesheets = getStylesheets(version);
            new JsonStylesheet()
                    .root(root)
                .setTransformer(getTransformer())
                .setStylesheets(stylesheets)
                .transform(buffer.getInputStream(), writer);
        }
        return this;
    }

}
