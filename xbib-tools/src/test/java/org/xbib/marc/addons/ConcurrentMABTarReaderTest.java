package org.xbib.marc.addons;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import org.xbib.elements.ElementMapper;
import org.xbib.elements.mab.MABBuilder;
import org.xbib.elements.mab.MABContext;
import org.xbib.elements.output.ElementOutput;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.marc.MarcXchange2KeyValue;

public class ConcurrentMABTarReaderTest {

    private final static Logger logger = Logger.getLogger(ConcurrentMABTarReaderTest.class.getName());
        
    public void test() throws InterruptedException, ExecutionException {
        ImporterFactory factory = new ImporterFactory() {

            @Override
            public Importer newImporter() {
                return createImporter();
            }
        };
        new ImportService().setThreads(4).setFactory(factory).run(
                "tarbz2:///Users/joerg/Downloads/clobs.hbz.metadata.mab.alephxml-clob-dump3"
        );
    }
    
    private Importer createImporter() {
        ElementOutput<MABContext> output = new ElementOutput<MABContext>() {

            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public void output(MABContext context, Object info) {
            }
        };        
        MABBuilder builder = new MABBuilder().addOutput(output);
        ElementMapper mapper = new ElementMapper("mab").addBuilder(builder);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().setListener(mapper);
        return new MABTarReader().setListener(kv);
    }
    
}
