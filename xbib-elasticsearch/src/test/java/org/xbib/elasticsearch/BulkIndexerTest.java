package org.xbib.elasticsearch;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.testng.annotations.Test;
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

public class BulkIndexerTest {

    private static final Logger logger = LoggerFactory.getLogger(BulkIndexerTest.class.getName());

    @Test
    public void test() throws Exception {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", "test").build();

        final ElasticsearchIndexer es =
                new ElasticsearchIndexer()
                        .newClient(false)
                        .setIndex("document")
                        .setType("test");

        es.deleteIndex();
        ResourceContext c = createContext();
        new ElasticsearchResourceSink(es).output(c);
        es.flush();
        Thread.sleep(2000);
        Logger queryLogger = LoggerFactory.getLogger("test", BulkIndexerTest.class.getName());
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        // check if IRI path "document" worked
        new Elasticsearch()
                .settings(settings)
                .newClient(false)
                .newRequest()
                .setIndex("document")
                .setType("test")
                .setFrom(0)
                .setSize(10)
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
        //es.deleteIndex();
    }

    private ResourceContext createContext() {
        ResourceContext context = new JsonLdContext().newNamespaceContext();
        context.namespaceContext().addNamespace(ES.NS_PREFIX, ES.NS_URI);
        context.namespaceContext().addNamespace("urn", "http://urn");
        context.namespaceContext().addNamespace("dc", "http://purl.org/dc/terms/");
        Resource resource = new SimpleResource()
                .context(context)
                .id(IRI.create("urn:document#1"))
                .property("dc:title", "Hello")
                .property("dc:title", "World")
                .property("xbib:person", "Jörg Prante")
                .property("dc:subject", "An")
                .property("dc:subject", "example")
                .property("dc:subject", "for")
                .property("dc:subject", "subject")
                .property("dc:subject", "sequence")
                .property("http://purl.org/dc/terms/place", "Köln");
        resource.newResource("urn:res1")
                .property("property1", "value1")
                .property("property2", "value2");
        resource.newResource("urn:res1")
                .property("property3", "value3")
                .property("property4", "value4");
        resource.newResource("urn:res1")
                .property("property5", "value5")
                .property("property6", "value6");
        context.newResource(resource);
        return context;
    }

}
