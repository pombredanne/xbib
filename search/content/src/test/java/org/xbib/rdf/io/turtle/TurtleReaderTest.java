package org.xbib.rdf.io.turtle;

import java.io.InputStream;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.simple.SimpleResource;

public class TurtleReaderTest {
    

    @Test
    public void testTurtleGND() throws Exception {
        InputStream in = getClass().getResourceAsStream("GND.ttl");
        Resource root = new SimpleResource();
        TripleListener listener = new ResourceBuilder(root);
        TurtleReader reader = new TurtleReader(IRI.create("http://d-nb.info/gnd/"));
        reader.setListener(listener);
        reader.parse(in);
    }

    class ResourceBuilder implements TripleListener {

        Resource resource;

        ResourceBuilder(Resource resource) {
            this.resource = resource;
        }
        
        public Resource getResource() {
            return resource;
        }
        
        @Override
        public ResourceBuilder newIdentifier(IRI uri) {
            resource.clear();
            resource.id(uri);
            return this;
        }

        @Override
        public ResourceBuilder triple(Triple triple) {
            resource.add(triple);
            return this;
        }
    }
}
