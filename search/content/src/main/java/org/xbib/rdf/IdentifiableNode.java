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

import java.util.concurrent.atomic.AtomicLong;
import org.xbib.iri.IRI;

/**
 * An identifiable node (including blank nodes)
 */
public class IdentifiableNode implements Identifier, Node {
    
    private final static AtomicLong nodeCounter = new AtomicLong();

    private final static String GENID = "genid";

    private final static String PLACEHOLDER = "_:";

    private IRI id;

    public IdentifiableNode() {
    }

    public IdentifiableNode blank() {
        id(GENID + nodeCounter.incrementAndGet());
        return this;
    }
    
    @Override
    public IdentifiableNode id(String id) {
        id(IRI.builder().curi(GENID, id).build());
        return this;
    }
    
    @Override
    public IdentifiableNode id(IRI id) {
        this.id = id;
        return this;
    }

    @Override
    public IRI id() {
        return id;
    }

    public boolean isBlank() {
        return GENID.equals(id.getScheme());
    }

    @Override
    public String toString() {
        if (id == null) {
            // blank
            blank();
        }
        return isBlank() ?
                PLACEHOLDER + id.getSchemeSpecificPart() : id.toString();
    }

    @Override
    public Object nativeValue() {
        return id;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    public static void reset() {
        nodeCounter.set(0L);
    }

    public static long next() {
        return nodeCounter.incrementAndGet();
    }
}
