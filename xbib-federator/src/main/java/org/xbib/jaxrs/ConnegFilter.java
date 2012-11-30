package org.xbib.jaxrs;

import com.sun.jersey.api.container.filter.UriConnegFilter;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;

public class ConnegFilter extends UriConnegFilter {
  private static final Map<String, MediaType> mappedMediaTypes = new HashMap(2);

  static {
    mappedMediaTypes.put("xhtml", MediaType.APPLICATION_XHTML_XML_TYPE);
    mappedMediaTypes.put("xml", MediaType.APPLICATION_XML_TYPE);
    mappedMediaTypes.put("sru", MediaType.valueOf("application/sru+xml"));
  }

  public ConnegFilter() {
    super(mappedMediaTypes);
  }
}