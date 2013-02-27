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

import org.xbib.keyvalue.KeyValue;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.context.ResourceContext;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.BlockingQueue;

public class KeyValuePipeline<K,V,E extends Element,C extends ResourceContext>
        implements Callable<Boolean> {
    private final BlockingQueue<List<KeyValue>> queue;
    private final ElementBuilder<K,V,E,C> builder;
    private final Map map;
    private long counter;
    protected boolean detectUnknownKeys;
    protected Set<String> unknownKeys;
    private final Logger logger;

    public KeyValuePipeline(int i,
                            BlockingQueue<List<KeyValue>> queue,
                            Map map,
                            ElementBuilderFactory<K, V, E, C> factory) {
        this.logger = LoggerFactory.getLogger("pipeline" + i);
        this.queue = queue;
        this.map = map;
        this.counter = 0L;
        this.builder = factory.newBuilder();
        this.unknownKeys = new TreeSet();
    }

    public KeyValuePipeline detectUnknownKeys(boolean enabled) {
        this.detectUnknownKeys = enabled;
        return this;
    }

    public Boolean call() {
        try {
            logger.info("key/value pipeline running");
            while(true) {
                List<KeyValue> e = queue.take();
                // poison element? then quit
                if (e.isEmpty()) {
                    logger.info("end of key/value pipeline");
                    if (!unknownKeys.isEmpty()) {
                        logger.info("unknown keys = {}", unknownKeys);
                    }
                    break;
                }
                // only a single marker element in list? then skip
                if (e.size() == 1) {
                    if (e.get(0).key() == null) {
                        continue;
                    }
                }
                builder.begin();
                boolean end = false;
                Iterator<KeyValue> it = e.iterator();
                while (it.hasNext()) {
                    KeyValue<K, V> kv = it.next();
                    K key = kv.key();
                    V value = kv.value();
                    if (key == null) {
                        builder.end(value);
                        end = true;
                    } else {
                        build(key, value);
                    }
                    counter++;
                }
                if (!end) {
                    builder.end();
                }
            }
        } catch (InterruptedException e1) {
            logger.warn("key/value pipeline interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e1) {
            logger.error("error in key/value pipeline, exiting", e1);
        }
        // nothing special
        return true;
    }

    public long getCounter() {
        return counter;
    }

    protected Map map() {
        return map;
    }

    protected ElementBuilder<K,V,E,C> builder() {
        return builder;
    }

    protected void build(K key, V value) {
    }

}
