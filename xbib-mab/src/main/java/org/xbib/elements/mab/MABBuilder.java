package org.xbib.elements.mab;

import org.xbib.elements.AbstractElementBuilder;
import org.xbib.elements.ElementContextFactory;
import org.xbib.elements.ResourceFactory;
import org.xbib.elements.output.ElementOutput;
import org.xbib.elements.dublincore.DublinCoreProperties;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;

public class MABBuilder<K,V> 
    extends AbstractElementBuilder<MABContext,MABElement, K, V>
    implements DublinCoreProperties {

    private final ResourceFactory<Resource> resourceFactory = new ResourceFactory() {

        @Override
        public Resource newResource() {
            return new SimpleResource();
        }
    };
    private final ElementContextFactory<MABContext> contextFactory = new ElementContextFactory<MABContext>() {

        @Override
        public MABContext newContext() {
            return new MABContext();
        }
    };     
    
    @Override
    protected ResourceFactory<Resource> getResourceFactory() {
        return resourceFactory;
    }

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
    public void build(MABElement element, K key, V value) {
    }
    
}
