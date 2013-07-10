package org.xbib.analyzer.dublincore
public class CreatorElement extends GroovyDublinCoreElement {
    CreatorElement build(builder, key, value) {
        println 'got author ' + value
        builder.context().resource().add("dc:creator", value)
        return this
    }
}
creatorElement = new CreatorElement()