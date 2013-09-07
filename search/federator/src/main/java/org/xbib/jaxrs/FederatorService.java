package org.xbib.jaxrs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import org.xbib.federator.Federator;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

@Path("/federator")
public class FederatorService {

    private final static Logger logger = LoggerFactory.getLogger(FederatorService.class.getName());

    @Context
    ServletConfig servletConfig;

    Federator federator;

    @GET
    @Produces({"application/xml; charset=UTF-8"})
    public StreamingOutput getXml(@QueryParam("q") final String query)
            throws Exception {
        return query("application/xml", query);
    }

    @POST
    @Produces({"application/xml; charset=UTF-8"})
    public StreamingOutput postXml(@QueryParam("q") final String query)
            throws Exception {
        return query("application/xml", query);
    }

    @GET
    @Produces({"application/sru+xml; charset=UTF-8"})
    public StreamingOutput getSRU(@QueryParam("q") final String query)
            throws Exception {
        return query("application/sru+xml", query);
    }

    @POST
    @Produces({"application/sru+xml; charset=UTF-8"})
    public StreamingOutput postSRU(@QueryParam("q") final String query)
            throws Exception {
        return query("application/sru+xml", query);
    }
    
    @GET
    @Produces({"application/xhtml+xml; charset=UTF-8"})
    public StreamingOutput getXHTML(@QueryParam("q") final String query)
            throws Exception {
        return query(MediaType.APPLICATION_XHTML_XML, query);
    }

    @POST
    @Produces({"application/xhtml+xml; charset=UTF-8"})
    public StreamingOutput postXHTML(@QueryParam("q") final String query)
            throws Exception {
        return query(MediaType.APPLICATION_XHTML_XML, query);
    }
    
    private StreamingOutput query(final String mediaType,
            final String query) {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                Writer writer = new OutputStreamWriter(output, "UTF-8");
                try {
                    final String base = servletConfig.getInitParameter("federator.base");
                    final int threads = Integer.parseInt(servletConfig.getInitParameter("federator.threads"));
                    //final String stylesheet = servletConfig.getInitParameter(mediaType);
                    if (federator == null) {
                        federator = Federator.getInstance()
                                .setBase(base)
                                .setThreads(threads);
                                //.setStylesheetPath("xsl");
                    }
                    // write SRU XML response
                    //StylesheetTransformer transformer = new StylesheetTransformer("xsl");
                    federator.bibliographic(query)
                            .execute()
                            .toSRUResponse("1.2", writer);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    throw new IOException(e);
                }
            }
        };
    }
}
