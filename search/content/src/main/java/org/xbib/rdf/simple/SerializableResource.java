package org.xbib.rdf.simple;

import java.io.Serializable;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;

public class SerializableResource<S extends Identifier, P extends Property, O extends Node> 
   extends SimpleResource<S,P,O> implements Serializable {
    
}
