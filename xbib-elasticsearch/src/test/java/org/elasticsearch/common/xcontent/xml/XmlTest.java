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
package org.elasticsearch.common.xcontent.xml;

import java.io.IOException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import static org.elasticsearch.common.xcontent.XContentFactory.xmlBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlTest extends Assert {

    private static final Logger logger = LoggerFactory.getLogger(XmlTest.class.getName());

    @Test
    public void testXmlObject() throws IOException {

        XContentBuilder builder = xmlBuilder();
        builder.startObject().field("hello", "World").endObject();
        assertEquals(builder.string(), "<es:result><es:hello>World</es:hello></es:result>");
    }

    @Test
    public void testXmlArray() throws IOException {

        XContentBuilder builder = xmlBuilder();
        builder.startObject().array("test", "Hello", "World").endObject();
        assertEquals(builder.string(), "<es:result><es:test>Hello</es:test><es:test>World</es:test></es:result>");
    }

    @Test
    public void testXmlHandler() throws IOException {
        DefaultHandler handler = new DefaultHandler() {

            @Override
            public void startDocument() throws SAXException {
                logger.info("start document");
            }

            @Override
            public void endDocument() throws SAXException {
                logger.info("end document");
            }

            @Override
            public void startPrefixMapping(String string, String string1) throws SAXException {
                logger.info("start prefix mapping {} {}", string, string1);
            }

            @Override
            public void endPrefixMapping(String string) throws SAXException {
                logger.info("end prefix mapping");
            }

            @Override
            public void startElement(String ns, String localname, String string2, Attributes atrbts) throws SAXException {
                logger.info("start element {} {}", ns, localname);
            }

            @Override
            public void endElement(String ns, String localname, String string2) throws SAXException {
                logger.info("end element {} {}", ns, localname);
            }

            @Override
            public void characters(char[] chars, int i, int i1) throws SAXException {
                logger.info("character {}", new String(chars, i, i1));
            }
        };
        XContentBuilder builder = xmlBuilder(handler);
        builder.startObject().array("test", "Hello", "World").endObject();
        assertEquals(builder.string(), "<es:result><es:test>Hello</es:test><es:test>World</es:test></es:result>");
    }
}
