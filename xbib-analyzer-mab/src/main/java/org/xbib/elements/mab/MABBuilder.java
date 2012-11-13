package org.xbib.elements.mab;

import org.xbib.elements.AbstractElementBuilder;
import org.xbib.elements.ElementContextFactory;
import org.xbib.elements.dublincore.DublinCoreProperties;
import org.xbib.elements.output.ElementOutput;
import org.xbib.marc.FieldCollection;

public class MABBuilder
    extends AbstractElementBuilder<FieldCollection, String, MABElement, MABContext>
    implements DublinCoreProperties {

    private final ElementContextFactory<MABContext> contextFactory = new ElementContextFactory<MABContext>() {

        @Override
        public MABContext newContext() {
            return new MABContext();
        }
    };     
    
    @Override
    protected ElementContextFactory<MABContext> getContextFactory() {
        return contextFactory;
    }
    
    @Override
    public MABContext context() {
        return context.get();
    }
    
    @Override
    public MABBuilder addOutput(ElementOutput output) {
        super.addOutput(output);
        return this;
    }
    
    @Override
    public void build(MABElement element, FieldCollection key, String value) {
    }
    
}
