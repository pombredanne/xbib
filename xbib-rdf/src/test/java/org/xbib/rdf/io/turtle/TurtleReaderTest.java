package org.xbib.rdf.io.turtle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.testng.annotations.Test;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.io.StatementListener;
import org.xbib.rdf.simple.SimpleResource;

public class TurtleReaderTest {
    

    @Test
    public void testTurtleGND() throws Exception {
        InputStream in = getClass().getResourceAsStream("/GND.ttl");
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
            resource.id(uri);
            //logger.log(Level.INFO, uri.toString());
        }

        @Override
        public void statement(Statement statement) {
            resource.add(statement);
        }
    }
}
