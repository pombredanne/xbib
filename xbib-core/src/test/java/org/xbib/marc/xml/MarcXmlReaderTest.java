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
package org.xbib.marc.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.MarcXchangeListener;
import org.xml.sax.InputSource;

public class MarcXmlReaderTest {

    private static final Logger logger = LoggerFactory.getLogger(MarcXmlReaderTest.class.getName());
    
    @Test
    public void testOAI() throws Exception {
        InputStream in = new FileInputStream("src/test/resources/test/xml/zdb-oai-marc.xml");
        if (in == null) {
            throw new IOException("input stream not found");
        }
        InputSource source = new InputSource(new InputStreamReader(in, "UTF-8"));
        MarcXmlReader reader = new MarcXmlReader(source);
        reader.setListener(new MarcXchangeListener() {

            @Override
            public void leader(String label) {
                logger.debug("leader="+label);
            }

            @Override
            public void beginRecord(String format, String type) {
                logger.debug("beginRecord format="+format + " type="+type);
            }

            @Override
            public void beginControlField(Field field) {
                logger.debug("beginControlField field="+field);
            }

            @Override
            public void endControlField(Field field) {
                logger.debug("endControlField field="+field);
            }

            @Override
            public void beginDataField(Field field) {
                logger.debug("beginDataField field="+field);
            }

            @Override
            public void endDataField(Field field) {
                logger.debug("endDataField field="+field);
            }

            @Override
            public void beginSubField(Field field) {
                logger.debug("beginSubField field="+field);
            }

            @Override
            public void endSubField(Field field) {
                logger.debug("endsubField field="+field);
            }

            @Override
            public void endRecord() {
                logger.debug("endRecord");
            }

            @Override
            public void trailer(String trailer) {
                logger.debug("trailer " + trailer);
            }
        });
        reader.parse();
    }
}
