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
package org.xbib.rdf.io.json;

import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.RDF;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.rdf.xcontent.DefaultContentBuilder;

import java.io.IOException;
import java.util.Map;

public class JsonWriter<S extends Identifier, P extends Property, O extends Node>
        implements TripleListener<S,P,O> {

    private final Logger logger = LoggerFactory.getLogger(JsonWriter.class.getName());

    private IRINamespaceContext context;

    private Resource resource;

    private String translatePicaSortMarker;

    private boolean nsWritten;

    private ContentBuilder contentBuilder;

    private StringBuilder sb;

    private long byteCounter;

    private long idCounter;

    public JsonWriter() {
        this.context = IRINamespaceContext.newInstance();
        this.nsWritten = false;
        this.resource = new SimpleResource();
        this.contentBuilder = new DefaultContentBuilder();
        this.sb = new StringBuilder();
        this.translatePicaSortMarker = null;
    }

    public JsonWriter translatePicaSortMarker(String marker) {
        this.translatePicaSortMarker = marker;
        return this;
    }

    public JsonWriter contentBuilder(ContentBuilder contentBuilder) {
        this.contentBuilder = contentBuilder;
        return this;
    }

    @Override
    public JsonWriter newIdentifier(IRI iri) {
        if (!iri.equals(resource.id())) {
            try {
                if (!nsWritten) {
                    writeNamespaces();
                }
                contentBuilder.build(resource.context(), resource);
                idCounter++;
                resource = new SimpleResource();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        resource.id(iri);
        return this;
    }

    @Override
    public TripleListener<S, P, O> startPrefixMapping(String prefix, String uri) {
        return null;
    }

    @Override
    public TripleListener<S, P, O> endPrefixMapping(String prefix) {
        return null;
    }


    @Override
    public TripleListener<S, P, O> triple(Triple<S, P, O> triple) {
        return null; // TODO
    }

    public JsonWriter writeNamespaces() throws IOException {
        if (context == null) {
            return this;
        }
        nsWritten = false;
        for (Map.Entry<String, String> entry : context.getNamespaces().entrySet()) {
            if (entry.getValue().length() > 0) {
                String nsURI = entry.getValue().toString();
                if (!RDF.NS_URI.equals(nsURI)) {
                    writeNamespace(entry.getKey(), nsURI);
                    nsWritten = true;
                }
            }
        }
        return this;
    }

    private void writeNamespace(String prefix, String name) throws IOException {
        // TODO

    }

}
