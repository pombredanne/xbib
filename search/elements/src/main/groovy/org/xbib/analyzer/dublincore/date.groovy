package org.xbib.analyzer.dublincore
public class DateElement extends DublinCoreElement {
    DateElement build(builder, key, value) {
        println 'got date ' + value
        return this
    }
}
dateElement = new DateElement()
