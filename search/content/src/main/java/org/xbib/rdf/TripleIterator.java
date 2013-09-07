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
package org.xbib.rdf;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import org.xbib.rdf.simple.SimpleTriple;

/**
 * Iterate over a resource and generate statements 
 * 
 */
public class TripleIterator<S extends Identifier, P extends Property, O extends Node>
        implements Iterator<Triple> {

    private final LinkedList<Triple> triples;
    private final boolean includeResources;

    public TripleIterator(Resource<S, P, O> resource, boolean includeResources) {
        this.includeResources = includeResources;
        this.triples = unfold(resource);
    }

    @Override
    public boolean hasNext() {
        return !triples.isEmpty();
    }

    @Override
    public Triple next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return triples.poll();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private LinkedList<Triple> unfold(Resource<S, P, O> resource) {
        LinkedList<Triple> list = new LinkedList();
        if (resource == null) {
            return list;
        }
        S subj = resource.subject();
        for (P pred : resource.predicateSet(subj)) {
            for (O obj : resource.objects(pred)) {
                list.offer(new SimpleTriple(subj, pred, obj));
                if (includeResources && obj instanceof Resource) {
                    list.addAll(unfold((Resource<S,P,O>)obj));
                }
            }
        }
        return list;
    }

}
