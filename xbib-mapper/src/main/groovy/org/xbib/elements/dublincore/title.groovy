package org.xbib.elements.dublincore
public class TitleElement extends DublinCoreElement {
    TitleElement build(builder, key, value) {
        println 'got title ' + value
        builder.context().resource().property("dc:title", value)
        return this
    }
}