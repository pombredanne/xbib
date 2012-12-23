package org.xbib.elements.dublincore
public class IdentifierElement extends DublinCoreElement {
    IdentifierElement build(builder, key, value) {
        println 'got identifier ' + value
        builder.context().resource().id(value)
        builder.context().resource().property("dc:identifier", value)
        return this
    }
}