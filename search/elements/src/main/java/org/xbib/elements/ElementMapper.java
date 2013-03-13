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
package org.xbib.elements;

import org.xbib.classloader.URIClassLoader;
import org.xbib.keyvalue.KeyValue;
import org.xbib.keyvalue.KeyValueStreamListener;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.context.ResourceContext;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class ElementMapper<K, V, E extends Element, C extends ResourceContext>
        implements KeyValueStreamListener<K, V>, Closeable {

    private final Logger logger = LoggerFactory.getLogger(ElementMapper.class.getName());
    protected final BlockingQueue<List<KeyValue>> queue;
    protected final Map map;
    protected ElementBuilderFactory<K, V, E, C> factory;
    protected Set<KeyValuePipeline> pipelines;
    private ExecutorService service;
    private LinkedList<KeyValue> keyvalues;
    private int numPipelines;
    private boolean detectUnknownKeys;

    public ElementMapper(String path, String format) {
        this(new URIClassLoader(), path, format);
    }

    public ElementMapper(ClassLoader cl, String path, String format) {
        this.queue = new SynchronousQueue<>(true);
        this.pipelines = new HashSet();
        try {
            this.map = ElementMap.getElementMap(cl, path, format);
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Map map() {
        return map;
    }

    public ElementMapper pipelines(int numPipelines) {
        this.numPipelines = numPipelines;
        return this;
    }

    public ElementMapper detectUnknownKeys(boolean enabled) {
        this.detectUnknownKeys = enabled;
        return this;
    }

    public Set<KeyValuePipeline> pipelines() {
        return pipelines;
    }

    public ElementMapper start(ElementBuilderFactory factory) {
        logger.info("starting {}", this);
        if (numPipelines == 0) {
            numPipelines = 1;
        }
        // restrict numPipelines to a reasonable value
        if (numPipelines >= 100) {
            numPipelines = 100;
        }
        if (service == null) {
            this.service = Executors.newFixedThreadPool(numPipelines);
        }
        this.factory = factory;
        for (int i = 0; i < numPipelines; i++) {
            KeyValuePipeline pipeline = createPipeline(i)
                    .detectUnknownKeys(detectUnknownKeys);
            pipelines.add(pipeline);
            service.submit(pipeline);
        }
        return this;
    }

    public void close() {
        if (service == null) {
            return;
        }
        for (int i = 0; i < numPipelines; i++) {
            try {
                queue.offer(new LinkedList(), 1, TimeUnit.SECONDS); // send poison element to all numPipelines
            } catch (InterruptedException e) {
                logger.error("interrupted while close()");
            }
        }
        try {
            service.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            service.shutdownNow();
        }
        logger.info("closed");
    }

    @Override
    public void begin() {
        keyvalues = new LinkedList();
    }

    @Override
    public void keyValue(K key, V value) {
        keyvalues.add(new KeyValue(key, value));
    }

    @Override
    public void end() {
        try {
            queue.offer((List<KeyValue>) keyvalues.clone(), 2, TimeUnit.SECONDS);
            keyvalues.clear();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void end(Object info) {
        try {
            // add marker element
            if (keyvalues != null) {
                keyvalues.add(new KeyValue(null, info));
                // move shallow copy of key/values to pipeline, this ensures thread safety
                queue.offer((List<KeyValue>) keyvalues.clone(), 2, TimeUnit.SECONDS);
                keyvalues.clear();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected KeyValuePipeline createPipeline(int i) {
        return new KeyValuePipeline(i, queue, map, factory);
    }

    /**
     * Helper method for diagnosing unknown keys.
     *
     * @return
     */
    public String unknownKeys() {
        Set<String> unknownKeys = new TreeSet();
        for (KeyValuePipeline p : pipelines()) {
            unknownKeys.addAll(p.unknownKeys);
        }
        StringBuilder sb = new StringBuilder();
        String lastKey = "   ";
        for (String key : unknownKeys) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            if (!key.startsWith(lastKey.substring(0,3))) {
                sb.append("\n");
            }
            sb.append("\"").append(key).append("\"");
            lastKey = key;
        }
        return sb.toString();
    }

}
