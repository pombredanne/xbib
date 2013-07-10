package org.xbib.analyzer.dublincore;
public class TitleElement extends DublinCoreElement {
    TitleElement build(builder, key, value) {
        println 'got title ' + value
        builder.context().resource().add("dc:title", value)
        return this
    }
}
titleElement = new TitleElement()
