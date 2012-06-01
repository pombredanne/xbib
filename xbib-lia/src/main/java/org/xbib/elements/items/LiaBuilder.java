package org.xbib.elements.items;

import org.xbib.elements.AbstractElementBuilder;
import org.xbib.elements.ElementContextFactory;
import org.xbib.elements.ResourceFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;

public class LiaBuilder<K,V> extends AbstractElementBuilder<LiaContext, LiaElement, K, V> {

    private final static ResourceFactory<Resource> resourceFactory = new ResourceFactory<Resource>() {

        @Override
        public Resource newResource() {
            return new SimpleResource();
        }
    };
    private final static ElementContextFactory<LiaContext> contextFactory = new ElementContextFactory<LiaContext>() {

        @Override
        public LiaContext newContext() {
            return new LiaContext();
        }
    };

    @Override
    protected ResourceFactory<Resource> getResourceFactory() {
        return resourceFactory;
    }

    @Override
    protected ElementContextFactory<LiaContext> getContextFactory() {
        return contextFactory;
    }

}
