package org.xbib.rdf.io;

import java.io.IOException;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.xml.transform.XMLFilterReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public interface XmlTriplifier<S extends Resource<S, P, O>, P extends Property, O extends Literal<O>>
    extends Triplifier<S,P,O> {
    XmlTriplifier parse(InputSource source) throws IOException, SAXException;
    
    XmlTriplifier parse(XMLReader reader, InputSource source) throws IOException, SAXException;
    
    XmlTriplifier parse(XMLFilterReader reader, InputSource source) throws IOException, SAXException;
    
    XmlHandler getHandler();
}
