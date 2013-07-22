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
package org.xbib.json;

import com.google.common.io.CharStreams;
import org.testng.annotations.Test;

import org.xbib.common.xcontent.XContentHelper;
import org.xbib.common.xcontent.xml.XmlNamespaceContext;
import org.xbib.common.xcontent.xml.XmlXParams;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

public class Json2XmlTest {

    private final QName root = new QName("http://elasticsearch.org/ns/1.0/", "result", "es");

    String[] jsons = new String[] {
            "elasticsearch-hit-example-1",
            "es-test-20130719"
    };


    @Test
    public void testJSON() throws Exception {
        for (String s : jsons) {
            testJSONXmlReader(s);
            testJSONStreamer(s);
            testJSONXmlXContent(s);
        }
    }

    private void testJSONXmlReader(String path) throws Exception {
        Reader r = getInput(path);
        InputSource in = new InputSource(r);
        JsonXmlReader parser = new JsonXmlReader().root(root()).context(context());
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        Writer w = getOutput("test-jsonxmlreader-"+path+".xml");
        StreamResult stream = new StreamResult(w);
        transformer.transform(new SAXSource(parser, in), stream);
        w.close();
        r.close();
    }

    private void testJSONStreamer(String path) throws Exception {
        Reader r = getInput(path);
        Writer w = getOutput("test-jsonxmlstreamer-" + path + ".xml");
        JsonXmlStreamer jsonXml = new JsonXmlStreamer().root(root()).context(context());
        jsonXml.toXML(r, w);
        w.close();
        r.close();
    }

    private void testJSONXmlXContent(String path) throws Exception {
        Reader r = getInput(path);
        String json = CharStreams.toString(r);
        byte[] buf = json.getBytes("UTF-8");
        XmlXParams params = new XmlXParams(root(), context());
        String xml = XContentHelper.convertToXml(params, buf, 0, buf.length, false);
        Writer w = getOutput("test-xmlxcontent-" + path + ".xml");
        w.write(xml);
        w.close();
        r.close();
    }

    private Reader getInput(String path) throws IOException {
        InputStream in = getClass().getResourceAsStream("/org/xbib/json/" + path + ".json");
        if (in == null) {
            throw new IOException("resource not found: " + path);
        }
        return new InputStreamReader(in, "UTF-8");
    }
    
    private Writer getOutput(String path) throws IOException {
        return new OutputStreamWriter(new FileOutputStream("target/" + path),"UTF-8");
    }

    private QName root() {
        return root;
    }

    private XmlNamespaceContext context() {
        XmlNamespaceContext nsContext = XmlNamespaceContext.getDefaultInstance();
        nsContext.addNamespace("bib","info:srw/cql-context-set/1/bib-v1/");
        nsContext.addNamespace("xbib", "http://xbib.org/");
        nsContext.addNamespace("abc", "http://localhost/");
        nsContext.addNamespace("lia", "http://xbib.org/namespaces/lia/");
        return nsContext;
    }


}
