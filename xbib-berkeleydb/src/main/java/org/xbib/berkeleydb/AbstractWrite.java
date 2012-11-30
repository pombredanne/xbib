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
import com.sleepycat.je.OperationStatus;
import java.io.IOException;
import org.xbib.io.Identifiable;
import org.xbib.io.operator.CreateOperator;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;

public abstract class AbstractWrite<S extends Resource<?, ?, ?>, P extends Property, O extends Literal<?>>
        implements CreateOperator<BerkeleyDBSession, Identifiable, Resource<S, P, O>> {

    protected final DatabaseEntry keyEntry = new DatabaseEntry();
    protected final DatabaseEntry valueEntry = new DatabaseEntry();

    /**
     * Write into Berkeley DB. A cursor is placed within a transaction to the
     * appropriate key.
     *
     * @param session the session
     * @param resource the resource
     */
    @Override
    public void write(BerkeleyDBSession session, Resource resource) throws IOException {
        if (session == null || !session.isOpen()) {
            return;
        }
        if (resource == null) {
            return;
        }
        String key = resource.id().toString();
        if (isTransactionalWrite() && session.getConfig().getTransactional() && !session.transactionStarted()) {
            session.beginTransaction();
        }
        keyEntry.setData(key.getBytes("UTF-8"));
        session.getBinding().objectToEntry(resource, valueEntry);
        OperationStatus status = session.getCursor().put(keyEntry, valueEntry);
        if (status != OperationStatus.SUCCESS) {
            throw new IOException("value insertion got status " + status);
        }
    }

    @Override
    public void create(BerkeleyDBSession session, Identifiable identifier, Resource resource) throws IOException {
        write(session, resource);
    }
    
    
    @Override
    public void execute(BerkeleyDBSession session) throws IOException {
        if (isTransactionalWrite() && session.getConfig().getTransactional()) {
            session.commitTransaction();
        }
    }

    public abstract boolean isTransactionalWrite();
}
