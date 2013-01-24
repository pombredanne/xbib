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
package org.xbib.io.tar;

import java.net.URI;
import org.testng.annotations.Test;
import org.xbib.io.Connection;
import org.xbib.io.ConnectionService;
import org.xbib.io.Session;
import org.xbib.io.StringPacket;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class TarSessionTest {

    private static final Logger logger = LoggerFactory.getLogger(TarSessionTest.class.getName());

    @Test
    public void readFromTar() throws Exception {
        Connection<Session<StringPacket>> c = ConnectionService.getInstance()
                .getConnectionFactory("tarbz2")
                .getConnection(URI.create("tarbz2:src/test/resources/test/test"));
        Session<StringPacket> session = c.createSession();
        session.open(Session.Mode.READ);
        StringPacket message = session.read();
        logger.info("name = {} number = {} object = {}",
                message.name(), message.number(), message.packet());
        session.close();
        c.close();
    }
}
