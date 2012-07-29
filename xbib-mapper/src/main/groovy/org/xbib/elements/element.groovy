package org.xbib.elements
import java.util.Map
public class GroovyElement implements Element {
    void setParameter(Map map) {}
    void begin() {}
    void build(builder, key, value) { 
         println 'Hello World!'
         builder.context()
    }
    void end() {}
}