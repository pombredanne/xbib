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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.namespace.QName;
import org.testng.annotations.Test;
import org.xbib.xml.ES;

public class JsonXmlStreamerTest {


    private void doJSONTest(String name) throws Exception {
        String path = "/org/xbib/json/" + name ;
        InputStream in = getClass().getResourceAsStream(path +  ".json");
        if (in == null) {
            throw new IOException("resource not found");
        }
        JsonXmlStreamer streamer = 
                new JsonXmlStreamer(JsonXmlValueMode.SKIP_EMPTY_VALUES);
        StringWriter sw = new StringWriter();
        QName root = new QName(ES.NS_URI, "result", ES.NS_PREFIX);
        streamer.toXML(in, sw, root);

        //write("test-streamer-" + name + ".xml", sw.toString());
        
        //InputStream result = getClass().getResourceAsStream(path + ".xml");
    }

    private void write(String filename, String xml) throws IOException {
        Writer writer = new OutputStreamWriter(new FileOutputStream(filename),"UTF-8");
        writer.write(xml);
        writer.close();
    }
    
    @Test
    public void testJSONStreamer1() throws Exception {
        doJSONTest("elasticsearch-hit-example-1");
    }

}
