package org.xbib.rdf.io.xml;

import org.xbib.rdf.io.TripleListener;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

public interface XmlHandler
     extends EntityResolver, DTDHandler, ContentHandler, ErrorHandler {

    XmlHandler setListener(TripleListener listener);

}
