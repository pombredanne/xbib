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

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.Transaction;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xbib.io.Session;
import org.xbib.rdf.Resource;

public class WriteDuplicates extends AbstractWrite {

    private static final Logger logger = Logger.getLogger(WriteDuplicates.class.getName());
    private String uniqueKey;
    /**
     * Duplicate key summary
     */
    private final Map<Integer, Integer> dups = new TreeMap<Integer, Integer>();
    ;
    /**
     * Weak key store
     */
    private final Map<String, Set<String>> weakkeys = new TreeMap<String, Set<String>>();

    public void setUniqueKey(String key) {
        this.uniqueKey = key;
    }

    @Override
    public boolean isTransactionalWrite() {
        return true;
    }

    @Override
    public void write(BerkeleyDBSession session, Resource resource) throws IOException {
        String key = resource.getIdentifier().toString();
        SecondaryCursor secondarycursor = null;
        Transaction transaction = null;
        try {
            DatabaseEntry seckey = new DatabaseEntry(uniqueKey.getBytes("UTF-8"));
            if (isTransactionalWrite() && session.getConfig().getTransactional()) {
                transaction = session.getSecondaryDatabase().getEnvironment().beginTransaction(null, null);
            }
            secondarycursor = session.getSecondaryDatabase().openCursor(transaction, CursorConfig.DEFAULT);
            OperationStatus status = secondarycursor.getSearchKey(seckey, keyEntry, valueEntry, LockMode.DEFAULT);
            if (status != OperationStatus.SUCCESS) {
                throw new IOException("search for unique key " + uniqueKey + " got status " + status);
            }
            Integer count = secondarycursor.count();
            dup(count);
            // do we have duplicates?
            if (count == 2) {
                // We need to store two keys. Store this duplicate
                addWeak(uniqueKey, key);
                // Get the key of the other duplicate and store it.
                if (secondarycursor.getNextDup(seckey, keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                    addWeak(uniqueKey, key);
                }
            } else if (count > 2) {
                // more than two duplicates, just append key.
                addWeak(uniqueKey, key);
            }
        } catch (IllegalStateException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } finally {
            if (transaction != null) {
                transaction.commit();
            }
            if (secondarycursor != null) {
                secondarycursor.close();
            }
        }
    }

    @Override
    public void flush(Session session) throws IOException {
        // not needed
    }

    /**
     * Helper method for managing duplicate summary.
     * @param n the occurrences of a duplicate key
     */
    private synchronized void dup(Integer n) {
        try {
            if (n > 1 && dups.containsKey(n - 1)) {
                // decrease predecessing duplicate counter, if n > 1
                Integer m = dups.get(n - 1) - 1;
                if (m > 0) {
                    dups.put(n - 1, m);
                } else {
                    dups.remove(n - 1);
                }
            }
            // increase current duplicate counter
            dups.put(n, (dups.containsKey(n) ? dups.get(n) : 0) + 1);
        } catch (Exception e) {
            // this warning appears when duplicate summary is dubios, e.g. second runs
            logger.log(Level.WARNING, "wrong summary when counting dup = " + n + ", summary = " + dups);
        }
    }

    /**
     * Helper method for adding a "weak" key to a set of primary keys. This method
     * updates the weak key store with a new member of a weak key set.
     *
     * @param weakkey the weak key
     * @param member the new member of the weak key set
     */
    private void addWeak(String weakkey, String member) {
        Set<String> members = weakkeys.get(weakkey);
        if (members == null) {
            // a linked hash set is used to store the primary keys in order as they appear
            members = new LinkedHashSet<String>();
            members.add(member);
            weakkeys.put(weakkey, members);
        } else {
            members.add(member);
        }
    }
}
