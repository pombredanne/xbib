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
package org.xbib.marc.addons;

import org.xbib.marc.addons.AlephSequentialReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.testng.annotations.Test;
import org.xbib.io.BytesProgressWatcher;
import org.xbib.io.ProgressMonitoredOutputStream;
import org.xbib.io.SplitWriter;
import org.xbib.marc.FieldDesignator;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchangeListener;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class AlephSequentialFileImporterTest {
    
    private static final Logger logger = Logger.getLogger(AlephSequentialFileImporterTest.class.getName());
        
    private long count;
    
    @Test
    public void testSeq() throws Exception {
        InputStream in = getClass().getResourceAsStream("/test/seq.txt");
        convert("seq", in);
    }
    
    public void testAll() throws Exception {
        String prefix = "/Users/joerg/MARC21_BVB/";
        for (String name : Arrays.asList(
                "m0.20111130.sys.aa.seq.no.marc",
                "m0.20111130.sys.ab.seq.no.marc",
                "m0.20111130.sys.ac.seq.no.marc",
                "m0.20111130.sys.ad.seq.no.marc",
                "m0.20111130.sys.ae.seq.no.marc",
                "m0.20111130.sys.af.seq.no.marc",
                "m0.20111130.sys.ag.seq.no.marc",
                "m0.20111130.sys.ah.seq.no.marc",
                "m0.20111130.sys.ai.seq.no.marc",
                "m0.20111130.sys.aj.seq.no.marc",
                "m0.20111130.sys.ak.seq.no.marc",
                "m0.20111130.sys.al.seq.no.marc"
                )) {
            FileInputStream in = new FileInputStream(prefix + name);
            BytesProgressWatcher watcher = new BytesProgressWatcher(8192L);
            watchConvert(name, in, watcher);
        }
    }
    
    private void convert(String name, InputStream in) throws Exception {        
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        AlephSequentialReader seq = new AlephSequentialReader(br);
        OutputStream out = new GZIPOutputStream(new FileOutputStream("target/" + name+".xml.gz"));
        try (Writer bw = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))) {
            Iso2709Reader reader = new Iso2709Reader();
            //reader.setMarcXchangeListener(new LoggingMarcXchangeListener());
            reader.setProperty(Iso2709Reader.SCHEMA, "marc21");
            reader.setProperty(Iso2709Reader.FORMAT, "Marc21");
            reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            StreamResult target = new StreamResult(bw);
            transformer.transform(new SAXSource(reader, new InputSource(seq)), target);
        }
    }
    
    private void watchConvert(final String name, InputStream in, final BytesProgressWatcher watcher) throws Exception {        
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        AlephSequentialReader seq = new AlephSequentialReader(br);
        setCount(0L);
        try (SplitWriter bw = new SplitWriter(newWriter(name, getCount(), watcher))) {
            final Iso2709Reader reader = new Iso2709Reader();
            final StreamResult target = new StreamResult(bw);

            reader.setMarcXchangeListener(new MarcXchangeListener() {

                @Override
                public void beginRecord(String format, String type) {
                }

                @Override
                public void beginControlField(FieldDesignator designator) {
                }

                @Override
                public void beginDataField(FieldDesignator designator) {
                }

                @Override
                public void beginSubField(FieldDesignator designator) {
                }

                @Override
                public void endRecord() {
                    FieldDesignator f1 = new FieldDesignator("941", "  ");
                    f1.setSubfieldId("d");
                    reader.getAdapter().beginDataField(f1);
                    reader.getAdapter().beginSubField(f1);
                    f1.setData(" 1");
                    reader.getAdapter().endSubField(f1);
                    reader.getAdapter().endDataField(null);                    
                    FieldDesignator f2 = new FieldDesignator("956", "  ");                    
                    f2.setSubfieldId("u");
                    reader.getAdapter().beginDataField(f2);
                    reader.getAdapter().beginSubField(f2);
                    f2.setData(" http://index.hbz-nrw.de/query/services/document/xhtml/hbz/title/" + reader.getAdapter().getIdentifier());
                    reader.getAdapter().endSubField(f2);
                    reader.getAdapter().endDataField(null);
                }
                
                @Override
                public void leader(String label) {
                }

                @Override
                public void trailer(String trailer) {
                    long n = watcher.getBytesTransferred();
                    if (n > 1000000) {
                        try {
                            setCount(getCount() + 1);
                            reader.getAdapter().endCollection();
                            target.getWriter().flush();
                            bw.split(newWriter(name, getCount(), watcher));
                            reader.getAdapter().beginCollection();
                            watcher.resetWatcher();
                        } catch (IOException | SAXException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }

                @Override
                public void endControlField(FieldDesignator designator) {
                }

                @Override
                public void endDataField(FieldDesignator designator) {
                }

                @Override
                public void endSubField(FieldDesignator designator) {
                }
            });
            reader.setProperty(Iso2709Reader.SCHEMA, "marc21");
            reader.setProperty(Iso2709Reader.FORMAT, "Marc21");
            reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();            
            transformer.transform(new SAXSource(reader, new InputSource(seq)), target);
        }
    }

    private Writer newWriter(String name, long count, BytesProgressWatcher watcher) throws IOException {
        OutputStream out = new ProgressMonitoredOutputStream(new FileOutputStream(name+"_"+count+".xml"), watcher);        
        return new OutputStreamWriter(out, "UTF-8");
    }
    
    private void setCount(long count) {
        this.count = count;
    }
    
    private long getCount() {
        return count;
    }
      
}
