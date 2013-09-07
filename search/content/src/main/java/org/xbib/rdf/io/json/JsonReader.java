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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.xbib.json.JsonSaxAdapter;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.Triplifier;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;

/**
 * A triplifier for JSON (not JSON-LD)
 *
 */
public class JsonReader<S extends Resource<S, P, O>, P extends Property, O extends Literal<O>>
        implements Triplifier<S, P, O> {

    private TripleListener listener;

    private XmlHandler handler;

    private QName root;

    public JsonReader() {
    }

    @Override
    public JsonReader setTripleListener(TripleListener listener) {
        this.listener = listener;
        return this;
    }

    public JsonReader setHandler(XmlHandler handler) {
        this.handler = handler;
        return this;
    }

    public XmlHandler getHandler() {
        return handler;
    }

    public JsonReader root(QName root) {
        this.root = root;
        return this;
    }

    @Override
    public JsonReader parse(InputStream in) throws IOException {
        return parse(new InputStreamReader(in, "UTF-8"));
    }

    @Override
    public JsonReader parse(Reader reader) throws IOException {
        if (handler != null) {
            if (listener != null) {
                handler.setListener(listener);
            }
            JsonSaxAdapter adapter = new JsonSaxAdapter(reader, handler)
                    .root(root)
                    .context(handler.resourceContext().namespaceContext());
            try {
                adapter.parse();
            } catch (SAXException e) {
                throw new IOException(e);
            }
        }
        return this;
    }


}
