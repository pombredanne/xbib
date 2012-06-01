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
package org.xbib.tools.oai;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import org.xbib.builders.elasticsearch.ElasticsearchResourceOutput;
import org.xbib.io.EmptyWriter;
import org.xbib.io.util.DateUtil;
import org.xbib.oai.ListRecordsRequest;
import org.xbib.oai.ListRecordsResponse;
import org.xbib.oai.MetadataReader;
import org.xbib.oai.ResumptionToken;
import org.xbib.oai.client.OAIClient;
import org.xbib.oai.client.OAIClientFactory;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import static org.xbib.tools.opt.util.DateConverter.*;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.io.RdfXmlReader;
import org.xbib.rdf.io.StatementListener;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public final class ElasticsearchOAIHarvester {

    private Resource resource;

    public static void main(String[] args) throws Exception {
        OptionParser parser = new OptionParser() {

            {
                accepts("index").withRequiredArg().ofType(String.class).required();
                accepts("type").withRequiredArg().ofType(String.class).required();
                accepts("server").withRequiredArg().ofType(String.class).required();
                accepts("set").withRequiredArg().ofType(String.class);
                accepts("from").withRequiredArg().withValuesConvertedBy(datePattern("yyyy-MM-dd'T'hh:mm:ss'Z'"));
                accepts("until").withRequiredArg().withValuesConvertedBy(datePattern("yyyy-MM-dd'T'hh:mm:ss'Z'")).defaultsTo(new Date());
            }
        };
        new ElasticsearchOAIHarvester().harvest(parser.parse(args));
    }

    private void setResource(Resource resource) {
        this.resource = resource;
    }

    private Resource getResource() {
        return resource;
    }

    private void harvest(final OptionSet options) throws Exception {
        if (options == null) {
            throw new IllegalArgumentException("no options");
        }
        final ElasticsearchResourceOutput es = new ElasticsearchResourceOutput();
        es.connect(options.valueOf("index").toString(), options.valueOf("type").toString());
        final OAIClient client = OAIClientFactory.getClient(options.valueOf("server").toString());
        final ListRecordsRequest request = new OAIListRecordsRequest(client.getURI(), options);
        StylesheetTransformer transformer = new StylesheetTransformer("src/main/resources/xsl");
        do {
            EmptyWriter w = new EmptyWriter();
            ListRecordsResponse response = new ListRecordsResponse(w);
            client.setStylesheetTransformer(transformer);
            //client.setProxy("localhost", 3128);
            RdfXmlReader reader = new RdfXmlReader();
            final StatementListener stmt = new StatementListener() {

                @Override
                public void newIdentifier(URI uri) {
                    getResource().setIdentifier(uri);
                }

                @Override
                public void statement(Statement statement) {
                    getResource().add(statement);
                }
            };
            reader.setListener(stmt);
            final DefaultHandler handler = reader.getHandler();
            MetadataReader metadataReader = new MetadataReader() {

                @Override
                public void startDocument() throws SAXException {
                    handler.startDocument();
                    setResource(new SimpleResource());
                }

                @Override
                public void endDocument() throws SAXException {
                    handler.endDocument();
                    es.output(null, DateUtil.formatNow());
                }

                @Override
                public void startPrefixMapping(String string, String string1) throws SAXException {
                    handler.startPrefixMapping(string, string1);
                }

                @Override
                public void endPrefixMapping(String string) throws SAXException {
                    handler.endPrefixMapping(string);
                }

                @Override
                public void startElement(String ns, String localname, String string2, Attributes atrbts) throws SAXException {
                    handler.startElement(ns, localname, string2, atrbts);
                }

                @Override
                public void endElement(String ns, String localname, String string2) throws SAXException {
                    handler.endElement(ns, localname, string2);
                }

                @Override
                public void characters(char[] chars, int i, int i1) throws SAXException {
                    handler.characters(chars, i, i1);
                }
            };
            client.setMetadataReader(metadataReader);
            client.prepareListRecords(request, response).execute();
        } while (request.getResumptionToken() != null);
        es.disconnect();
    }

    class OAIListRecordsRequest extends ListRecordsRequest {

        OptionSet options;
        ResumptionToken token;
        Date from;
        Date until;

        OAIListRecordsRequest(URI uri, OptionSet options) {
            super(uri);
            this.options = options;
        }

        public void setFrom(Date from) {
            this.from = from;
        }

        @Override
        public Date getFrom() {
            return (Date) options.valueOf("from");
        }

        public void setUntil(Date until) {
            this.until = until;
        }

        @Override
        public Date getUntil() {
            return (Date) options.valueOf("until");
        }

        @Override
        public String getSet() {
            return (String) options.valueOf("set");
        }

        @Override
        public String getMetadataPrefix() {
            return "RDFxml";
        }

        @Override
        public void setResumptionToken(ResumptionToken token) {
            this.token = token;
        }

        @Override
        public ResumptionToken getResumptionToken() {
            return token;
        }

        @Override
        public String getPath() {
            return null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return null;
        }
    }
}
