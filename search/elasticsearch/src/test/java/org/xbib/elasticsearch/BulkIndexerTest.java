package org.xbib.elasticsearch;

import org.elasticsearch.client.support.ingest.transport.TransportClientIngestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.elasticsearch.support.CQLRequest;
import org.xbib.elasticsearch.support.CQLSearchSupport;
import org.xbib.elasticsearch.support.Formatter;
import org.xbib.elasticsearch.support.OutputFormat;
import org.xbib.elasticsearch.support.OutputStatus;
import org.xbib.elasticsearch.xml.ES;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.JsonLdContext;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.xml.transform.StylesheetTransformer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
                    .index("document")
                    .type("test");

            es.deleteIndex();
            ResourceContext c = createContext();

            new ElasticsearchResourceSink(es).output(c);
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
                    .execute(queryLogger)
                    .context(c)
                    .format(OutputFormat.formatOf("application/json"))
                    .styleWith(new StylesheetTransformer("/xsl"), null, output)
                    .dispatchTo(new Formatter() {
                @Override
                public void format(OutputStatus status, OutputFormat format, byte[] message) throws IOException {
                    output.write(message);
                }
            });
            logger.info("result = {}", output.toString());
            assertTrue(output.toString().length() > 0);
            //es.deleteIndex();
        } catch (ClusterBlockException | NoNodeAvailableException | IndexMissingException e) {
            logger.warn(e.getMessage());
        }
    }

    private ResourceContext createContext() {
        ResourceContext context = new JsonLdContext()
                .id(IRI.create("http://test#1"))
                .newNamespaceContext();
        context.namespaceContext().addNamespace(ES.NS_PREFIX, ES.NS_URI);
        context.namespaceContext().addNamespace("urn", "http://urn");
        context.namespaceContext().addNamespace("dc", "http://purl.org/dc/terms/");
        Resource resource = new SimpleResource()
                .context(context)
                .id(IRI.create("urn:document#1"))
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
        context.newResource(resource);
        return context;
    }
}
