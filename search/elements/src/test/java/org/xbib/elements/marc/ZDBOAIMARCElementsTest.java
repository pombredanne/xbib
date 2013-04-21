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
package org.xbib.elements.marc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.annotations.Test;
import org.xbib.elements.output.ElementOutput;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.rdf.context.ResourceContext;
import org.xml.sax.InputSource;

public class ZDBOAIMARCElementsTest {

    private static final Logger logger = LoggerFactory.getLogger(ZDBOAIMARCElementsTest.class.getName());

    @Test
    public void testOAIElements() throws Exception {
        logger.info("testOAIElements");
        final AtomicLong counter = new AtomicLong();
        final ElementOutput output = new ElementOutput<ResourceContext>() {
            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public void enabled(boolean enabled) {
            }

            @Override
            public void output(ResourceContext context) throws IOException {
                if (context != null) {
                    logger.info("resource = {}", context.resource());
                    counter.incrementAndGet();
                }
            }

            @Override
            public long getCounter() {
                return counter.get();
            }
        };

        MARCBuilderFactory factory = new MARCBuilderFactory() {
            public MARCBuilder newBuilder() {
                MARCBuilder builder = new OurMARCBuilder().addOutput(output);
                return builder;
            }
        };
        MARCElementMapper mapper = new MARCElementMapper("marc").start(factory);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        InputStream in = getClass().getResourceAsStream("/org/xbib/marc/xml/zdb-oai-marc.xml");
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        InputSource source = new InputSource(br);
        Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        reader.parse(source);
        br.close();
        mapper.close();
        logger.info("counter = {}", output.getCounter());
    }


    class OurMARCBuilder extends MARCBuilder {

        @Override
        public void build(MARCElement element, FieldCollection fields, String value) {
            if (context().resource().id() == null) {
                IRI id = IRI.builder().scheme("http").host("xbib.org").fragment(Long.toString(context().increment())).build();
                context().resource().id(id);
            }
            for (Field field : fields) {
                logger.debug("element={} field={}", element, field);
            }
        }
    }
}
