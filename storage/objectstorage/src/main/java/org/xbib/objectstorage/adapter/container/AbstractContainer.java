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
package org.xbib.objectstorage.adapter.container;

import org.xbib.objectstorage.Action;
import org.xbib.objectstorage.Container;
import org.xbib.objectstorage.ContainerInfo;
import org.xbib.objectstorage.ObjectStorageAdapter;
import org.xbib.objectstorage.action.ContainerGetAction;
import org.xbib.objectstorage.action.ContainerHeadAction;
import org.xbib.objectstorage.action.ItemGetAction;
import org.xbib.objectstorage.action.ItemHeadAction;
import org.xbib.objectstorage.action.ItemUpdateAction;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractContainer implements Container {

    private final static Logger logger = Logger.getLogger(AbstractContainer.class.getName());
    private final String name;
    private final String description;
    private ContainerInfo containerInfo;
    private final ResourceBundle actionBundle;

    public AbstractContainer(String name, String description, ResourceBundle bundle) {
        this.name = name;
        this.description = description;
        this.actionBundle = bundle;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public ResourceBundle getBundle() {
        return actionBundle;
    }

    @Override
    public String createPath(ObjectStorageAdapter adapter, String fileName) {
        return adapter.getRoot() + "/" + getName() + "/" + fileName;
    }

    @Override
    public ContainerInfo getContainerInfo(ObjectStorageAdapter adapter) {
        try {
            this.containerInfo = new ContainerInfo(adapter, this);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return containerInfo;
    }

    @Override
    public Action getContainerHeadAction() {
        return new ContainerHeadAction(actionBundle != null ? actionBundle.getString(name + "containerhead") : null);
    }

    @Override
    public Action getContainerByDateHeadAction() {
        return new ContainerHeadAction(actionBundle != null ? actionBundle.getString(name + "containerheadbydate") : null);
    }

    @Override
    public Action getContainerGetAction() {
        return new ContainerGetAction(actionBundle != null ? actionBundle.getString(name + "containerget") : null);
    }

    @Override
    public Action getContainerGetByDateAction() {
        return new ContainerGetAction(actionBundle != null ? actionBundle.getString(name + "containergetbydate") : null);
    }

    @Override
    public Action getItemHeadAction() {
        return new ItemHeadAction(actionBundle != null ? actionBundle.getString(name + "itemhead") : null);
    }

    @Override
    public Action getItemGetAction() {
        return new ItemGetAction(actionBundle != null ? actionBundle.getString(name + "itemget") : null);
    }

    @Override
    public Action getItemUpdateAction() {
        return new ItemUpdateAction(actionBundle != null ? actionBundle.getString(name + "itemupdate") : null);
    }
}
