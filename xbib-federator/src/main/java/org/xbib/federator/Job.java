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
package org.xbib.federator;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import javax.xml.stream.events.XMLEvent;
import org.xbib.federator.action.Action;

public class Job {

    private String id;
    private ExecutorService service;
    private List<Action> actions;
    private List<ResponseListener<Action>> listeners;

    public Job(String id, ExecutorService service) {
        this.id = id;
        this.service = service;
        this.actions = new LinkedList();
        this.listeners = new LinkedList();
    }

    public String getID() {
        return id;
    }

    public Job add(Action action) {
        actions.add(action);
        return this;
    }

    /**
     * Add listener
     *
     * @param listener
     */
    public Job addListener(ResponseListener<Action> listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * Remove listener
     *
     * @param listener
     */
    public Job removeListener(ResponseListener<Action> listener) {
        listeners.remove(listener);
        return this;
    }

    public List<ResponseListener<Action>> getListeners() {
        return listeners;
    }

    /**
     * Invoke job and wait for responses. Call the listeners for this group if
     * response arrive.
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public Result execute() throws InterruptedException, ExecutionException {
        long count = 0L;
        LinkedList<XMLEvent> events = new LinkedList();
        for (Future<Action> f : service.invokeAll(actions)) {
            Action action = f.get();
            count += action.getCount();
            events.addAll(action.getResponse().getEvents());
            for (ResponseListener<Action> listener : listeners) {
                listener.onResponse(action);
            }
        }
        return new Result(count, events);
    }
}
