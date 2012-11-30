package org.xbib.elements.dublincore
public class TitleElement extends DublinCoreElement {
    void build(builder, key, value) { 
         println 'got title ' + value
         builder.context().resource().property("dc:title", value)
    }
}