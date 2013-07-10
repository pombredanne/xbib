package org.xbib.marc.extensions;

import org.testng.annotations.Test;
import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABContext;
import org.xbib.elements.marc.extensions.mab.MABElementMapper;
import org.xbib.analyzer.output.ElementOutput;
import org.xbib.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.tools.util.AtomicIntegerIterator;

import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;

public class MarcXmlTarReaderTest {

    private final Logger logger = LoggerFactory.getLogger(AlephPublishingReaderTest.class.getName());

    private MABElementMapper mapper;

    /**
     * Takes a long time (~10-20 minutes!)
     * @throws Exception
     */

    @Test
    public void testMABTarImport() throws Exception {
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
            public void output(MABContext context) throws IOException {
                counter++;
            }

            @Override
            public long getCounter() {
                return counter;
            }
            
        };

        ResourceBundle bundle = ResourceBundle.getBundle("org.xbib.marc.extensions.alephtest");
        String uriStr = bundle.getString("uri");
        Integer from = Integer.parseInt(bundle.getString("from"));
        Integer to = Integer.parseInt(bundle.getString("to"));

        MABBuilder builder = new MABBuilder().addOutput(output);
        mapper = new MABElementMapper("mab").start(builder);
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

        new MarcXmlTarReader()
                .setIterator(new AtomicIntegerIterator(from, to))
                .setURI(URI.create(uriStr))
                .setListener(kv);
    }
    
}
