
package org.xbib.rdf;

import java.io.StringWriter;
import java.util.Iterator;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.io.xml.XMLResourceWriter;
import org.xbib.rdf.simple.SimpleBlankNode;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.rdf.simple.SimpleStatement;

/**
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class SimpleResourceTest<S extends Resource<S, P, O>, P extends Property, O extends Literal<O>>
        extends Assert {
    private final Factory<S,P,O> factory = Factory.getInstance();

    @Test
    public void testStatementEquaivalence() {
        SimpleStatement a = new SimpleStatement("urn:1", IRI.create("urn:2"), new SimpleLiteral("Hello World"));
        SimpleStatement b = new SimpleStatement(IRI.create("urn:1"), IRI.create("urn:2"), new SimpleLiteral("Hello World"));
        assertEquals(a.toString(), b.toString());
    }

    @Test
    public void testEmptyObject() throws Exception {
        SimpleResource<S, P, O> root = new SimpleResource(IRI.create("urn:root"));
        root.property("urn:property", (O)null);
        assertEquals(root.properties.size(), 0);
    }

    @Test
    public void testIntegerLiteral() throws Exception {
        SimpleResource<S, P, O> r = new SimpleResource(IRI.create("urn:root"));
        P pred = factory.asPredicate("urn:property");
        O literal = r.toObject(new SimpleLiteral(123).type(Literal.XSD_INT));
        r.property(pred, literal);
        assertEquals(r.properties.size(), 1);
    }    
    
    @Test
    public void testProperties() throws Exception {
        SimpleResource<S, P, O> r = new SimpleResource(IRI.create("urn:doc1"));
        String id = "urn:doc1";
        r.id(IRI.create(id));
        r.property("urn:valueURI", "Hello World");
        r.property("urn:creator", "Smith");
        r.property("urn:creator", "Jones");
        assertEquals("urn:doc1 urn:valueURI Hello World\nurn:doc1 urn:creator Smith\nurn:doc1 urn:creator Jones\n", r.toString());
    }

    @Test
    public void testPropertyIterator() throws Exception {
        SimpleResource<S, P, O> r = new SimpleResource(IRI.create("urn:doc2"));
        String id = "urn:doc2";
        r.id(IRI.create(id));
        r.property("urn:valueURI", "Hello World");
        r.property("urn:name", "Smith");
        r.property("urn:name", "Jones");
        Iterator<Statement<S, P, O>> it = r.iterator();
        assertEquals("urn:doc2 urn:valueURI Hello World", it.next().toString());
        assertEquals("urn:doc2 urn:name Smith", it.next().toString());
        assertEquals("urn:doc2 urn:name Jones", it.next().toString());
    }

    @Test
    public void testResourceIterator() throws Exception {
        SimpleBlankNode.reset();
        SimpleResource<S, P, O> parent = new SimpleResource<>();
        parent.id(IRI.create("urn:doc3"));
        Resource<S, P, O> child = parent.newResource("urn:resource");
        child.property("urn:property", "value");
        XMLResourceWriter<S, P, O> xmlrw = new XMLResourceWriter<>();
        // parent parent
        StringWriter sw = new StringWriter();
        xmlrw.toXML(parent, sw);
        assertEquals(sw.toString(), "<urn:doc3><urn:resource><urn:property>value</urn:property></urn:resource></urn:doc3>");
        // child parent
        sw = new StringWriter();
        xmlrw.toXML(child, sw);
        assertEquals(sw.toString(), "<genid:a1><urn:property>value</urn:property></genid:a1>");
    }

    @Test
    public void testBlankNodeRenumbering() throws Exception {
        SimpleBlankNode.reset();
        SimpleResource<S, P, O> r = new SimpleResource<>(IRI.create("urn:meta1"));
        r.id(IRI.create("urn:meta1"));
        
        Resource<S, P, O> r1 = r.newResource("urn:res1");
        r1.property("urn:has", "a first resource value");
        r.property("urn:has", "a first metadata value");

        SimpleResource<S, P, O> q = new SimpleResource<>(IRI.create("urn:meta2"));
        q.id(IRI.create("urn:meta2"));
        
        Resource<S, P, O> r2 = q.newResource("urn:res2");
        r2.property("urn:has", "a second resource value");
        q.property("urn:has", "a second metadata value");

        r.add("a:res", q);

        Iterator<Statement<S, P, O>> it = r.iterator(true);
        assertEquals("urn:meta1 urn:res1 _:a1", it.next().toString());
        assertEquals("_:a1 urn:has a first resource value", it.next().toString());
        assertEquals("urn:meta1 urn:has a first metadata value", it.next().toString());
        assertEquals("urn:meta1 urn:has a second metadata value", it.next().toString());
    }

    @Test
    public void testObjectSet() throws Exception {
        SimpleResource<S, P, O> resource = new SimpleResource<>(IRI.create("urn:doc4"));
        resource.property("urn:hasAttribute", "a");
        resource.property("urn:hasAttribute", "b");
        resource.property("urn:hasAttribute", "a");
        resource.property("urn:hasAttribute", "c");
        assertEquals("[a, b, c]", resource.objectSet("urn:hasAttribute").toString());
    }

    @Test
    public void testPredicateIterator() throws Exception {
        SimpleResource<S, P, O> r = new SimpleResource<>();
        r.id(IRI.create("urn:doc11"));
        r.property("urn:valueURI", "Hello World");
        r.property("urn:name", "Smith");
        r.property("urn:name", "Jones");
        Resource<S, P, O> r1 = r.newResource("urn:res1");
        r1.property("urn:has", "a first resource value");
        Resource<S, P, O> r2 = r.newResource("urn:res1");
        r2.property("urn:has", "a second resource value");
        Iterator<P> it = r.predicateSet(r.subject()).iterator();
        int propCounter = 0;
        int valueCounter = 0;
        while (it.hasNext()) {
            P pred = it.next();
            propCounter++;
            Iterator<O> vit = r.objectSet(pred).iterator();
            while (vit.hasNext()) {
                vit.next();
                valueCounter++;
            }
        }
        assertEquals(valueCounter, 5);
        assertEquals(propCounter, 3);
    }

    @Test
    public void testCompact() throws Exception {
        SimpleResource<S, P, O> r = new SimpleResource<>();
        r.id(IRI.create("urn:doc12"));
        r.property("urn:value1", "Hello World");
        P predicate = factory.asPredicate("urn:value2");
        Resource<S, P, O> r1 = r.newResource("urn:value2");
        r1.property(predicate, "a value");
        r.compact(predicate);
        Iterator<Statement<S, P, O>> it = r.iterator();
        int cnt = 0;
        /*
         * urn:doc12 urn:value1 Hello World 
         * urn:doc12 urn:value2 a value
         */
        while (it.hasNext()) {
            Statement<S, P, O> stmt = it.next();
            cnt++;
        }
        assertEquals(cnt, 2);
    }
}
