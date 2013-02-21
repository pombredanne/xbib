package org.xbib.rdf.io.json;

import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.xml.AbstractXmlHandler;
import org.xbib.rdf.simple.Factory;

import javax.xml.namespace.QName;
import java.util.Stack;

public abstract class JsonResourceHandler extends AbstractXmlHandler {

    protected final Factory factory = Factory.getInstance();
    private Stack<Resource> stack = new Stack();

    public JsonResourceHandler(ResourceContext resourceContext) {
        super(resourceContext);
        super.setDefaultNamespace("", "http://json.org");
    }

    @Override
    public void openResource() {
        super.openResource();
        stack.push(resourceContext.resource());
    }

    @Override
    public void closeResource() {
        super.closeResource();
        stack.clear();
    }

    @Override
    public void openPredicate(QName parent, QName name, int level) {
        // nested resource creation
        // always create newResource, even if there will be only a single literal. We will compact later.
        Resource r = stack.peek()
                .newResource(makePrefix(name.getPrefix()) + ":" + name.getLocalPart());
        stack.push(r);
    }

    @Override
    public void addToPredicate(String content) {
        //stack.peek().setValue(content);
    }

    @Override
    public void closePredicate(QName parent, QName name, int level) {
        Property p = (Property) factory.asPredicate(makePrefix(name.getPrefix()) + ":" + name.getLocalPart());
        Resource r = stack.pop();
        if (level < 0) {
            // it's a newResource
            if (!stack.isEmpty()) {
                stack.peek().add(p, r);
            }
        } else {
            // it's a property
            String s = content();
            if (s != null) {
                r.add(p, factory.newLiteral(toLiteral(name, s)));
                // compact because it has only a single value
                if (!stack.isEmpty()) {
                    stack.peek().compactPredicate(p);
                }
            }
        }
    }

    protected Object toLiteral(QName name, String content) {
        return content;
    }
}
