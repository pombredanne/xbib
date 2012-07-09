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

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xbib.elements.ElementMapper;
import org.xbib.elements.mab.MABBuilder;
import org.xbib.elements.mab.MABContext;
import org.xbib.elements.output.ElementOutput;
import org.xbib.io.util.AtomicIntegerIterator;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.rdf.Resource;

public class AlephPublishingReaderTest {

    private static final Logger logger = Logger.getLogger(AlephPublishingReaderTest.class.getName());

    public void testSimpleAleph() throws IOException {
        System.setProperty("java.naming.factory.initial", "org.xbib.naming.SimpleContextFactory");

        AlephPublishingReader reader = new AlephPublishingReader().setIterator(new AtomicIntegerIterator(1, 10)).setLibrary("hbz50").setSetName("ALEPHSEMAB").setURI(URI.create("jdbc://alephse:alephse@localhost:1241/aleph0?jdbcScheme=jdbc:oracle:thin:@&driverClassName=oracle.jdbc.OracleDriver"));
        try {
            while (reader.hasNext()) {
                logger.log(Level.INFO, reader.next().toString());
            }
        } finally {
            reader.close();
        }
    }

    public void testAleph2MarcXChange() throws IOException {
        System.setProperty("java.naming.factory.initial", "org.xbib.naming.SimpleContextFactory");

        ElementOutput<MABContext> output = new ElementOutput<MABContext>() {
            long counter;

            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public void output(MABContext context, Object info) {
                logger.log(Level.INFO, "resource = {0}", context.resource());
                logger.log(Level.INFO, "info = {0}", info);
                counter++;
            }

            @Override
            public long getCounter() {
                return counter;
            }
        };        
        MABBuilder builder = new MABBuilder().addOutput(output);
        ElementMapper mapper = new ElementMapper("mab").addBuilder(builder);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().setListener(mapper);
        AlephPublishingReader reader = new AlephPublishingReader().setListener(kv).setIterator(new AtomicIntegerIterator(1, 10)).setLibrary("hbz50").setSetName("ALEPHSEMAB").setURI(URI.create("jdbc://alephse:alephse@localhost:1241/aleph0?jdbcScheme=jdbc:oracle:thin:@&driverClassName=oracle.jdbc.OracleDriver"));
        try {
            while (reader.hasNext()) {
                logger.log(Level.INFO, reader.next().toString());
            }
        } finally {
            reader.close();
        }
    }
}
