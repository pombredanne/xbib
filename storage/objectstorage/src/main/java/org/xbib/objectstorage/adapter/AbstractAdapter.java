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
package org.xbib.objectstorage.adapter;

import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.ws.rs.core.SecurityContext;
import org.xbib.objectstorage.Action;
import org.xbib.objectstorage.Container;
import org.xbib.objectstorage.ItemInfo;
import org.xbib.objectstorage.ObjectStorageAPI;
import org.xbib.objectstorage.ObjectStorageAdapter;

public abstract class AbstractAdapter implements ObjectStorageAdapter {

    private URI baseURI;
    private ResourceBundle bundle;
    private Map<String, Container> containers = new HashMap();

    @Override
    public ObjectStorageAdapter init() {
        if (getStatementBundleName() != null) {
            this.bundle = ResourceBundle.getBundle(getStatementBundleName());
        }
        this.containers = new HashMap();
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    disconnect();
                } catch (IOException ex) {
                }
            }
        });
        return this;
    }

    @Override
    public String getDefaultContainerName() {
        return bundle.getString("container_name");
    }

    @Override
    public void connect(URI baseURI) throws IOException {
        if (baseURI == null) {
            throw new IOException("base URI is null");
        }
        this.baseURI = baseURI.resolve(ObjectStorageAPI.VERSION);
    }

    @Override
    public void disconnect() throws IOException {
    }

    @Override
    public URI getBaseURI() {
        return baseURI;
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

    public final void addContainer(Container container) {
        containers.put(container.getName(), container);
    }

    @Override
    public Container getContainer(String container) throws IOException {
        ensureContainer(container);
        return containers.get(container);
    }

    @Override
    public Action getContainerHeadAction(String container) throws IOException {
        ensureContainer(container);
        return getContainer(container).getContainerHeadAction();
    }

    @Override
    public Action getContainerHeadByDateAction(String container) throws IOException {
        ensureContainer(container);
        return getContainer(container).getContainerByDateHeadAction();
    }

    @Override
    public Action getContainerGetAction(String container) throws IOException {
        ensureContainer(container);
        return getContainer(container).getContainerGetAction();
    }

    @Override
    public Action getContainerGetByDateAction(String container) throws IOException {
        ensureContainer(container);
        return getContainer(container).getContainerGetByDateAction();
    }

    @Override
    public Action getItemHeadAction(String container) throws IOException {
        ensureContainer(container);
        return getContainer(container).getItemHeadAction();
    }

    @Override
    public Action getItemGetAction(String container) throws IOException {
        ensureContainer(container);
        return getContainer(container).getItemGetAction();
    }

    @Override
    public Action getItemUpdateAction(String container) throws IOException {
        ensureContainer(container);
        return containers.get(container).getItemUpdateAction();
    }

    @Override
    public Action getItemJournalAction(String container, ItemInfo itemInfo) throws IOException {
        ensureContainer(container);
        return containers.get(container).getItemJournalAction(itemInfo);
    }

    @Override
    public boolean canUploadTo(String mimeType, String container) {
        return containers.get(container).canUpload(mimeType);
    }

    private void ensureContainer(String container) throws IOException {
        if (!containers.containsKey(container)) {
            throw new IOException("container '" + container + "' does not exist, available containers: " + containers.keySet());
        }
    }

    @Override
    public MessageDigest getMessageDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256");
    }

    @Override
    public ItemInfo newItemInfo(String container, String item) throws IOException {
        return ItemInfo.newInfo(this, getContainer(container), item);
    }

    @Override
    public abstract String getRoot();

    public abstract String getDriverClassName();

    public abstract String getUser();
    
    public abstract String getPassword();
    
    public abstract String getConnectionSpec();

    public abstract String getStatementBundleName();

    public abstract DirContext getDirContext() throws NamingException;
}
