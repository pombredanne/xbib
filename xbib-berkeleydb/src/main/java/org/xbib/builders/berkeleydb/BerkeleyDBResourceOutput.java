package org.xbib.builders.berkeleydb;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xbib.berkeleydb.BerkeleyDBSession;
import org.xbib.berkeleydb.Write;
import org.xbib.elements.ElementContext;
import org.xbib.elements.output.DefaultElementOutput;
import org.xbib.io.Mode;
import org.xbib.rdf.Resource;

public class BerkeleyDBResourceOutput<C extends ElementContext>
    extends DefaultElementOutput<C> {

    private static final Logger logger = Logger.getLogger(BerkeleyDBResourceOutput.class.getName());
    private BerkeleyDBSession session; 
    private Write write;

    public void connect(URI uri) throws IOException {
         this.session =  new BerkeleyDBSession(uri);
         this.write = new Write();
         connect();
     }

    private void connect() {
        try {
            session.open(Mode.WRITE);
            if (!session.isOpen()) {
                logger.log(Level.SEVERE, "unable to open session {0}", session);
            } else {
                logger.log(Level.INFO, "session {0} created", session);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "I/O exception while opening session, reason: {1}",
                    new Object[]{e.getMessage()});
        }
    }

    public void disconnect() throws IOException {
        session.close();
    }
    
    @Override
    public void output(C context, Object info) {
        try {
            if (session.isOpen()) {
                write.write(session, context.resource());
                write.execute(session);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            try {
                session.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

}

