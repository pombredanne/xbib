package org.xbib.analyzer.dublincore
public class IdentifierElement extends DublinCoreElement {
    IdentifierElement build(builder, key, value) {
        println 'got identifier ' + value
        builder.context().resource().id(value)
        builder.context().resource().add("dc:identifier", value)
        return this
    }
}
identifierElement = new IdentifierElement()
