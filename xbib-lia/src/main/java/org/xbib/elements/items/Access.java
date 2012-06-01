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

import java.util.LinkedList;

public class Access implements Node {

    private String name;
    private Authority authority;
    private final LinkedList<Library> libraries;
    private final LinkedList<Service> services;
    private final LinkedList<Item> items;

    public Access() {
        this.libraries = new LinkedList();
        this.services = new LinkedList();
        this.items = new LinkedList();
    }

    public Access name(String name, Authority authority) {
        this.name = name;
        this.authority = authority;
        return this;
    }

    public Access library(Library library) {
        libraries.add(library);
        return this;
    }

    public Access service(Service service) {
        services.add(service);
        return this;
    }

    public Access item(Item item) {
        items.add(item);
        return this;
    }

    public String getName() {
        return name;
    }

    public Authority getAuthority() {
        return authority;
    }

    public LinkedList<Library> getLibraries() {
        return libraries;
    }

    public LinkedList<Service> getServices() {
        return services;
    }

    public LinkedList<Item> getItems() {
        return items;
    }

    public PhysicalItem currentPhysicalItem() {
        if (items.isEmpty()) {
            PhysicalItem pi = new PhysicalItem();
            pi.type(ItemType.HARD_COVER).transport(TransportMethod.PHYSICAL).delivery(DeliveryMethod.PHYSICAL);
            item(pi);
            return pi;
        } else {
            return filter(items, isPhysical).getLast();
        }
    }
    
    public Access setIdentifier(String value) {
        getItems().getLast().identifier(value);
        return this;
    }

    public Access setCallNumber(String value) {
        currentPhysicalItem().number(value);
        return this;
    }

    public Access setCollection(String value) {
        currentPhysicalItem().collection(value);
        return this;
    }

    public Access setShelfMark(String value) {
        currentPhysicalItem().shelfmark(value);
        return this;
    }

    public Access setDescription(String value) {
        currentPhysicalItem().description(value);
        return this;
    }

    private static <S, T> LinkedList<T> filter(LinkedList<S> source, Predicate<S, T> p) {
        LinkedList<T> result = new LinkedList<T>();
        for (S s : source) {
            T t = p.apply(s);
            if (t != null) {
                result.add(t);
            }
        }
        return result;
    }
    private final Predicate<Item, PhysicalItem> isPhysical = new Predicate<Item, PhysicalItem>() {

        @Override
        public PhysicalItem apply(Item item) {
            return (PhysicalItem) (item instanceof PhysicalItem ? item : null);
        }
    };

    interface Predicate<S, T> {

        T apply(S s);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
