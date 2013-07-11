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
package org.xbib.marc.extensions.mab;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.testng.annotations.Test;
import org.xbib.marc.Iso2709Reader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MABTest {
    
    @Test
    public void testZDBMAB() throws IOException, SAXException,
            ParserConfigurationException, TransformerException {

        InputStream in = getClass().getResourceAsStream("1217zdbtit.dat");
        if (in == null) {
            throw new IOException("input stream not found");
        }
        FileOutputStream out = new FileOutputStream("target/zdb.xml");
        try (Writer w = new OutputStreamWriter(out, "UTF-8")) {
            InputSource source = new InputSource(new InputStreamReader(in, "x-MAB"));
            read(source, w);
            w.flush();
        }
    }

    @Test
    public void testCreateMABXML() throws IOException, SAXException,
            ParserConfigurationException, TransformerException {
        String s ="test.groupstream";
        InputStream in = getClass().getResourceAsStream(s);
        if (in == null) {
            throw new IOException("input stream not found");
        }
        FileOutputStream out = new FileOutputStream("target/" + s + ".xml");
        try (Writer w = new OutputStreamWriter(out, "UTF-8")) {
            InputSource source = new InputSource(new InputStreamReader(in, "x-MAB"));
            read(source, w);
            w.flush();
        }
    }

    private void read(InputSource source, Writer w) throws IOException, SAXException,
            ParserConfigurationException, TransformerException {
        Iso2709Reader reader = new Iso2709Reader();
        reader.setProperty(Iso2709Reader.FORMAT, "MAB");
        reader.setProperty(Iso2709Reader.TYPE, "Titel");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        StreamResult target = new StreamResult(w);
        transformer.transform(new SAXSource(reader, source), target);
    }
}
