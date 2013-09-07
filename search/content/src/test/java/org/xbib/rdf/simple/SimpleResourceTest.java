package org.xbib.rdf.simple;

import java.util.Iterator;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.Loggers;
import org.xbib.rdf.IdentifiableNode;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;

public class SimpleResourceTest<S extends Identifier, P extends Property, O extends Node>
        extends Assert {

    private final Logger logger = Loggers.getLogger(SimpleResourceTest.class.getSimpleName());


    private final SimpleFactory<S, P, O> simpleFactory = SimpleFactory.getInstance();

    @Test
    public void testResourceId() throws Exception {
        IRI iri = IRI.create("http://index?type#id");
        Resource<S, P, O> r = new SimpleResource().id(iri);
        assertEquals("http", r.id().getScheme());
        assertEquals("index", r.id().getHost());
        assertEquals("type", r.id().getQuery());
        assertEquals("id", r.id().getFragment());
    }

    @Test
    public void testEmptyResources() throws Exception {
        Resource<S, P, O> r = new SimpleResource().id(IRI.create("urn:root"));
        assertEquals(r.isEmpty(), true);
        assertEquals(r.toString(), "<urn:root>");
    }

    @Test
    public void testEmptyProperty() throws Exception {
        Resource<S, P, O> r = new SimpleResource().id(IRI.create("urn:root"));
        r.add("urn:property", (String) null);
        assertEquals(r.isEmpty(), true);
    }

    @Test
    public void testStringLiteral() throws Exception {
        Resource<S, P, O> r = new SimpleResource().id(IRI.create("urn:root"));
        r.add("urn:property", "Hello World");
        assertEquals(r.isEmpty(), false);
        assertEquals(r.iterator().next().object().toString(), "Hello World");
    }
    
    
    @Test
    public void testIntegerLiteral() throws Exception {
        Resource<S, P, O> r = new SimpleResource().id(IRI.create("urn:root"));
        SimpleLiteral<O> literal = new SimpleLiteral(123).type(Literal.INT);
        r.add("urn:property", literal);
        assertEquals(r.isEmpty(), false);
        assertEquals(r.iterator().next().object().toString(), "123^^xsd:int");
    }

    @Test
    public void testPredicateSet() throws Exception {
        Resource<S, P, O> r = new SimpleResource().id(IRI.create("urn:doc1"))
                .add("urn:valueURI", "Hello World")
                .add("urn:creator", "Smith")
                .add("urn:creator", "Jones");
        S subject = r.subject();
        String[] s = new String[]{"urn:valueURI","urn:creator"};
        int i = 0;
        for (P predicate : r.predicateSet(subject)) {
            assertEquals(s[i++], predicate.toString());
        }
    }

    @Test
    public void testObjects() throws Exception {
        Resource<S, P, O> r = new SimpleResource().id(IRI.create("urn:doc4"));
        r.add("urn:hasAttribute", "a")
                .add("urn:hasAttribute", "b")
                .add("urn:hasAttribute", "a")
                .add("urn:hasAttribute", "c");
        assertEquals("[a, b, c]", r.objects("urn:hasAttribute").toString());
    }

    @Test
    public void testPropertyIterator() throws Exception {
        Resource<S, P, O> r = new SimpleResource();
        String id = "urn:doc2";
        r.id(IRI.create(id))
                .add("urn:valueURI", "Hello World")
                .add("urn:name", "Smith")
                .add("urn:name", "Jones");
        Iterator<Triple<S, P, O>> it = r.propertyIterator();
        assertEquals("urn:doc2 urn:valueURI Hello World", it.next().toString());
        assertEquals("urn:doc2 urn:name Smith", it.next().toString());
        assertEquals("urn:doc2 urn:name Jones", it.next().toString());
    }

    @Test
    public void testPredicateSetIterator() throws Exception {
        Resource<S, P, O> r = new SimpleResource<>();
        r.id(IRI.create("urn:doc1"))
                .add("urn:valueURI", "Hello World")
                .add("urn:name", "Smith")
                .add("urn:name", "Jones");
        // the first resource adds a resource value
        Resource<S, P, O> r1 = r.newResource("urn:res1");
        r1.add("urn:has", "a first resource value");
        // the second resource adds another resource value
        Resource<S, P, O> r2 = r.newResource("urn:res1");
        r2.add("urn:has", "a second resource value");
        
        assertEquals(r.nodeMap().size(), 3);
        
        // normal iterator
        int cnt = 0;
        Iterator<Triple<S,P,O>> it1 = r.iterator();
        while (it1.hasNext()) {
            it1.next();
            cnt++;
        }
        assertEquals(7, cnt);
        
        Iterator<P> it = r.predicateSet(r.subject()).iterator();
        int predCounter = 0;
        int objCounter = 0;
        while (it.hasNext()) {
            P pred = it.next();
            predCounter++;
            Iterator<O> values = r.objects(pred).iterator();
            while (values.hasNext()) {
                values.next();
                objCounter++;
            }
        }
        /**
         * val=Hello World
         * val=Smith
         * val=Jones
         * val=_:a2 urn:has a first resource value
         * val=_:a3 urn:has a second resource value
         */
        assertEquals(objCounter, 5);
        /**
         * pred=urn:valueURI
         * pred=urn:name
         * pred=urn:res1
         */
        assertEquals(predCounter, 3);
    }

    @Test
    public void testCompactPredicate() throws Exception {
        Resource<S, P, O> r = new SimpleResource<>();
        r.id(IRI.create("urn:doc"))
                .add("urn:value1", "Hello World");
        P predicate = simpleFactory.asPredicate("urn:pred");
        Resource<S, P, O> r1 = r.newResource(predicate);
        r1.add(predicate, "a value");
        Iterator<Triple<S,P,O>> it = r.iterator();
        int cnt = 0;
        while (it.hasNext()) {
            Triple<S,P,O> stmt = it.next();
            //System.err.println("stmt="+stmt);
            cnt++;
        }
        assertEquals(cnt, 3);        
  
        r.compactPredicate(predicate);
         it = r.iterator();
        cnt = 0;
        /*
         * urn:doc urn:value1 Hello World 
         * urn:doc urn:pred a value
         */
        while (it.hasNext()) {
            Triple stmt = it.next();
            cnt++;
        }
        assertEquals(cnt, 2);
    }


    @Test
    public void testAddingResources() throws Exception {
        Resource<S, P, O> r = new SimpleResource<>();
        r.id(IRI.create("urn:r"))
                .add("urn:value", "Hello R");

        // named ID
        Resource<S, P, O> s = new SimpleResource<>();
        s.id(IRI.create("urn:s"))
                .add("urn:value", "Hello S");

        // another named ID
        Resource<S, P, O> t = new SimpleResource<>();
        t.id(IRI.create("urn:t"))
                .add("urn:value", "Hello T");

        // a blank node resource ID
        IRI blank1 = new IdentifiableNode().blank().id();
        Resource<S, P, O> u = new SimpleResource<>();
        u.id(blank1).add("urn:value", "Hello U");

        // another blank node resource ID
        IRI blank2 = new IdentifiableNode().blank().id();
        Resource<S, P, O> v = new SimpleResource<>();
        v.id(blank2).add("urn:value", "Hello V");

        P predicate = simpleFactory.asPredicate("dc:subject");
        r.add(predicate, s);
        r.add(predicate, t);
        r.add(predicate, u);
        r.add(predicate, v);

        int cnt = 0;
        Iterator<Triple<S,P,O>> it = r.iterator();
        while (it.hasNext()) {
            Triple stmt = it.next();
            logger.info("{}", stmt);
            cnt++;
        }
        assertEquals(cnt, 9);
    }
}
