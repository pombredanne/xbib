package org.xbib.federator;

import java.io.IOException;
import java.io.OutputStream;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class FederatorAction
        
        implements Federator {

    private final static Logger logger = LoggerFactory.getLogger(FederatorAction.class.getName());
    OutputStream target;
    String mimeType;
    String query;

    @Override
    public Federator setMimeType(String mimetype) {
        this.mimeType = mimetype;
        return this;
    }

    @Override
    public Federator setTarget(OutputStream target) {
        this.target = target;
        return this;
    }

    @Override
    public Federator setQuery(String query) {
        this.query = query;
        return this;
    }

    @Override
    public OutputStream getTarget() {
        return target;
    }
    private final FederatorService service = FederatorService.getInstance().setThreads(100);

    @Override
    public Federator search() throws IOException {
       
        return this;
    }

}
