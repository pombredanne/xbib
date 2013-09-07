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
package org.xbib.common.xcontent.xml;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;

import org.xbib.common.bytes.BytesReference;
import org.xbib.common.io.FastStringReader;
import org.xbib.common.xcontent.XContent;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.common.xcontent.XContentGenerator;
import org.xbib.common.xcontent.XContentParser;
import org.xbib.common.xcontent.XContentType;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * A XML based content implementation using Jackson XML dataformat
 *
 */
public class XmlXContent implements XContent {

    private final static XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    private final static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

    static {
        inputFactory.setProperty("javax.xml.stream.isNamespaceAware", Boolean.TRUE);
        inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
        inputFactory.setProperty("javax.xml.stream.isCoalescing", Boolean.TRUE);
        inputFactory.setProperty("javax.xml.stream.isReplacingEntityReferences", Boolean.FALSE);
        inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);

        outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.TRUE);

        xmlFactory = new XmlFactory(inputFactory, outputFactory);

        xmlXContent = new XmlXContent();
    }


    public static XContentBuilder contentBuilder() throws IOException {
        XContentBuilder builder = XContentBuilder.builder(xmlXContent);
        if (builder.generator() instanceof XmlXContentGenerator) {
            ((XmlXContentGenerator) builder.generator()).setParams(XmlXParams.getDefaultParams());
        }
        return builder;
    }

    public static XContentBuilder contentBuilder(XmlXParams params) throws IOException {
        XContentBuilder builder = XContentBuilder.builder(xmlXContent, params); // payload
        if (builder.generator() instanceof XmlXContentGenerator) {
            ((XmlXContentGenerator) builder.generator()).setParams(params);
        }
        return builder;
    }

    private final static XmlFactory xmlFactory;

    private final static XmlXContent xmlXContent;


    private XmlXContent() {
    }

    public XContentType type() {
        return XContentType.XML;
    }

    public static XmlXContent xmlXContent() {
        return xmlXContent;
    }

    protected static XmlFactory xmlFactory() {
        return xmlFactory;
    }

    public byte streamSeparator() {
        throw new UnsupportedOperationException("xml does not support stream parsing...");
    }

    public XContentGenerator createGenerator(OutputStream os) throws IOException {
        return new XmlXContentGenerator(xmlFactory.createGenerator(os, JsonEncoding.UTF8));
    }
    
    public XContentGenerator createGenerator(Writer writer) throws IOException {
        return new XmlXContentGenerator(xmlFactory.createGenerator(writer));
    }

    public XContentParser createParser(String content) throws IOException {
        return new XmlXContentParser(xmlFactory.createParser(new FastStringReader(content)));
    }

    public XContentParser createParser(InputStream is) throws IOException {
        return new XmlXContentParser(xmlFactory.createParser(is));
    }

    public XContentParser createParser(byte[] data) throws IOException {
        return new XmlXContentParser(xmlFactory.createParser(data));
    }

    public XContentParser createParser(byte[] data, int offset, int length) throws IOException {
        return new XmlXContentParser(xmlFactory.createParser(data, offset, length));
    }

    public XContentParser createParser(BytesReference bytes) throws IOException {
        if (bytes.hasArray()) {
            return createParser(bytes.array(), bytes.arrayOffset(), bytes.length());
        }
        return createParser(bytes.streamInput());
    }

    public XContentParser createParser(Reader reader) throws IOException {
        return new XmlXContentParser(xmlFactory.createParser(reader));
    }
}
