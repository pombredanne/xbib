package org.xbib.elements.output
import org.xbib.elements.dublincore.DublinCoreContext
import org.xbib.rdf.Resource
public class DublinCoreOutput extends DefaultElementOutput<DublinCoreContext> {
    public boolean output(DublinCoreContext context) { 
         println 'scripted output, got resource ' + context.resource()
         return true
    }
    public long getCounter() {
        return 0;
    }
}