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
package org.xbib.berkeleydb;

import com.sleepycat.je.DatabaseEntry;
import java.io.IOException;
import org.xbib.io.Identifiable;

public class Delete {

    protected final DatabaseEntry keyEntry = new DatabaseEntry();
    protected final DatabaseEntry valueEntry = new DatabaseEntry();

    public void delete(BerkeleyDBSession session, Identifiable identifier) throws IOException {
        String key = identifier.getIdentifier().toString();
        if (session.getConfig().getTransactional() && !session.transactionStarted()) {
            session.beginTransaction();
        }
        keyEntry.setData(key.getBytes("UTF-8"));
        session.getDatabase().delete(session.getTransaction(), keyEntry);
    }

    public void flush(BerkeleyDBSession session) throws IOException {
    }

    public void execute(BerkeleyDBSession session) throws IOException {
        if (session.getConfig().getTransactional() && session.transactionStarted()) {
            session.commitTransaction();
        }
    }
}
