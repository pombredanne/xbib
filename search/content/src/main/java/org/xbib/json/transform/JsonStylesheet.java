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
package org.xbib.json.transform;

import org.xbib.json.JsonXmlReader;
import org.xbib.json.JsonXmlStreamer;
import org.xbib.json.JsonXmlValueMode;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.util.XMLEventConsumer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

/**
 *  Transform JSON with stylesheets
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class JsonStylesheet {

    private QName root;

    private StylesheetTransformer transformer;

    private String[] stylesheets;

    private IRINamespaceContext context;

    public JsonStylesheet() {
    }

    public JsonStylesheet root(QName root) {
        this.root = root;
        return this;
    }

    public JsonStylesheet setTransformer(StylesheetTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    public JsonStylesheet setStylesheets(String... stylesheets) {
        this.stylesheets = stylesheets;
        return this;
    }

    public JsonStylesheet setNamespaceContext(IRINamespaceContext context) {
        this.context = context;
        return this;
    }

    public JsonStylesheet transform(InputStream in, OutputStream out) throws IOException {
        return transform(in, new OutputStreamWriter(out, "UTF-8"));
    }

    public JsonStylesheet transform(InputStream in, Writer writer) throws IOException {
        if (root == null) {
            return this;
        }
        if (in == null) {
            return this;
        }
        if (writer == null) {
            return this;
        }
        if (transformer == null) {
            return this;
        }
        try {
            JsonXmlReader reader = new JsonXmlReader(root);
            if (stylesheets == null) {
                transformer.setSource(new SAXSource(reader, new InputSource(in)))
                        .setResult(writer).transform();
                return this;
            } else {
                transformer.setSource(new SAXSource(reader, new InputSource(in)))
                    .setResult(writer)
                    .transform(Arrays.asList(stylesheets));
            }
            return this;
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    public JsonStylesheet toXML(InputStream in, OutputStream out) throws IOException {
        return toXML(in, new OutputStreamWriter(out, "Utf-8"));
    }

    public JsonStylesheet toXML(InputStream in, Writer writer) throws IOException {
        if (root == null) {
            return this;
        }
        if (in == null) {
            return this;
        }
        if (writer == null) {
            return this;
        }
        try {
            JsonXmlStreamer jsonXml = context != null ?
                    new JsonXmlStreamer(context, JsonXmlValueMode.SKIP_EMPTY_VALUES)
                    : new JsonXmlStreamer(JsonXmlValueMode.SKIP_EMPTY_VALUES);
            XMLEventWriter events = jsonXml.openWriter(writer);
            jsonXml.toXML(in, events, root);
            events.flush();
            events.close();
            writer.flush();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
        return this;
    }

    public JsonStylesheet stream(InputStream in, XMLEventConsumer out) throws IOException {
        if (root == null) {
            return this;
        }
        if (in == null) {
            return this;
        }
        if (out == null) {
            return this;
        }
        try {
            JsonXmlStreamer jsonXml = context != null ?
                new JsonXmlStreamer(context, JsonXmlValueMode.SKIP_EMPTY_VALUES)
                : new JsonXmlStreamer(JsonXmlValueMode.SKIP_EMPTY_VALUES);
            jsonXml.toXML(in, out, root);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
        return this;
    }
}
