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

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xbib.rdf.Resource;

/**
 * A BDB metadata iterator is a Berkeley DB cursor over resource
 * wrapped into an Iterator interface
 *
 */
public class BerkeleyDBIterator implements Iterator<Resource>, Closeable {

    private DatabaseEntry key;
    private DatabaseEntry value;
    private Cursor cursor;
    private OperationStatus status;
    private EntryBinding binding;

    /**
     * Duplicate key statistics
     */
    private HashMap<Integer, Integer> dupkeystat;

    public BerkeleyDBIterator(Database db, EntryBinding binding)
            throws DatabaseException {
        if (db == null) {
            throw new IllegalArgumentException("unable to initialize Berkeley DB iterator");
        }
        if (binding == null) {
            throw new IllegalArgumentException("unable to use empty binding in Berkeley DB iterator");
        }
        this.binding = binding;
        this.key = new DatabaseEntry();
        this.value = new DatabaseEntry();
        this.cursor = db.openCursor(null, null);
        this.dupkeystat = new HashMap<Integer, Integer>();
    }

    @Override
    public boolean hasNext() {
        try {
            this.status = cursor.getNext(key, value, LockMode.DEFAULT);
            Integer n = cursor.count();
            dupkeystat.put(n, (dupkeystat.containsKey(n) ? dupkeystat.get(n) : 0) + 1);
            return status == OperationStatus.SUCCESS;
        } catch (DatabaseException e) {
            return false;
        }
    }

    @Override
    public Resource next() {
        return status == OperationStatus.SUCCESS ? (Resource) binding.entryToObject(value) : null;
    }

    public String getKey() {
        return new String(key.getData());
    }

    public int getDuplicateKeys() throws DatabaseException {
        return cursor.count();
    }

    public Map<Integer,Integer> getDupKeyStat() {
        return dupkeystat;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws IOException {
        try {
            cursor.close();
        } catch (DatabaseException ex) {
            IOException e = new IOException(ex.getMessage());
            e.setStackTrace(ex.getStackTrace());
            throw e;
        }
    }

}
