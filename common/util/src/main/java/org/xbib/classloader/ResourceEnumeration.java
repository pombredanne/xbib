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
package org.xbib.classloader;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ResourceEnumeration implements Enumeration {

    private Iterator iterator;
    private final String resourceName;
    private Object next;

    public ResourceEnumeration(Collection resourceLocations, String resourceName) {
        this.iterator = resourceLocations.iterator();
        this.resourceName = resourceName;
    }

    public boolean hasMoreElements() {
        fetchNext();
        return (next != null);
    }

    public Object nextElement() {
        fetchNext();

        // save next into a local variable and clear the next field
        Object next = this.next;
        this.next = null;

        // if we didn't have a next throw an exception
        if (next == null) {
            throw new NoSuchElementException();
        }
        return next;
    }

    private void fetchNext() {
        if (iterator == null) {
            return;
        }
        if (next != null) {
            return;
        }

        try {
            while (iterator.hasNext()) {
                ResourceLocation resourceLocation = (ResourceLocation) iterator.next();
                ResourceHandle resourceHandle = resourceLocation.getResourceHandle(resourceName);
                if (resourceHandle != null) {
                    next = resourceHandle.getUrl();
                    return;
                }
            }
            // no more elements
            // clear the iterator so it can be GCed
            iterator = null;
        } catch (IllegalStateException e) {
            // Jar file was closed... this means the resource finder was destroyed
            // clear the iterator so it can be GCed
            iterator = null;
            throw e;
        }
    }
}
