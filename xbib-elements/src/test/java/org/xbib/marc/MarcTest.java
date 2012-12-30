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
package org.xbib.marc;

import java.io.FileInputStream;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MarcTest {

   //  private final String pkg = "/" + getClass().getPackage().getName().replaceAll(".", "/");

    @Test
    public void testMarc() throws IOException, SAXException,
            ParserConfigurationException, TransformerConfigurationException, TransformerException {
        for (String s : new String[]{
                    "brkrtest.mrc", "makrtest.mrc", "chabon-loc.mrc", "chabon.mrc", "diacritic4.mrc",
                    "summerland.mrc", "error.mrc"
                }) {
            InputStream in = getClass().getResourceAsStream(s);
            if (in == null) {
                throw new IOException("input stream not found");
            }
            Iso2709Reader reader = new Iso2709Reader();
            FileOutputStream out = new FileOutputStream("target/" + s + ".xml");
            Writer w = new OutputStreamWriter(out, "UTF-8");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            InputSource source = new InputSource(new InputStreamReader(in, "ANSEL"));
            StreamResult target = new StreamResult(w);
            transformer.transform(new SAXSource(reader, source), target);
        }
    }

    public void testTalis() throws IOException, SAXException,
            ParserConfigurationException, TransformerConfigurationException, TransformerException {
        InputStream in = new FileInputStream("/Users/joerg/Downloads/talis-openlibrary-contribution.mrc");
        if (in == null) {
            throw new IOException("input stream not found");
        }
        Iso2709Reader reader = new Iso2709Reader();
        FileOutputStream out = new FileOutputStream("target/talis.xml");
        Writer w = new OutputStreamWriter(out, "UTF-8");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        InputSource source = new InputSource(new InputStreamReader(in, "ANSEL"));
        StreamResult target = new StreamResult(w);
        transformer.transform(new SAXSource(reader, source), target);
    }
}
