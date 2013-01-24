package org.xbib.builders.elasticsearch;

import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.elasticsearch.ElasticsearchIndexerMock;
import org.xbib.elasticsearch.ElasticsearchResourceSink;
import org.xbib.elements.ElementMapper;
import org.xbib.keyvalue.KeyValueStreamListener;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.addons.MABDisketteReader;
import org.xml.sax.InputSource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class ElasticsearchResourceBuilderTest {

    public void testElements() throws Exception {
        InputStream in = getClass().getResourceAsStream("/test/mgl.txt");
        MABDisketteReader br = new MABDisketteReader(new BufferedReader(new InputStreamReader(in, "cp850")));
        Writer w = new OutputStreamWriter(new FileOutputStream("target/mgl2.xml"), "UTF-8");
        ElasticsearchIndexerMock es = new ElasticsearchIndexerMock()
                .index("test")
                .type("test");
        ElasticsearchResourceSink sink = new ElasticsearchResourceSink(es);
        try {
            MABBuilder builder = new MABBuilder().addOutput(sink);
            KeyValueStreamListener listener = new ElementMapper("mab").addBuilder(builder);
            MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(listener);
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
        }
    }

    public void testZDBElements() throws Exception {
        InputStream in = new FileInputStream("/Users/joerg/Daten/zdb/2012/1211zdbtit.dat");
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "x-MAB"));
        Writer w = new OutputStreamWriter(new FileOutputStream("target/2012-11-zdb.xml"), "UTF-8");
        ElasticsearchIndexerMock es = new ElasticsearchIndexerMock()
                .index("test")
                .type("test");
        ElasticsearchResourceSink sink = new ElasticsearchResourceSink(es);
        try {
            MABBuilder builder = new MABBuilder().addOutput(sink);
            KeyValueStreamListener listener = new ElementMapper("mab").addBuilder(builder);
            MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(listener);
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
        }
    }
}
