package org.xbib.jaxrs;

import java.io.IOException;
import java.io.OutputStream;
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
import org.xbib.federator.FederatorAction;

@Path("/service")
public class QueryPage extends FederatorAction {

    @GET
    @POST
    @Produces({"application/json; charset=UTF-8","application/xml; charset=UTF-8"})
    public StreamingOutput queryPage(
            @Context final HttpServletResponse response,
            @HeaderParam("content-type") final String mediaType, 
            @QueryParam("q") final String query)
            throws Exception {
        return query(response, mediaType, query);
    }
    
    private StreamingOutput query(
            final HttpServletResponse response,
            final String mediaType,
            final String query
            ) {
        return new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                setQuery(query);
                setMimeType(mediaType);
                setTarget(output);
                search();
            }
        };        
    }
    
}
