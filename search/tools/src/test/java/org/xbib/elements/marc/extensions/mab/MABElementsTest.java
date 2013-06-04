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
package org.xbib.elements.marc.extensions.mab;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xbib.elements.output.ElementOutput;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.InputService;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.extensions.MABTarReader;
import org.xml.sax.InputSource;

public class MABElementsTest {

    private static final Logger logger = LoggerFactory.getLogger(MABElementsTest.class.getName());

    private MABElementMapper mapper;

    public void testSetupOfElements() throws Exception {
        MABBuilder builder = new MABBuilder();
        mapper = new MABElementMapper("mab").start(builder);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MAB");
        reader.setProperty(Iso2709Reader.TYPE, "Titel");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        mapper.close();
    }
    
    public void testZDBElements() throws Exception {
        InputStream in =  InputService.getInputStream(URI.create("file:src/test/resources/1217zdbtit.dat"));
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "x-MAB"));
        Writer w = new OutputStreamWriter(new FileOutputStream("target/ZDB-MAB-Titel.xml"), "UTF-8");
        MABBuilder builder = new MABBuilder();
        mapper = new MABElementMapper("mab").start(builder);
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
    
    public void testAlephXML() throws InterruptedException, ExecutionException {
        ImporterFactory factory = new ImporterFactory() {

            @Override
            public Importer newImporter() {
                return createAlephXMLImporter();
            }
        };
        new ImportService().threads(1).factory(factory).execute();
        mapper.close();
    }
    
    private Importer createAlephXMLImporter() {
        ElementOutput<MABContext> output = new ElementOutput<MABContext>() {

            long counter;
            
            @Override
            public boolean enabled() {
                return true;
            }
            @Override
            public void enabled(boolean enabled) {
                
            }
            @Override
            public void output(MABContext context) throws IOException {
                logger.info("resource = {}", context.resource());
                counter++;
            }

            @Override
            public long getCounter() {
                return counter;
            }
        };
        MABBuilder builder = new MABBuilder().addOutput(output);
        mapper = new MABElementMapper("mab").start(builder);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        return new MABTarReader()
                .setURI(URI.create("tarbz2:src/test/resources/20120805_20120806"))
                .setListener(kv);
    }
    
}
