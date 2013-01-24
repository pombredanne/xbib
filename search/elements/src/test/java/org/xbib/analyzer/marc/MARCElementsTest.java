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
package org.xbib.analyzer.marc;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.elements.output.ElementOutput;
import org.xbib.keyvalue.KeyValueStreamListener;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.rdf.context.ResourceContext;
import org.xml.sax.InputSource;

public class MARCElementsTest extends Assert {

    @Test
    public void testSetupOfElements() throws Exception {
        MARCBuilder builder = new MARCBuilder();
        KeyValueStreamListener listener = new MARCElementMapper("marc").addBuilder(builder);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(listener);
        Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MARC");
        reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        assertNotNull(transformer);
    }

    @Test
    public void testZDBElements() throws Exception {
        InputStream in = getClass().getResourceAsStream("1217zdbtit.dat");
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "x-MAB"));
        Writer w = new OutputStreamWriter(new FileOutputStream("target/zdb.xml"), "UTF-8");
        MARCBuilder builder = new MARCBuilder();
        KeyValueStreamListener listener = new MARCElementMapper("marc").addBuilder(builder);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(listener);
        Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MARC");
        reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        InputSource source = new InputSource(br);
        StreamResult target = new StreamResult(w);
        transformer.transform(new SAXSource(reader, source), target);
    }

    @Test
    public void testStbBonnElements() throws Exception {
        InputStream in = getClass().getResourceAsStream("stb-bonn.mrc");
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        Writer w = new OutputStreamWriter(new FileOutputStream("target/DE-369.xml"), "UTF-8");
        MARCBuilder builder = new MARCBuilder();
        builder.addOutput(new ElementOutput<ResourceContext>() {
            @Override
            public boolean enabled() {
                return true;
            }
            @Override
            public void enabled(boolean enabled) {
                
            }
            @Override
            public void output(ResourceContext context) throws IOException {
            }

            @Override
            public long getCounter() {
                return 0;
            }
        });
        KeyValueStreamListener kvlistener = new MARCElementMapper("marc").addBuilder(builder);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(kvlistener);
        Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MARC");
        reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        InputSource source = new InputSource(br);
        StreamResult target = new StreamResult(w);
        transformer.transform(new SAXSource(reader, source), target);
    }
}
