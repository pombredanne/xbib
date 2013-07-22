package org.xbib.analyzer.output
import org.xbib.analyzer.dublincore.DublinCoreContext
import org.xbib.elements.DefaultElementOutput

public class DublinCoreOutput extends DefaultElementOutput<DublinCoreContext> {
    public void output(DublinCoreContext context) {
         println 'scripted output, got resource ' + context.resource()
         return
    }
    public long getCounter() {
        return 0;
    }
}