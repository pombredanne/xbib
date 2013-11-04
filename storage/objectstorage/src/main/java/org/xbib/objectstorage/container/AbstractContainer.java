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
package org.xbib.objectstorage.container;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.objectstorage.Action;
import org.xbib.objectstorage.Container;
import org.xbib.objectstorage.action.ContainerGetAction;
import org.xbib.objectstorage.action.ContainerHeadAction;
import org.xbib.objectstorage.action.ItemGetAction;
import org.xbib.objectstorage.action.ItemHeadAction;
import org.xbib.objectstorage.action.ItemUpdateAction;

import javax.ws.rs.core.SecurityContext;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ResourceBundle;

public abstract class AbstractContainer implements Container {

    private final URI baseURI;
    private final ResourceBundle bundle;

    protected final static Logger logger = LoggerFactory.getLogger(AbstractContainer.class.getName());

    protected long objectCount;
    protected long totalSize;


    public AbstractContainer(URI baseURI, ResourceBundle bundle) {
        this.baseURI = baseURI;
        this.bundle = bundle;
    }

    @Override
    public URI getBaseURI() {
        return baseURI;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }


    @Override
    public Principal getPrincipal(SecurityContext context) {
        Principal p = context.getUserPrincipal();
        return p != null ? p : new Principal() {

            @Override
            public String getName() {
                return "anonymous";
            }
        };
    }

    public void setObjectCount(long count) {
        this.objectCount = count;
    }

    public long getObjectCount() {
        return objectCount;
    }

    public void setTotalSize(long length) {
        this.totalSize = length;
    }

    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public MessageDigest createMessageDigest()  {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Returns the size as a human readable string, rounding to the nearest
     * KB/MB/GB
     *
     * @return The size of the object as a human readable string.
     */
    public String getSizeString() {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (totalSize > gb) {
            return (totalSize / gb) + " GB";
        } else if (totalSize > mb) {
            return (totalSize / mb) + " MB";
        } else if (totalSize > kb) {
            return (totalSize / kb) + " KB";
        } else {
            return totalSize + " Bytes";
        }
    }

    /*public ItemInfo newItemInfo(String item) throws IOException {
        return new ItemInfo(this, item);
        // URI.create(container.getBaseURI() + URLEncoder.encode(item, "UTF-8")).toURL();
    }*/

    @Override
    public Action getContainerHeadAction() {
        return new ContainerHeadAction(bundle.getString("containerhead"));
    }

    @Override
    public Action getContainerGetAction() {
        return new ContainerGetAction(bundle.getString("containerget"));
    }

    @Override
    public Action getItemHeadAction() {
        return new ItemHeadAction(bundle.getString("itemhead"));
    }

    @Override
    public Action getItemGetAction() {
        return new ItemGetAction(bundle.getString("itemget"));
    }

    @Override
    public Action getItemUpdateAction() {
        return new ItemUpdateAction(bundle.getString("itemupdate"));
    }
}
