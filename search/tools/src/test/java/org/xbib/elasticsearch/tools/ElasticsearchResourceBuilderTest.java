package org.xbib.elasticsearch.tools;

import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.xbib.elasticsearch.ResourceSink;
import org.xbib.elasticsearch.support.client.MockBulkClient;
import org.xbib.elements.marc.dialects.mab.MABElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABElementBuilderFactory;
import org.xbib.elements.marc.dialects.mab.MABElementMapper;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.dialects.MABDisketteReader;
import org.xml.sax.InputSource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class ElasticsearchResourceBuilderTest {

    public void testElements() throws Exception {
        InputStream in = getClass().getResourceAsStream("/test/mgl.txt");
        MABDisketteReader br = new MABDisketteReader(new BufferedReader(new InputStreamReader(in, "cp850")));
        Writer w = new OutputStreamWriter(new FileOutputStream("target/mgl2.xml"), "UTF-8");
        MockBulkClient es = new MockBulkClient()
                .setIndex("test")
                .setType("test");
        final ResourceSink sink = new ResourceSink(es);
        final MABElementBuilderFactory builderFactory = new MABElementBuilderFactory() {
            public MABElementBuilder newBuilder() {
                return new MABElementBuilder().addOutput(sink);
            }
        };
        final MABElementMapper mapper = new MABElementMapper("mab").start(builderFactory);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .addListener(mapper);
        try {
            Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
            reader.setProperty(Iso2709Reader.FORMAT, "MAB");
            reader.setProperty(Iso2709Reader.TYPE, "Titel");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            InputSource source = new InputSource(br);
            StreamResult target = new StreamResult(w);
            transformer.transform(new SAXSource(reader, source), target);
        } catch (NoNodeAvailableException e) {
        } finally {        
            es.shutdown();
            mapper.close();
        }
    }

    public void testZDBMAB() throws Exception {
        InputStream in = new FileInputStream(System.getProperty("user.home") + "/Daten/zdb/1211zdbtit.dat");
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "x-MAB"));
        Writer w = new OutputStreamWriter(new FileOutputStream("target/2012-11-zdb.xml"), "UTF-8");
        MockBulkClient es = new MockBulkClient()
                .setIndex("test")
                .setType("test");
        final ResourceSink sink = new ResourceSink(es);
        final MABElementBuilderFactory builderFactory = new MABElementBuilderFactory() {
            public MABElementBuilder newBuilder() {
                return new MABElementBuilder().addOutput(sink);
            }
        };
        final MABElementMapper mapper = new MABElementMapper("mab").start(builderFactory);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        try {
            Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
            reader.setProperty(Iso2709Reader.FORMAT, "MAB");
            reader.setProperty(Iso2709Reader.TYPE, "Titel");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            InputSource source = new InputSource(br);
            StreamResult target = new StreamResult(w);
            transformer.transform(new SAXSource(reader, source), target);
        } catch (NoNodeAvailableException e) {
        } finally {
            es.shutdown();
            mapper.close();
        }
    }
}
