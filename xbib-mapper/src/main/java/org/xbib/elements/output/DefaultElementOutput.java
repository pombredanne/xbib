package org.xbib.elements.output;

import org.xbib.elements.ElementContext;

public abstract class DefaultElementOutput<C extends ElementContext> 
    implements ElementOutput<C> {
    
    @Override
    public boolean enabled() {
        String enabled = System.getProperty(getClass().getName());
        return enabled == null || !"false".equalsIgnoreCase(enabled);
    }
    
}
