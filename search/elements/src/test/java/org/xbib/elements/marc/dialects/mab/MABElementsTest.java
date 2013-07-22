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
package org.xbib.elements.marc.dialects.mab;

import org.testng.annotations.Test;
import org.xbib.elements.ElementOutput;
import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xml.sax.InputSource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

public class MABElementsTest {

    private static final Logger logger = LoggerFactory.getLogger(MABElementsTest.class.getName());

    @Test
    public void testSetupOfMABElements() throws Exception {
        MABElementMapper mapper = new MABElementMapper("mab/hbz/dialect").start();
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MAB");
        reader.setProperty(Iso2709Reader.TYPE, "Titel");
        mapper.close();
    }

    @Test
    public void testZDBMABElements() throws Exception {
        InputStream in =  InputService.getInputStream(URI.create("file:src/test/resources/org/xbib/elements/marc/dialects/mab/1217zdbtit.dat"));
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "x-MAB"));
        Writer w = new OutputStreamWriter(new FileOutputStream("target/zdb-mab-titel.xml"), "UTF-8");
        final ElementOutput output = new ElementOutput<ResourceContext, Resource>() {
            final AtomicLong counter = new AtomicLong();

            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public void enabled(boolean enabled) {
            }

            @Override
            public void output(ResourceContext context, ContentBuilder<ResourceContext, Resource> builder) throws IOException {
                IRI iri = IRI.builder().scheme("http")
                        .host("dummy")
                        .query("dummy")
                        .fragment(Long.toString(counter.getAndIncrement())).build();
                context.resource().id(iri);
                StringWriter sw = new StringWriter();
                TurtleWriter tw = new TurtleWriter().output(sw);
                tw.write(context.resource());
                //logger.info("out={}", sw.toString());
            }

            @Override
            public long getCounter() {
                return counter.get();
            }
        };
        MABElementBuilderFactory mabElementBuilderfactory = new MABElementBuilderFactory() {
            public MABElementBuilder newBuilder() {
                return new MABElementBuilder().addOutput(output);
            }
        };

        MABElementMapper mapper = new MABElementMapper("mab/hbz/dialect")
                .detectUnknownKeys(true)
                .start(mabElementBuilderfactory);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MAB");
        reader.setProperty(Iso2709Reader.TYPE, "Titel");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        InputSource source = new InputSource(br);
        StreamResult target = new StreamResult(w);
        transformer.transform(new SAXSource(reader, source), target);
        logger.info("unknown elements = {}", mapper.unknownKeys());
        mapper.close();
    }

}
