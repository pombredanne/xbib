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
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.io.IOException;
import org.xbib.io.operator.QueryOperator;
import org.xbib.rdf.Resource;

public abstract class AbstractRead implements QueryOperator<BerkeleyDBSession> {

    protected final DatabaseEntry keyEntry = new DatabaseEntry();
    protected final DatabaseEntry valueEntry = new DatabaseEntry();
    protected Resource resource;
    
    @Override
    public void query(BerkeleyDBSession session, String key) throws IOException {
       try {
            keyEntry.setData(key.getBytes("UTF-8"));
            session.setCursor(session.getDatabase().openCursor(null, null));
            this.resource = session.getCursor().getSearchKey(keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS
                    ? (Resource) session.getBinding().entryToObject(valueEntry) : null;
        } finally {
            session.getCursor().close();
        }        
    }

    @Override
    public void execute(BerkeleyDBSession session) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Resource getResource() {
        return resource;
    }
    
}
