package org.xbib.jaxrs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.stream.XMLStreamException;
import org.xbib.federator.Federator;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

@Path("/")
public class FederatorService {

    private final static Logger logger = LoggerFactory.getLogger(FederatorService.class.getName());
    @Context
    ServletConfig servletConfig;
    @Context
    ServletContext servletContext;
    @Context
    HttpServletRequest servletRequest;
    Federator federator;

    @GET
    @Produces({"application/xml; charset=UTF-8"})
    public StreamingOutput queryGet(
            @Context final HttpServletResponse response,
            @HeaderParam("content-type") final String mediaType,
            @QueryParam("q") final String query)
            throws Exception {
        return query(response, mediaType, query);
    }

    @POST
    @Produces({"application/xml; charset=UTF-8"})
    public StreamingOutput queryPost(
            @Context final HttpServletResponse response,
            @HeaderParam("content-type") final String mediaType,
            @QueryParam("q") final String query)
            throws Exception {
        return query(response, mediaType, query);
    }

    private StreamingOutput query(
            final HttpServletResponse response,
            final String mediaType,
            final String query) {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                Writer writer = new OutputStreamWriter(output, "UTF-8");
                try {
                    if (federator == null) {
                        final String base = servletConfig.getInitParameter("federator.base");
                        final int threads = Integer.parseInt(servletConfig.getInitParameter("federator.threads"));
                        federator = Federator.getInstance()
                                .setBase(base)
                                .setThreads(threads)
                                .setStylesheetPath("xsl");
                    }
                    // write SRU XML response
                    federator.bibliographic(query).execute().toSRUResponse("1.2", writer);
                } catch (InterruptedException | ExecutionException | NoSuchAlgorithmException | XMLStreamException e) {
                    logger.error(e.getMessage(), e);
                    throw new IOException(e);
                }
            }
        };
    }
}
