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
package org.xbib.marc.extensions;

import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;

import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABContext;
import org.xbib.elements.marc.extensions.mab.MABElementMapper;
import org.xbib.analyzer.output.ElementOutput;
import org.xbib.keyvalue.KeyValueStreamAdapter;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.tools.util.AtomicIntegerIterator;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;

public class AlephPublishingReaderTest {

    private final Logger logger = LoggerFactory.getLogger(AlephPublishingReaderTest.class.getName());

    public void testAleph2MarcXChange() throws IOException {
        System.setProperty("java.naming.factory.initial", "org.xbib.naming.SimpleContextFactory");

        ElementOutput<MABContext> output = new ElementOutput<MABContext>() {
            long counter;

            @Override
            public void enabled(boolean enabled) {
            }
            
            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public void output(MABContext context) throws IOException{
                logger.info("MAB context resource = {}", context.resource());
                counter++;
            }

            @Override
            public long getCounter() {
                return counter;
            }
        };        
        MABBuilder builder = new MABBuilder().addOutput(output);
        MABElementMapper mapper = new MABElementMapper("mab").start(builder);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .addListener(mapper)
                .addListener(new KeyValueStreamAdapter<FieldCollection, String>() {
                    @Override
                    public void keyValue(FieldCollection key, String value) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("begin");
                            for (Field f : key) {
                                logger.debug("tag={} ind={} subf={} data={}",
                                        f.tag(), f.indicator(), f.subfieldId(), f.data());
                            }
                            logger.debug("end");
                        }
                    }

                });

        ResourceBundle bundle = ResourceBundle.getBundle("org.xbib.marc.extensions.alephtest");
        String library = bundle.getString("library");
        String setName = bundle.getString("setname");
        String uriStr = bundle.getString("uri");

        Integer from = Integer.parseInt(bundle.getString("from"));
        Integer to = Integer.parseInt(bundle.getString("to"));

        AlephPublishingReader reader = new AlephPublishingReader()
                .setListener(kv)
                .setIterator(new AtomicIntegerIterator(from, to))
                .setLibrary(library)
                .setSetName(setName)
                .setURI(URI.create(uriStr));
        try {
            while (reader.hasNext()) {
                logger.info(reader.next().toString());
            }
        } finally {
            reader.close();
            mapper.close();
        }
    }
}
