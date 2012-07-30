/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 * 
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.xbib.builders.berkeleydb;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xbib.berkeleydb.BerkeleyDBSession;
import org.xbib.berkeleydb.Write;
import org.xbib.elements.output.DefaultElementOutput;
import org.xbib.io.Mode;
import org.xbib.rdf.ResourceContext;

public class BerkeleyDBResourceOutput<C extends ResourceContext>
    extends DefaultElementOutput<C> {

    private static final Logger logger = Logger.getLogger(BerkeleyDBResourceOutput.class.getName());
    private BerkeleyDBSession session; 
    private Write write;
    private long counter;

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
                counter++;
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

    @Override
    public long getCounter() {
        return counter;
    }

}

