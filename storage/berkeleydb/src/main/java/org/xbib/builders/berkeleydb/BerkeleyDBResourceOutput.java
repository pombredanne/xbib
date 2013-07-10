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
import org.xbib.berkeleydb.BerkeleyDBSession;
import org.xbib.berkeleydb.Write;
import org.xbib.analyzer.output.DefaultElementOutput;
import org.xbib.io.Session;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.context.ResourceContext;

public class BerkeleyDBResourceOutput<C extends ResourceContext>
    extends DefaultElementOutput<C> {

    private static final Logger logger = LoggerFactory.getLogger(BerkeleyDBResourceOutput.class.getName());
    private BerkeleyDBSession session; 
    private Write write;
    private long counter;

    public void connect(URI uri) throws IOException {
         this.session =  new BerkeleyDBSession(IRI.create(uri.toString()));
         this.write = new Write();
         connect();
     }

    private void connect() {
        try {
            session.open(Session.Mode.WRITE);
            if (!session.isOpen()) {
                logger.error("unable to open session {}", session);
            } else {
                logger.info("session {} created", session);
            }
        } catch (IOException e) {
            logger.warn("I/O exception while opening session, reason: {}",
                    e.getMessage());
        }
    }

    public void disconnect() throws IOException {
        session.close();
    }
    
    @Override
    public void output(C context) throws IOException {
        try {
            if (session.isOpen()) {
                write.write(session, context.resource());
                write.execute(session);
                counter++;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            try {
                session.close();
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
            throw e;
        }
    }

    @Override
    public long getCounter() {
        return counter;
    }

}

