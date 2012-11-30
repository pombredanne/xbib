package org.xbib.tools.indexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import org.xbib.elasticsearch.ElasticsearchIndexerDAO;
import org.xbib.elements.output.ElementOutput;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.file.FileConnectionFactory;
import org.xbib.io.file.Finder;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;

public class ElasticsearchCatalogEnrichmentIndexer extends AbstractImporter<Long, AtomicLong> {

    private final static  Logger logger = LoggerFactory.getLogger(ElasticsearchCatalogEnrichmentIndexer.class.getName());
    private static Queue<URI> input;
    private final static AtomicLong fileCounter = new AtomicLong(0L);
    private final SimpleResourceContext ctx = new SimpleResourceContext();
    private ElementOutput out;
    private boolean done = false;

    public static void main(String[] args) {
        int exitcode = 0;
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.txt");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("bulksize").withRequiredArg().ofType(Integer.class).defaultsTo(1000);
                    accepts("bulks").withRequiredArg().ofType(Integer.class).defaultsTo(10);
                    accepts("help");
                }
            };
            final OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("ElasticsearchCEIndexer");
                System.err.println("--elasticsearch <uri>");
                System.err.println("--index <name>");
                System.err.println("--type <name>");
                System.err.println("--path <uri>");
                System.err.println("--pattern <pattern>");
                System.err.println("--threads <num>");
                System.err.println("--bulksize <num>");
                System.err.println("--bulks <num>");
                System.exit(1);
            }

            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();
            logger.info("found {} input files", input.size() );
            final Integer threads = (Integer) options.valueOf("threads");

            final ElasticsearchIndexerDAO es = new ElasticsearchIndexerDAO();
                    es.newClient(URI.create(options.valueOf("elasticsearch").toString()), false)
                    .setIndex(options.valueOf("index").toString())
                    .setType(options.valueOf("type").toString())
                            .setBulkSize((Integer)options.valueOf("bulksize"))
                            .setMaxActiveRequests((Integer)options.valueOf("bulks"));

            logger.info("connected to ES with bulk size {} and max bulks {}",
                    (Integer)options.valueOf("bulksize"),
                    (Integer)options.valueOf("bulks"));
            
            ImportService service = new ImportService().setThreads(threads).setFactory(
                    new ImporterFactory() {
                        @Override
                        public Importer newImporter() {
                            return new ElasticsearchCatalogEnrichmentIndexer(es);
                        }
                    }).execute();
            
            logger.info("finished, number of files = {}, docs indexed = {}", fileCounter, es.getCounter());
            
            es.flush();
            es.shutdown();

        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            exitcode = 1;
        }
        System.exit(exitcode);
    }

    public ElasticsearchCatalogEnrichmentIndexer(ElementOutput out) {
        this.out = out;
    }

    @Override
    public void close() throws IOException {
        // do not clear input
    }

    @Override
    public boolean hasNext() {
        if (input.isEmpty()) {
            done = true;
        }
        return !done && !input.isEmpty();
    }

    @Override
    public AtomicLong next() {
        if (done) {
            return fileCounter;            
        }
        try {
            URI uri = input.poll();
            if (uri != null) {
                push(uri);
            } else {
                done = true;
            }
            fileCounter.incrementAndGet();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            done = true;
        }
        return fileCounter;
    }
    FileConnectionFactory factory = new FileConnectionFactory();

    private void push(URI uri) throws Exception {
        if (uri == null) {
           return;
        }    
        InputStream in = factory.getInputStream(uri);
        if (in == null) {
            throw new IOException("unable to open " + uri);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String title = null;
            String author = null;
            String year = null;
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (title == null && line.startsWith("Titel:")) {
                    title = line.substring("Titel:".length()).trim();
                } else if (author == null && line.startsWith("Autor:")) {
                    author = line.substring("Autor:".length()).trim();
                } else if (year == null && line.startsWith("Jahr:")) {
                    year = line.substring("Jahr:".length()).trim();
                } else if (line.startsWith("ocr-text:")) {
                    sb.append(line.substring("ocr-text:".length()).trim()).append(" ");
                } else {                    
                    sb.append(line).append(" ");
                }
            }
            String id = uri.getPath();
            if (id.endsWith(".txt")) {
                int pos = id.lastIndexOf("/");
                id = pos >= 0 ? id.substring(pos + 1) : id;
                // remove .txt and force uppercase
                id = id.substring(0, id.length() - 4).toUpperCase();
                URI identifier = URI.create("urn:hbz#" + id);
                Resource resource = ctx.newResource();
                resource.id(identifier)
                        .property("dc:title", title)
                        .property("dc:creator", author)
                        .property("dc:date", year)
                        .newResource("dc:description")
                            .property("dcterms:tableOfContents", sb.toString());

                out.output(ctx);
            }
        }
    }
}
