package org.xbib.marc.dialects;

import org.testng.annotations.Test;
import org.xbib.elements.marc.dialects.mab.MABElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElementBuilderFactory;
import org.xbib.elements.marc.dialects.mab.MABElementMapper;
import org.xbib.elements.ElementOutput;
import org.xbib.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.rdf.Resource;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.tools.util.AtomicIntegerIterator;

import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;

public class MarcXmlTarReaderTest {

    private final Logger logger = LoggerFactory.getLogger(AlephPublishingReaderTest.class.getName());

    private MABElementMapper mapper;

    @Test
    public void testMABTarImport() throws Exception {
        final ElementOutput<MABContext,Resource> output = new ElementOutput<MABContext,Resource>() {
            long counter;

            @Override
            public void enabled(boolean enabled) {
                
            }
            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public void output(MABContext context, ContentBuilder contentBuilder) throws IOException {
                counter++;
            }

            @Override
            public long getCounter() {
                return counter;
            }
            
        };

        ResourceBundle bundle = ResourceBundle.getBundle("org.xbib.marc.dialects.alephtest");
        String uriStr = bundle.getString("uri");
        Integer from = Integer.parseInt(bundle.getString("from"));
        Integer to = Integer.parseInt(bundle.getString("to"));

        final MABElementBuilderFactory builderFactory = new MABElementBuilderFactory() {
            public MABElementBuilder newBuilder() {
                return new MABElementBuilder().addOutput(output);
            }
        };
        final MABElementMapper mapper = new MABElementMapper("mab/hbz/dialect").start(builderFactory);
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
