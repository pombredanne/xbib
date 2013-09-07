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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.xbib.federator.action.Action;
import org.xbib.federator.action.PQFZAction;
import org.xbib.federator.action.SRUAction;

public class Federator {

    private final static Federator instance = new Federator();
    private ExecutorService executorService;
    private String base;

    private Federator() {
    }

    public static Federator getInstance() {
        return instance;
    }

    /**
     * Set the number pf threads and create the ExecutorService.
     * @param threadnum
     * @return 
     */
    public Federator setThreads(int threadnum) {
        if (executorService == null && threadnum > 0) {
            this.executorService = Executors.newFixedThreadPool(threadnum);
        }
        return this;
    }

    public Federator setBase(String base) {
        this.base = base;
        return this;
    }

    public void shutdown(long millisToWait) throws InterruptedException {
        if (executorService != null) {
            executorService.awaitTermination(millisToWait, TimeUnit.MILLISECONDS);
            executorService.shutdown();
        }
    }

    /**
     * Submit JSON query for bibliographic federation. Create job from a
     * JSON specification.
     *
     * @param json the query
     * @return
     * @throws IOException
     */
    public FederatorRequest bibliographic(String json)
            throws IOException, InterruptedException, ExecutionException, NoSuchAlgorithmException {
        return bibliographic(null, json);
    }
    
    /**
     * Submit JSON query for bibliographic federation. Create job tasks from a
     * JSON specification and a given job ID.
     *
     * @param jobId
     * @param json
     * @return
     * @throws IOException
     */
    public FederatorRequest bibliographic(String jobId, String json)
            throws IOException, InterruptedException, ExecutionException, NoSuchAlgorithmException {
        if (jobId == null) {
            jobId = digest(json);
        }
        ArrayList<HashMap<String, Object>> specs = null;
        try {
            specs = new ObjectMapper().readValue(json, ArrayList.class);
        } catch (Exception e) {
            throw new IOException(e);
        }
        List<Action> actions = new ArrayList();
        for (Map<String, Object> params : specs) {
            String type = (String) params.get("type");
            switch (type) {
                case "z3950":
                    actions.add(new PQFZAction().setParams(params).setBase(base));
                    break;
                case "sru":
                    actions.add(new SRUAction().setParams(params).setBase(base));
                    break;
            }
        }
        return execute(jobId, actions);
    }

    /**
     * Submit actions for federation.
     * 
     * @param jobId
     * @param actions
     * @return
     * @throws InterruptedException
     * @throws ExecutionException 
     */
    public FederatorRequest execute(String jobId, List<Action> actions)
            throws InterruptedException, ExecutionException {
        if (jobId == null) {
            throw new ExecutionException("no job ID set", null);
        }
        if (executorService == null) {
            throw new ExecutionException("no executor service", null);
        }
        FederatorRequest federatorRequest = new FederatorRequest(jobId, executorService);
        for (Action action : actions) {
            //LinkedList<XMLEvent> events = new LinkedList();
            //SearchRetrieveResponse response = new SearchRetrieveResponse();
            //response.setEvents(events);
            action.setGroup(jobId);
            //action.setResponse(response);
            //action.setTransformer(new StylesheetTransformer(stylesheetPath));
            federatorRequest.add(action);
        }
        return federatorRequest;
    }
    
    private String digest(String input) 
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(Integer.toHexString(b & 0xff));
        }
        return sb.toString();
    }

}
