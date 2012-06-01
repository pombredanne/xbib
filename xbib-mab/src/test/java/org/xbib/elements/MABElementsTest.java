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
package org.xbib.elements;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.testng.annotations.Test;
import org.xbib.elements.mab.MABBuilder;
import org.xbib.elements.mab.MABContext;
import org.xbib.elements.output.ElementOutput;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.keyvalue.KeyValueStreamListener;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.addons.MABTarReader;
import org.xbib.rdf.Resource;
import org.xml.sax.InputSource;

public class MABElementsTest {

    private static final Logger logger = Logger.getLogger(MABElementsTest.class.getName());

    @Test
    public void testSetupOfElements() throws Exception {
        MABBuilder builder = new MABBuilder();
        KeyValueStreamListener listener = new ElementMapper("mab").addBuilder(builder);                
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().setListener(listener);        
        Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MAB");
        reader.setProperty(Iso2709Reader.TYPE, "Titel");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
    }
    
    public void testZDBElements() throws Exception {
        InputStream in = new FileInputStream("/Users/joerg/Daten/zdb/2012/1211zdbtit.dat");
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "x-MAB"));
        Writer w = new OutputStreamWriter(new FileOutputStream("target/2012-11-zdb.xml"), "UTF-8");
        MABBuilder builder = new MABBuilder();
        KeyValueStreamListener listener = new ElementMapper("mab").addBuilder(builder);
                
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().setListener(listener);        
        
        Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MAB");
        reader.setProperty(Iso2709Reader.TYPE, "Titel");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        InputSource source = new InputSource(br);
        StreamResult target = new StreamResult(w);
        transformer.transform(new SAXSource(reader, source), target);        
    }
    
    public void testAlephXML() throws InterruptedException, ExecutionException {
        ImporterFactory factory = new ImporterFactory() {

            @Override
            public Importer newImporter() {
                return createImporter();
            }
        };
        new ImportService().setThreads(1).setFactory(factory).run(
            "tarbz2:///Users/joerg/Downloads/clobs.hbz.metadata.mab.alephxml-clob-dump3"
        );
    }
    
    private Importer createImporter() {
        ElementOutput<MABContext> output = new ElementOutput<MABContext>() {

            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public void output(MABContext context, Object info) {
                logger.log(Level.INFO, "resource = {0}", context.resource());
                logger.log(Level.INFO, "info = {0}", info);
            }
        };
        MABBuilder builder = new MABBuilder().addOutput(output);
        ElementMapper mapper = new ElementMapper("mab").addBuilder(builder);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().setListener(mapper);
        return new MABTarReader().setListener(kv);
    }
    
}
