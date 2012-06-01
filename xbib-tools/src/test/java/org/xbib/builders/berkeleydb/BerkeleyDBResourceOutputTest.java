package org.xbib.builders.berkeleydb;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.xbib.elements.ElementMapper;
import org.xbib.keyvalue.KeyValueStreamListener;
import org.xbib.elements.mab.MABBuilder;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xml.sax.InputSource;

public class BerkeleyDBResourceOutputTest {

    public void testZDBElements() throws Exception {
        InputStream in = new FileInputStream("/Users/joerg/Daten/zdb/2012/1211zdbtit.dat");
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "x-MAB"));
        Writer w = new OutputStreamWriter(new FileOutputStream("target/2012-11-zdb.xml"), "UTF-8");
        BerkeleyDBResourceOutput bdb = new BerkeleyDBResourceOutput();
        try {
            bdb.connect(URI.create("bdbresource:target/zdbdb"));
            MABBuilder builder = new MABBuilder().addOutput(bdb);
            KeyValueStreamListener listener = new ElementMapper("mab").addBuilder(builder);
            MarcXchange2KeyValue kv = new MarcXchange2KeyValue().setListener(listener);
            Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
            reader.setProperty(Iso2709Reader.FORMAT, "MAB");
            reader.setProperty(Iso2709Reader.TYPE, "Titel");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            InputSource source = new InputSource(br);
            StreamResult target = new StreamResult(w);
            transformer.transform(new SAXSource(reader, source), target);
        } finally {
            bdb.disconnect();
        }
    }
}
