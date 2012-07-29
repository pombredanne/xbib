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
import org.xbib.io.ConnectionManager;
import org.xbib.io.Mode;
import org.xbib.io.Packet;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class TarSessionTest {

    private static final Logger logger = LoggerFactory.getLogger(TarSessionTest.class.getName());

    @Test
    public void readFromTar() throws Exception {
        Connection<TarSession> conn = (Connection<TarSession>)ConnectionManager.getConnection(URI.create("tarbz2:src/test/resources/test/test"));
        TarSession session = conn.createSession();
        session.open(Mode.READ);
        TarEntryReadOperator op = new TarEntryReadOperator();
        Packet message = op.read(session);
        logger.info("name = {0} number = {1} link = {2} object = {3}", 
                message.getName(), message.getNumber(), message.getLink(), message.toString());
        session.close();
        conn.close();
    }
}
