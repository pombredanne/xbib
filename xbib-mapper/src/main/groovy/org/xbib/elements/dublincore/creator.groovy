package org.xbib.elements.dublincore
public class CreatorElement extends GroovyDublinCoreElement {
    void build(builder, key, value) { 
         println 'got author ' + value
         builder.context().resource().addProperty("dc:creator", value)
    }
}