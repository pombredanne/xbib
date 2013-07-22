package org.xbib.marc.dialects;

import java.io.IOException;

import org.xbib.elements.marc.dialects.mab.MABElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElementBuilderFactory;
import org.xbib.elements.marc.dialects.mab.MABElementMapper;
import org.xbib.elements.ElementOutput;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.rdf.Resource;
import org.xbib.rdf.xcontent.ContentBuilder;

import java.net.URI;

public class ConcurrentMABTarReaderTest {

    private MABElementMapper mapper;

    /**
     * Takes a long time (~10-20 minutes!)
     * @throws Exception
     */
    public void testMABTarImport() throws Exception {
        ImporterFactory factory = new ImporterFactory() {

            @Override
            public Importer newImporter() {
                return createImporter();
            }
        };
        new ImportService()
                .threads(Runtime.getRuntime().availableProcessors())
                .factory(factory)
                .execute();
        mapper.close();
    }
    
    private Importer createImporter() {
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
        final MABElementBuilderFactory builderFactory = new MABElementBuilderFactory() {
            public MABElementBuilder newBuilder() {
                return new MABElementBuilder().addOutput(output);
            }
        };
        final MABElementMapper mapper = new MABElementMapper("mab").start(builderFactory);
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        return new MarcXmlTarReader()
                .setURI(URI.create("tarbz2://"+System.getProperty("user.home")+"/import/hbz/aleph/clobs.hbz.metadata.mab.alephxml-clob-dump0"))
                .setListener(kv);
    }
    
}
