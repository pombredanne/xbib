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

import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.io.IOException;
import org.xbib.io.Identifiable;
import org.xbib.io.ResultProcessor;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;

public class Update<S extends Resource<?, ?, ?>, P extends Property, O extends Literal<?>>
        extends AbstractWrite<S, P, O>
         {

    public void update(BerkeleyDBSession session, Identifiable identifier, Resource<S, P, O> resource,
        ResultProcessor<Resource<S, P, O>> processor) throws IOException {
        if (session == null || !session.isOpen()) {
            return;
        }
        if (resource == null) {
            return;
        }
        String key = resource.id().toString();
        try {
            keyEntry.setData(key.getBytes("UTF-8"));
            // before update, enforce a commit() and begin a new transaction
            session.commitTransaction();
            session.beginTransaction();
            // read resource, start processor, write resource
            if (session.getCursor().getSearchKey(keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                Resource thisResource = (Resource) session.getBinding().entryToObject(valueEntry);
                if (processor != null) {
                    processor.process(thisResource);
                }
                session.getBinding().objectToEntry(thisResource, valueEntry);
                // update cursor
                OperationStatus status = session.getCursor().putCurrent(valueEntry);
                if (status != OperationStatus.SUCCESS) {
                    throw new IOException("resource update got status " + status);
                }
            }
        } finally {
            session.commitTransaction();
        }
    }

    @Override
    public boolean isTransactionalWrite() {
        return true;
    }

    public void flush(BerkeleyDBSession session) throws IOException {
        session.commitTransaction();
    }

}
