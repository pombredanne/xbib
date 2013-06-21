package org.xbib.marc.extensions;

import java.io.IOException;

import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABContext;
import org.xbib.elements.marc.extensions.mab.MABElementMapper;
import org.xbib.elements.output.ElementOutput;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.marc.MarcXchange2KeyValue;

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
        MABBuilder builder = new MABBuilder().addOutput(output);
        mapper = new MABElementMapper("mab").start(builder);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(mapper);
        return new MarcXmlTarReader()
                .setURI(URI.create("tarbz2://"+System.getProperty("user.home")+"/Daten/hbz/aleph/clobs.hbz.metadata.mab.alephxml-clob-dump0"))
                .setListener(kv);
    }
    
}
