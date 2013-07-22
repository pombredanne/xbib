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
package org.xbib.marc.dialects.mab;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.testng.annotations.Test;
import org.xbib.elements.marc.dialects.mab.MABElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElementBuilderFactory;
import org.xbib.elements.marc.dialects.mab.MABElementMapper;
import org.xbib.elements.ElementOutput;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.dialects.MABDisketteReader;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MABDisketteTest {
    
    @Test
    public void testMAB() throws Exception {
        InputStream in = getClass().getResourceAsStream("mgl.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "cp850"));
        MABDisketteReader mab = new MABDisketteReader(reader);
        Writer w = new OutputStreamWriter(new FileOutputStream("target/mgl1.txt"), "UTF-8");
        toMAB(mab, w);
    }

    @Test
    public void testXML() throws Exception {
        InputStream in = getClass().getResourceAsStream("mgl.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "cp850"));
        MABDisketteReader mab = new MABDisketteReader(reader);
        Writer w = new OutputStreamWriter(new FileOutputStream("target/mgl2.xml"), "UTF-8");
        toXml(new InputSource(mab), w);
    }

    @Test
    public void testElements() throws Exception {
        InputStream in = getClass().getResourceAsStream("mgl.txt");
        MABDisketteReader br = new MABDisketteReader(new BufferedReader(new InputStreamReader(in, "cp850")));
        Writer w = new OutputStreamWriter(new FileOutputStream("target/mgl3.xml"), "UTF-8");
        final ElementOutput<MABContext, Resource> output = new ElementOutput<MABContext, Resource>() {
            long counter;

            @Override
            public void enabled(boolean enabled) {
                
            }
            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public void output(MABContext context, ContentBuilder<MABContext, Resource> builder) throws IOException {
                counter++;
            }

            @Override
            public long getCounter() {
                return counter;
            }
            
        };
        MABElementBuilderFactory mabElementBuilderfactory = new MABElementBuilderFactory() {
            public MABElementBuilder newBuilder() {
                return new MABElementBuilder().addOutput(output);
            }
        };
        MABElementMapper mapper = new MABElementMapper("mab/hbz/dialect")
                .start(mabElementBuilderfactory);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MAB");
        reader.setProperty(Iso2709Reader.TYPE, "Titel");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        InputSource source = new InputSource(br);
        StreamResult target = new StreamResult(w);
        transformer.transform(new SAXSource(reader, source), target);
        mapper.close();
    }

    private void toMAB(Reader r, Writer w) throws Exception {
        int ch;
        while ((ch = r.read()) != -1) {
            w.write(ch);
        }
        w.flush();
        w.close();
    }

    private void toXml(InputSource source, Writer w) throws IOException, SAXException,
            ParserConfigurationException, TransformerException {
        Iso2709Reader reader = new Iso2709Reader();
        reader.setProperty(Iso2709Reader.FORMAT, "MAB");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        StreamResult target = new StreamResult(w);
        transformer.transform(new SAXSource(reader, source), target);
    }
}
