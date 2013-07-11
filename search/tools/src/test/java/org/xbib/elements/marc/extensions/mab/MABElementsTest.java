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

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import org.xbib.analyzer.output.ElementOutput;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.extensions.MarcXmlTarReader;

public class MABElementsTest {

    private static final Logger logger = LoggerFactory.getLogger(MABElementsTest.class.getName());

    private MABElementMapper mapper;

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
        MABElementBuilder builder = new MABElementBuilder().addOutput(output);
        mapper = new MABElementMapper("mab").start(builder);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        return new MarcXmlTarReader()
                .setURI(URI.create("tarbz2:src/test/resources/20120805_20120806"))
                .setListener(kv);
    }
    
}
