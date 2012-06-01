package org.xbib.rdf.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.testng.annotations.Test;
import org.xbib.io.InputStreamService;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.simple.SimpleResource;

public class TurtleReaderTest {
    
    private static final Logger logger = Logger.getLogger(TurtleReaderTest.class.getName());
    
    public void testTurtleGND() throws Exception {
        URI uri = URI.create("file:///Users/joerg/Downloads/GND.ttl.gz");
        InputStream in = new GZIPInputStream(new InputStreamService().getInputStream(uri));
        if (in == null) {
            throw new IOException("file not found");
        }
        Resource root = new SimpleResource();
        StatementListener listener = new ResourceBuilder(root);
        TurtleReader reader = new TurtleReader(URI.create("http://d-nb.info/gnd/"));
        reader.setListener(listener);
        reader.parse(in);
    }

    class ResourceBuilder implements StatementListener {

        Resource resource;

        ResourceBuilder(Resource resource) {
            this.resource = resource;
        }
        
        public Resource getResource() {
            return resource;
        }
        
        @Override
        public void newIdentifier(URI uri) {
            //if (!resource.isEmpty())
                //logger.log(Level.INFO, resource.toString());                
            resource.clear();
            resource.setIdentifier(uri);
            //logger.log(Level.INFO, uri.toString());
        }

        @Override
        public void statement(Statement statement) {
            resource.add(statement);
        }
    }
}
