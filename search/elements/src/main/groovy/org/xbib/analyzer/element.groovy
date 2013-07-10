package org.xbib.analyzer

import org.xbib.elements.Element

public class GroovyElement implements Element {
    GroovyElement setSettings(Map map) { return this }
    Map<String,Object> getSettings() { return null }
    GroovyElement begin() { return this }
    GroovyElement build(builder, key, value) {
         println 'Hello World!'
         builder.context()
        return this
    }
    GroovyElement end() { return this }
}

groovyElement = new GroovyElement()
