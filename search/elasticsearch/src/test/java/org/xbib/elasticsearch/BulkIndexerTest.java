package org.xbib.elasticsearch;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.elasticsearch.support.CQLSearchSupport;
import org.xbib.elasticsearch.support.ingest.transport.TransportClientIngestSupport;
import org.xbib.elasticsearch.xml.ES;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.JsonLdContext;
import org.xbib.rdf.context.ResourceContext;

import java.io.ByteArrayOutputStream;
import java.net.URI;

import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.indices.IndexMissingException;

public class BulkIndexerTest extends Assert {

    private static final Logger logger = LoggerFactory.getLogger(BulkIndexerTest.class.getName());

    @Test
    public void testBulkIndexerWithSingleResourceAndCQLSearch() throws Exception {
        try {
            final TransportClientIngestSupport es = new TransportClientIngestSupport()
                    .newClient(URI.create("es://localhost:9300?es.cluster.name=test"))
                    .setIndex("document")
                    .setType("test");

            es.deleteIndex();
            ResourceContext context = createContext();
            new ElasticsearchResourceSink(es).output(context, context.contentBuilder());
            es.flush();
            Thread.sleep(2000);
            Logger queryLogger = LoggerFactory.getLogger("test", BulkIndexerTest.class.getName());
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            // check if IRI path "document" worked
            new CQLSearchSupport()
                    .newClient(URI.create("es://localhost:9300?es.cluster.name=test"))
                    .newSearchRequest()
                    .from(0)
                    .size(10)
                    .cql("Hello")
                    .executeSearch(queryLogger)
                    .toJSON(output);
            logger.info("result = {}", output.toString());
            assertTrue(output.toString().length() > 0);
            //es.deleteIndex();
        } catch (ClusterBlockException | NoNodeAvailableException | IndexMissingException e) {
            logger.warn(e.getMessage());
        }
    }

    private ResourceContext createContext() {
        ResourceContext context = new JsonLdContext()
                .newNamespaceContext();
        context.namespaceContext().addNamespace(ES.NS_PREFIX, ES.NS_URI);
        context.namespaceContext().addNamespace("urn", "http://urn");
        context.namespaceContext().addNamespace("dc", "http://purl.org/dc/terms/");
        Resource resource = context.newResource()
                .id(IRI.create("http://test?test#2"))
                .add("dc:title", "Hello")
                .add("dc:title", "World")
                .add("xbib:person", "Jörg Prante")
                .add("dc:subject", "An")
                .add("dc:subject", "example")
                .add("dc:subject", "for")
                .add("dc:subject", "subject")
                .add("dc:subject", "sequence")
                .add("http://purl.org/dc/terms/place", "Köln");
        resource.newResource("urn:res1")
                .add("property1", "value1")
                .add("property2", "value2");
        resource.newResource("urn:res1")
                .add("property3", "value3")
                .add("property4", "value4");
        resource.newResource("urn:res1")
                .add("property5", "value5")
                .add("property6", "value6");
        context.setResource(resource);
        return context;
    }
}
