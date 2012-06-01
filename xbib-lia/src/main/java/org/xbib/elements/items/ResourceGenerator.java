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
package org.xbib.elements.items;

import java.util.Stack;
import org.xbib.rdf.Resource;

public class ResourceGenerator implements LIA, Visitor {

    private Stack<Resource> stack;
    private Resource resource;

    private final String LIA = NS_URI + "lia";
    private final String ACCESS = NS_URI + "access";
    private final String NAME = NS_URI + "name";
    private final String AUTHORITY = NS_URI + "authority";
    private final String LIBRARY = NS_URI + "library";
    private final String SERVICE = NS_URI + "service";
    private final String ITEM = NS_URI + "item";
    private final String ITEM_ID = NS_URI + "identifier";
    private final String ITEM_TYPE = NS_URI + "type";
    private final String ITEM_PREFERRED_TRANSPORT = NS_URI + "preferredTransport";
    private final String ITEM_PREFERRED_DELIVERY = NS_URI + "preferredDelivery";
    private final String ITEM_COLLECTION = NS_URI + "collection";
    private final String ITEM_DESCRIPTION = NS_URI + "description";
    private final String ITEM_NUMBER = NS_URI + "number";
    private final String ITEM_SHELFMARK = NS_URI + "shelfmark";
    private final String ITEM_ADDRESS = NS_URI + "address";
    private final String ITEM_LABEL = NS_URI + "label";
    

    public ResourceGenerator(Resource resource) {
        this.resource = resource;
        this.stack = new Stack();
        stack.push(resource);        
    }

    public Resource getResult() {
        return stack.peek();
    }

    @Override
    public void visit(Root lia) {
        stack.push(resource);
        resource = resource.createResource(LIA);
        for (Access access : lia.getAccesses()) {
            access.accept(this);
        }
        resource = stack.pop();
    }    
    
    @Override
    public void visit(Access access) {
        stack.push(resource);
        resource = resource.createResource(ACCESS);
        resource.addProperty(NAME, access.getName());
        resource.addProperty(AUTHORITY, access.getAuthority().getToken());
        for (Library library : access.getLibraries()) {
            library.accept(this);
        }
        for (Service service : access.getServices()) {
            service.accept(this);
        }
        for (Item item : access.getItems()) {
            item.accept(this);
        }
        resource = stack.pop();
    }

    @Override
    public void visit(Library library) {
        stack.push(resource);
        resource = resource.createResource(LIBRARY);
        resource.addProperty(NAME, library.getName());
        resource.addProperty(AUTHORITY, library.getAuthority().getToken());
        resource = stack.pop();
    }

    @Override
    public void visit(Service service) {
        resource.addProperty(SERVICE, service.getToken());
    }

    @Override
    public void visit(Item item) {
        stack.push(resource);
        resource = resource.createResource(ITEM);
        resource.addProperty(ITEM_ID, item.getIdentifier());
        resource.addProperty(ITEM_TYPE, item.getType().getToken()  );
        resource.addProperty(ITEM_PREFERRED_TRANSPORT, item.getTransport().getToken() );
        resource.addProperty(ITEM_PREFERRED_DELIVERY, item.getDelivery().getToken() );
        if (item instanceof PhysicalItem) {
            PhysicalItem physical = (PhysicalItem) item;
            resource.addProperty(ITEM_COLLECTION, physical.getCollection());
            resource.addProperty(ITEM_DESCRIPTION, physical.getDescription());
            resource.addProperty(ITEM_SHELFMARK, physical.getShelfMark());
            for (String number : physical.getNumbers()) {
                resource.addProperty(ITEM_NUMBER, number);
            }
        } else if (item instanceof LocatorItem) {
            LocatorItem locatorItem = (LocatorItem) item;
            Locator locator = locatorItem.getLocator();
            resource.addProperty(ITEM_ADDRESS, locator.getAddress().toExternalForm()  );
            resource.addProperty(ITEM_LABEL, locator.getLabel());
        }
        resource = stack.pop();
    }

}
