package org.xbib.elements.output
import org.xbib.elements.dublincore.DublinCoreContext
import org.xbib.rdf.Resource
public class DublinCoreOutput extends DefaultElementOutput<DublinCoreContext> {
    public void output(DublinCoreContext context, info) { 
         println 'scripted output, got resource ' + context.resource()
    }
}