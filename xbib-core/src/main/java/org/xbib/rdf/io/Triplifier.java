package org.xbib.rdf.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;

public interface Triplifier<S extends Resource<S, P, O>, P extends Property, O extends Literal<O>> {

    Triplifier setListener(StatementListener<S, P, O> listener);
    
    StatementListener<S, P, O> getListener();
            
    Triplifier parse(InputStream in) throws IOException;
    
    Triplifier parse(Reader reader) throws IOException;
    
}
