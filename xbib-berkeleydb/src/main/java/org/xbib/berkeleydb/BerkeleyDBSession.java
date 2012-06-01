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
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.BtreeStats;
import com.sleepycat.je.CheckpointConfig;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.Transaction;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xbib.io.Mode;
import org.xbib.io.Session;

/**
 * Berkeley Database session
 * 
 */
public class BerkeleyDBSession implements Session {

    private static final Logger logger = Logger.getLogger(BerkeleyDBSession.class.getName());

    private URI uri;

    /**
     * is this session open
     */
    private boolean isOpen;
    
    /**
     * is a transaction started
     */
    private boolean transactionStarted;
    /**
     * BDB environment
     */
    private Environment env;
    /**
     * Database configuration
     */
    private DatabaseConfig config;
    /**
     * Database
     */
    private Database db;
    /**
     * Transaction
     */
    private Transaction transaction;
    /**
     * Count commits
     */
    private long commitCounter;
    /**
     * Cursor
     */
    private Cursor cursor;
    /**
     * Binding
     */
    private EntryBinding binding;
    /**
     * Secondary database
     */
    private SecondaryDatabase secondarydb;
    /**
     * Get secondary config
     */
    private SecondaryConfig secondaryconfig;
    /**
     * Key creator for secondary database
     */
    private BerkeleyDBSecondaryKeyCreator keyCreator;
    /**
     * Container for database iterators
     */
    private List<BerkeleyDBIterator> iterators;

    /**
     * Creates a new BerkeleyDBSession object.
     */
    public BerkeleyDBSession(URI uri) {
        this.uri = uri;
        this.commitCounter = 0;
        this.iterators = new ArrayList();
    }

    /**
     * Open database
     *
     * @param mode
     * @throws IOException
     */
    @Override
    public void open(Mode mode) throws IOException {
        this.isOpen = false;
        // set different data bindings according to URI scheme
        if ("bdb".equals(uri.getScheme())) {
            setBinding(new StringBinding());
        } else if ("bdbresource".equals(uri.getScheme())) {
            ResourceTupleBinding resourceBinding = new ResourceTupleBinding(uri);
            setBinding(resourceBinding);
        }
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setReadOnly(false);
        envConfig.setTransactional(true);
        // primary database config
        config = new DatabaseConfig();
        config.setAllowCreate(true);
        config.setReadOnly(false);
        config.setTransactional(true);
        // duplicates are not allowed with a secondary database
        config.setSortedDuplicates(false);
        if (binding instanceof ResourceTupleBinding) {
            // secondary database config
            secondaryconfig = new SecondaryConfig();
            secondaryconfig.setAllowCreate(true);
            secondaryconfig.setReadOnly(false);
            secondaryconfig.setTransactional(true);
            secondaryconfig.setSortedDuplicates(true);
            keyCreator = new BerkeleyDBSecondaryKeyCreator();
            secondaryconfig.setKeyCreator(keyCreator);
        }
        switch (mode) {
            case DEFERRED_WRITE:
                config.setTransactional(false);
                config.setDeferredWrite(true);
                /* fall-through here */
            case WRITE:
                try {
                    // create directory if required
                    String path = uri.getSchemeSpecificPart();
                    File f = new File(path);
                    if (!f.exists()) {
                        f.mkdirs();
                    }
                    this.env = new Environment(f, envConfig);
                    this.db = env.openDatabase(null, "entries", config);
                    if (secondaryconfig != null) {
                        this.secondarydb = env.openSecondaryDatabase(null, "secondary", db, secondaryconfig);
                    }
                    this.isOpen = true;
                } catch (DatabaseException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
                break;
            case READ:
                try {
                    File f = new File(uri.getSchemeSpecificPart());
                    this.env = new Environment(f, envConfig);
                    this.db = env.openDatabase(null, "entries", config);
                    if (secondaryconfig != null) {
                        this.secondarydb = env.openSecondaryDatabase(null, "secondary", db, secondaryconfig);
                    }
                    this.isOpen = true;
                } catch (DatabaseException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
                break;
        }
    }

    /**
     * Close Berkeley DB
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        if (isOpen) {
            try {
                if (cursor != null) {
                    cursor.close();
                }
                if (transaction != null && transaction.isValid()) {
                    transaction.commitSync();
                }
                for (BerkeleyDBIterator it : iterators) {
                    it.close();
                }
                logger.log(Level.INFO, "cleaning up ...");
                // let's clean up the database
                boolean anyCleaned = false;
                while (env.cleanLog() > 0) {
                    anyCleaned = true;
                }
                if (anyCleaned) {
                    CheckpointConfig force = new CheckpointConfig();
                    force.setForce(true);
                    env.checkpoint(force);
                }
                logger.log(Level.INFO, "... cleaning done");
                if (secondarydb != null) {
                    secondarydb.close();
                }
                db.close();
            } catch (DatabaseException e) {
                throw new IOException(e.getMessage());
            } finally {
                this.isOpen = false;
            }
        }
    }

    /**
     * Is this database open?
     *
     * @return true if open
     */
    @Override
    public boolean isOpen() {
        return isOpen;
    }

    public boolean transactionStarted() {
        return transactionStarted;
    }
    
    public DatabaseConfig getConfig() {
        return config;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public Database getDatabase() {
        return this.db;
    }

    public SecondaryDatabase getSecondaryDatabase() {
        return this.secondarydb;
    }

    /**
     * Set data entry binding class for Berkeley DB
     *
     * @param binding the entry binding class
     */
    public void setBinding(EntryBinding binding) {
        this.binding = binding;
    }

    /**
     * Get binding
     *
     * @return the binding
     */
    public EntryBinding getBinding() {
        return binding;
    }

    /**
     * Create iterator over database. The iteraor is a slow, normal one,
     * with reading data values all the time.
     *
     * @return metadata iterator
     * @throws DatabaseException
     */
    public BerkeleyDBIterator createIterator() throws DatabaseException {
        BerkeleyDBIterator it = new BerkeleyDBIterator(db, binding);
        iterators.add(it);
        return it;
    }

    /**
     * Begin transaction
     * @throws DatabaseException
     */
    public void beginTransaction() throws DatabaseException {
        if (config.getTransactional()) {
            transaction = db.getEnvironment().beginTransaction(null, null);
            cursor = db.openCursor(transaction, CursorConfig.DEFAULT);
        } else {
            cursor = db.openCursor(null, CursorConfig.DEFAULT);
        }
        this.transactionStarted = true;
    }

    /**
     * Commit transaction
     * By default, Berkeley DB doesn't write to disk after every operation
     * (e.g., what's stored to disk can be out of sync with the database in
     * memory). In most cases, you have to explicitly tell Berkeley DB to
     * synchronize to disk. When you are committing a transaction, you can
     * opt for one of the following: commit, commitNoSync, or commitWithSync.
     * commit lets Berkeley DB decide whether to synchronize or not
     * (depending on the database and environment configuration);
     * commitNoSync and commitWithSync let you decide explicitly whether
     * or not the synchronization needs to occur.
     * @throws DatabaseException
     */
    public void commitTransaction() throws DatabaseException {
        if (cursor != null) {
            cursor.close();
        }
        if (config.getTransactional()) {
            if (transaction != null && transaction.isValid()) {
                transaction.commit();
            } else {
                transaction.abort();
            }
        }
        commitCounter++;
        this.transactionStarted = false;
    }

    /**
     * Abort transaction
     * @throws DatabaseException
     */
    public void abortTransaction() throws DatabaseException {
        if (cursor != null) {
            cursor.close();
        }
        if (config.getTransactional()) {
            if (transaction != null) {
                transaction.abort();
            }
        }
        this.transactionStarted = false;
    }
    
    public Transaction getTransaction() {
        return transaction;
    }

    /**
     * Get number of commits
     * @return
     */
    public long getCommits() {
        return commitCounter;
    }

    /**
     * Get leaf node count
     * @return leaf node count
     * @throws DatabaseException
     */
    public long getLeafNodeCount() throws DatabaseException {
        BtreeStats stats = (BtreeStats) db.getStats(null);
        return stats.getLeafNodeCount();
    }

}
