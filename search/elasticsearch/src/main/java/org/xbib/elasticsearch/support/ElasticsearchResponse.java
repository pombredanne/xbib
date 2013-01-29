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
package org.xbib.elasticsearch.support;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.xbib.elasticsearch.xml.ES;
import org.xbib.json.JsonXmlReader;
import org.xbib.json.JsonXmlStreamer;
import org.xbib.json.JsonXmlValueMode;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.util.XMLEventConsumer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ElasticsearchResponse {

    private SearchResponse searchResponse;
    private GetResponse getResponse;
    private OutputFormat format;
    private String stylesheets;
    private OutputStream target;
    private StylesheetTransformer transformer;
    private XMLEventConsumer consumer;
    private ResourceContext context;

    public ElasticsearchResponse searchResponse(SearchResponse response) {
        this.searchResponse = response;
        return this;
    }

    public ElasticsearchResponse getResponse(GetResponse response) {
        this.getResponse = response;
        return this;
    }

    public ElasticsearchResponse context(ResourceContext context) {
        this.context = context;
        return this;
    }

    public ElasticsearchResponse format(OutputFormat format) {
        this.format = format;
        return this;
    }

    public long tookInMillis() {
        return searchResponse.tookInMillis();
    }

    public long totalHits() {
        return searchResponse.getHits().getTotalHits();
    }

    public boolean exists() {
        return getResponse.exists();
    }

    public ElasticsearchResponse toJson(OutputStream out) throws IOException {
        if (out == null) {
            return this;
        }
        if (searchResponse == null) {
            out.write(jsonErrorMessage("no response yet"));
            return this;
        }
        XContentBuilder jsonBuilder = new XContentBuilder(JsonXContent.jsonXContent, out);
        jsonBuilder.startObject();
        searchResponse.toXContent(jsonBuilder, ToXContent.EMPTY_PARAMS);
        jsonBuilder.endObject();
        jsonBuilder.close();
        return this;
    }


    public ElasticsearchResponse xmlEventConsumer(XMLEventConsumer consumer) throws IOException {
        this.consumer = consumer;
        return this;

    }

    public ElasticsearchResponse styleWith(StylesheetTransformer transformer, String stylesheets, OutputStream target) throws IOException {
        this.transformer = transformer;
        this.stylesheets = stylesheets;
        this.target = target;
        return this;
    }

    public ElasticsearchResponse dispatchTo(Formatter processor) throws IOException {
        if (searchResponse == null) {
            if (processor != null) {
                processor.format(OutputStatus.ERROR, OutputFormat.JSON, jsonErrorMessage("no response yet"));
            }
            return this;
        }
        final boolean error = searchResponse.failedShards() > 0 || searchResponse.isTimedOut();

        // error handling
        if (error) {
            StringBuilder sb = new StringBuilder();
            if (searchResponse.failedShards() > 0) {
                for (ShardSearchFailure shf : searchResponse.getShardFailures()) {
                    sb.append(Integer.toString(shf.shardId())).append("=").append(shf.reason()).append(" ");
                }
            }
            if (processor != null) {
                processor.format(OutputStatus.ERROR, format, jsonErrorMessage(sb.toString()));
            }
            return this;
        }

        // fill bi-diectional buffer with JSON
        StreamByteBuffer buffer = new StreamByteBuffer();
        toJson(buffer.getOutputStream());
        buffer.getOutputStream().flush();

        // stylesheet transformation?
        if (transformer != null && stylesheets != null && target != null) {
            try {
                QName root = new QName(ES.NS_URI, "result", ES.NS_PREFIX); // TODO configure this element
                JsonXmlReader reader = new JsonXmlReader(root);
                SAXSource source = new SAXSource(reader, new InputSource(buffer.getInputStream()));
                String[] styles = stylesheets.split(",");
                if (styles.length == 1) {
                    transformer.setSource(source).setXsl(styles[0]).setTarget(target).apply();
                } else if (styles.length == 2) {
                    transformer.setSource(source).setXsl(styles[0]).setXsl2(styles[1]).setTarget(target).apply();
                } else {
                    throw new IOException("stylesheet error: " + stylesheets);
                }
                return this;
            } catch (TransformerException ex) {
                throw new IOException(ex);
            }
        }
        // XML without stylesheet?
        if (format == OutputFormat.XML && stylesheets == null) {
            try {
                QName root = new QName(ES.NS_URI, "result", ES.NS_PREFIX); // TODO configure this element
                JsonXmlStreamer jsonXml = context != null ?
                        new JsonXmlStreamer(context.namespaceContext(), JsonXmlValueMode.SKIP_EMPTY_VALUES)
                        : new JsonXmlStreamer(JsonXmlValueMode.SKIP_EMPTY_VALUES);
                if (consumer != null) {
                    jsonXml.toXML(buffer.getInputStream(), consumer, root);
                } else if (target != null) {
                    XMLEventWriter events = jsonXml.openWriter(target, "UTF-8");
                    jsonXml.toXML(buffer.getInputStream(), events, root);
                    events.flush();
                }
                return this;
            } catch (XMLStreamException ex) {
                throw new IOException(ex);
            }
        }
        // all other formats
        if (processor != null) {
            processor.format(OutputStatus.OK, format, buffer.readAsByteArray());
        }
        return this;
    }

    public ElasticsearchResponse singleDispatchTo(Formatter processor)
            throws TransformerException, XMLStreamException, IOException {
        if (!getResponse.exists() || getResponse.isSourceEmpty()) {
            if (processor != null) {
                processor.format(OutputStatus.EMPTY, format, jsonEmptyMessage("not found"));
            }
            return this;
        }
        // stylesheet transformation?
        if (transformer != null && stylesheets != null && target != null) {
            QName root = new QName(ES.NS_URI, "source", ES.NS_PREFIX); // TODO configure this element
            JsonXmlReader reader = new JsonXmlReader(root);
            String[] styles = stylesheets.split(",");
            SAXSource source = new SAXSource(reader, new InputSource(new ByteArrayInputStream(getResponse.source())));
            if (styles.length == 1) {
                transformer.setSource(source).setXsl(styles[0]).setTarget(target).apply();
            } else if (styles.length == 2) {
                transformer.setSource(source).setXsl(styles[0]).setXsl2(styles[1]).setTarget(target).apply();
            } else {
                throw new IOException("stylesheet error: " + stylesheets);
            }
            return this;
        }
        if (format == OutputFormat.XML && stylesheets == null) {
            QName root = new QName(ES.NS_URI, "source", ES.NS_PREFIX); // TODO configure this element
            JsonXmlStreamer jsonXml = new JsonXmlStreamer(JsonXmlValueMode.SKIP_EMPTY_VALUES);
            if (consumer != null) {
                jsonXml.toXML(new ByteArrayInputStream(getResponse.source()), consumer, root);
            } else if (target != null) {
                XMLEventWriter events = jsonXml.openWriter(target, "UTF-8");
                jsonXml.toXML(new ByteArrayInputStream(getResponse.source()), events, root);
                events.flush();
            }
            return this;
        }
        // json and other formats
        if (processor != null) {
            processor.format(OutputStatus.OK, format, getResponse.source());
        }
        return this;
    }

    private static byte[] jsonEmptyMessage(String message) {
        return ("{\"error\":404,\"message\":\"" + message + "\"}").getBytes();
    }


    private static byte[] jsonErrorMessage(String message) {
        return ("{\"error\":500,\"message\":\"" + message + "\"}").getBytes();
    }

}
