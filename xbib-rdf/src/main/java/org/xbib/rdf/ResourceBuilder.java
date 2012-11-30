package org.xbib.rdf;

import java.util.Stack;
import org.xbib.rdf.simple.SimpleResource;

public class ResourceBuilder {

    private Resource current;
    
    private Stack<Resource> stack;
    
    public ResourceBuilder() {
        this.stack = new Stack();
        this.current = new SimpleResource();
    }
    
    public Resource startResource(String predicate) {
        stack.push(current);
        current = current.newResource(predicate);
        return current;
    }
    
    public Resource property(String predicate, Object object) {
        current.property(predicate, object);
        return current;
    }
    
    public Resource endResource() {
        current = stack.pop();
        return current;
    }
}
