/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Affero General Public License as published 
 * by the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses 
 * or write to the Free Software Foundation, Inc., 51 Franklin Street, 
 * Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * The interactive user interfaces in modified source and object code 
 * versions of this program must display Appropriate Legal Notices, 
 * as required under Section 5 of the GNU Affero General Public License.
 * 
 * In accordance with Section 7(b) of the GNU Affero General Public 
 * License, these Appropriate Legal Notices must retain the display of the 
 * "Powered by xbib" logo. If the display of the logo is not reasonably 
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.importer;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ImportService<T, R> {

    private ExecutorService executorService;
    private Collection<Callable<T>> tasks;
    private ImporterFactory<T, R> factory;
    private int threadnum = 1;
    private Map<Long, T> results = new HashMap();

    public ImportService() {
    }

    public ImportService setThreads(int threadnum) {
        this.threadnum = threadnum;
        return this;
    }

    public void shutdown(long millisToWait) throws InterruptedException {
        if (executorService != null) {
            executorService.awaitTermination(millisToWait, TimeUnit.MILLISECONDS);
            executorService.shutdown();
        }
    }

    public ImportService setFactory(ImporterFactory<T, R> factory) {
        this.factory = factory;
        return this;
    }

    /**
     * Submit tasks and wait for results
     * @throws InterruptedException
     * @throws ExecutionException 
     */
    public ImportService execute() throws InterruptedException, ExecutionException {
        submit();
        return waitFor();
    }

    /**
     * Submit tasks for later invocation
     */
    public ImportService submit() {
        if (factory == null) {
            throw new IllegalArgumentException("no factory set");
        }
        if (executorService == null) {
            this.executorService = Executors.newFixedThreadPool(threadnum);
        }
        if (tasks == null) {
            this.tasks = new LinkedList();
        }
        for (int i = 0; i < threadnum; i++) {
            tasks.add(factory.newImporter());
        }
        return this;
    }

    /**
     * Invoke all tasks and wait for all results
     * @return
     * @throws InterruptedException
     * @throws ExecutionException 
     */
    public ImportService waitFor() throws InterruptedException, ExecutionException {
        for (Future<T> f : executorService.invokeAll(tasks)) {
            results.put(System.currentTimeMillis(), f.get());
        }
        return this;
    }

    public Map<Long, T> getResults() {
        return results;
    }
}
