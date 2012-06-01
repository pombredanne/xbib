package org.xbib.elements.dublincore
public class IdentifierElement extends DublinCoreElement {
    void build(builder, key, value) { 
         println 'got identifier ' + value
         builder.context().resource().setIdentifier(value)
         builder.context().resource().addProperty("dc:identifier", value)
    }
}