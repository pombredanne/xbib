package org.xbib.elements.dublincore;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.annotations.Test;
import org.xbib.elements.ElementMapper;
import org.xbib.keyvalue.KeyValueReader;
import org.xbib.keyvalue.KeyValueStreamListener;
import org.xbib.elements.output.ElementOutput;
import org.xbib.rdf.Resource;

public class DublinCoreBuilderTest {

    private static final Logger logger = Logger.getLogger(DublinCoreBuilderTest.class.getName());
    
    private long counter;
    
    @Test
    public void testDublinCoreBuilder() throws Exception {
        StringReader sr = new StringReader("100=John Doe\n200=Hello Word\n300=2012\n400=1");
        ElementOutput<DublinCoreContext> output = new ElementOutput<DublinCoreContext>() {
            
            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public void output(DublinCoreContext context, Object info) {
                logger.log(Level.INFO, "resource = {0}", context.resource());
                logger.log(Level.INFO, "info = {0}", info);
                counter++;
            }

            @Override
            public long getCounter() {
                return counter;
            }
        };
        
        DublinCoreBuilder builder = new DublinCoreBuilder("dublincore").addOutput(output);
        KeyValueStreamListener listener = new ElementMapper("dublincore").addBuilder(builder);
        try (KeyValueReader reader = new KeyValueReader(sr).addListener(listener)) {
            while (reader.readLine() != null);
        }
    }
}
