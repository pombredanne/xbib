package org.xbib.federator;

import java.util.Collection;
import java.util.concurrent.Callable;
import javax.xml.stream.events.XMLEvent;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.xml.transform.StylesheetTransformer;

public interface Action extends Callable<Action> {

    void setGroup(String group);

    Action setResponse(SearchRetrieveResponse response);

    SearchRetrieveResponse getResponse();
    
    Action setTransformer(StylesheetTransformer transformer);
    
    long getCount();
}
