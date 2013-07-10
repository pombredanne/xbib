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
package org.xbib.tools.indexer.elasticsearch;

import org.xbib.elasticsearch.ElasticsearchResourceSink;
import org.xbib.elasticsearch.support.bulk.transport.MockTransportClientBulk;
import org.xbib.elasticsearch.support.bulk.transport.TransportClientBulk;
import org.xbib.elasticsearch.support.bulk.transport.TransportClientBulkSupport;
import org.xbib.analyzer.output.ElementOutput;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.client.OAIClient;
import org.xbib.oai.client.OAIClientFactory;
import org.xbib.oai.record.ListRecordsRequest;
import org.xbib.oai.record.ListRecordsResponseListener;
import org.xbib.oai.util.MetadataHandler;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.rdfxml.RdfXmlReader;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Date;

import static org.xbib.tools.opt.util.DateConverter.datePattern;

/**
 *
 * OAI indexer for Elasticsearch
 *
 * @author Jörg Prante <joergprante@gmail.com>
 */
public class OAI {

    private final Logger logger = LoggerFactory.getLogger(OAI.class.getName());

    private static OptionSet options;

    private static String index;

    private static String type;

    private OAIClient client;

    private ElementOutput out;

    public static void main(String[] args) throws Exception {
        OptionParser parser = new OptionParser() {
            {
                accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                accepts("index").withRequiredArg().ofType(String.class).required();
                accepts("type").withRequiredArg().ofType(String.class).required();
                accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(4 * Runtime.getRuntime().availableProcessors());
                accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                accepts("server").withRequiredArg().ofType(String.class).required();
                accepts("set").withRequiredArg().ofType(String.class);
                accepts("prefix").withRequiredArg().ofType(String.class);
                accepts("from").withRequiredArg().withValuesConvertedBy(datePattern("yyyy-MM-dd'T'hh:mm:ss'Z'"));
                accepts("until").withRequiredArg().withValuesConvertedBy(datePattern("yyyy-MM-dd'T'hh:mm:ss'Z'")).defaultsTo(new Date());
                accepts("fromDate").withRequiredArg().withValuesConvertedBy(datePattern("yyyy-MM-dd"));
                accepts("untilDate").withRequiredArg().withValuesConvertedBy(datePattern("yyyy-MM-dd")).defaultsTo(new Date());
            }
        };
        options = parser.parse(args);
        URI esURI = URI.create((String)options.valueOf("elasticsearch"));
        index = (String)options.valueOf("index");
        type = (String)options.valueOf("type");
        int maxbulkactions = (Integer) options.valueOf("maxbulkactions");
        int maxconcurrentbulkrequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
        boolean mock = (Boolean)options.valueOf("mock");

        final TransportClientBulk es = mock ?
                new MockTransportClientBulk() :
                new TransportClientBulkSupport();

        es.maxBulkActions(maxbulkactions)
                .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                .newClient(esURI);
        //.waitForHealthyCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));

        final ElasticsearchResourceSink<ResourceContext, Resource> sink =
                new ElasticsearchResourceSink(es);

        String server = (String) options.valueOf("server");
        String prefix = (String) options.valueOf("prefix");
        Date from = options.valueOf("fromDate") != null?
                (Date) options.valueOf("fromDate") :  (Date) options.valueOf("from");
        Date until = options.valueOf("untilDate") != null?
                (Date) options.valueOf("untilDate") :  (Date) options.valueOf("until");

        final OAIClient client = OAIClientFactory.newClient(server);

        // pass options to ListRecords request
        ListRecordsRequest request = client.newListRecordsRequest()
                .setMetadataPrefix(prefix)
                .setFrom(from)
                .setUntil(until);

        new OAI(client, sink).execute(request);

        client.close();

        es.shutdown();
    }

    public OAI(OAIClient client, ElementOutput out) {
        this.client = client;
        this.out = out;
    }

    private void execute(ListRecordsRequest request) throws Exception {
        boolean failure;
        ResourceBuilder builder = new ResourceBuilder();
        RdfXmlReader reader = new RdfXmlReader()
                .setTripleListener(builder);
        MetadataHandler metadataHandler = new MyMetadataHandler(reader.getHandler());
        ListRecordsResponseListener listener = new ListRecordsResponseListener(request);
        listener.register(metadataHandler);
        do {
            try {
                request.prepare().execute(listener).waitFor();
                failure = listener.isFailure();
                if (listener.getResponse() != null) {
                    StringWriter w = new StringWriter();
                    listener.getResponse().to(w);
                }
                request = client.resume(request, listener.getResumptionToken());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                failure = true;
            }
        } while (request != null && request.getResumptionToken() != null && !failure);
    }

    private final SimpleResourceContext resourceContext = new SimpleResourceContext();

    class MyMetadataHandler extends MetadataHandler {

        final XmlHandler handler;

        final IRINamespaceContext context;

        MyMetadataHandler(XmlHandler handler) {
            this.handler = handler;
            context = IRINamespaceContext.newInstance();
            context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
            resourceContext.newNamespaceContext(context);
            resourceContext.newResource();
        }

        @Override
        public void startDocument() throws SAXException {
            handler.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            handler.endDocument();
            String identifier = getHeader().getIdentifier();
            try {
                IRI iri = IRI.builder().scheme("http")
                        .host(index)
                        .query(type)
                        .fragment(identifier).build();
                resourceContext.resource().id(iri);
                out.output(resourceContext);
                resourceContext.reset();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
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
    }

    class ResourceBuilder implements TripleListener {

        @Override
        public TripleListener startPrefixMapping(String prefix, String uri) {
            return this;
        }

        @Override
        public TripleListener endPrefixMapping(String prefix) {
            return this;
        }

        @Override
        public ResourceBuilder newIdentifier(IRI identifier) {
            //resourceContext.resource().id(identifier);
            return this;
        }

        @Override
        public ResourceBuilder triple(Triple triple) {
            resourceContext.resource().add(triple);
            return this;
        }
    }
}
