/**
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
package org.xbib.analyzer.mab.hbz.dialect;

import java.util.List;
import java.util.Map;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.items.Access;
import org.xbib.elements.items.Authority;
import org.xbib.elements.items.DeliveryMethod;
import org.xbib.elements.items.Item;
import org.xbib.elements.items.ItemType;
import org.xbib.elements.items.Library;
import org.xbib.elements.items.Service;
import org.xbib.elements.items.TransportMethod;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElement;
import org.xbib.elements.ValueMapFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;

public class Holding extends MABElement {

    private final static MABElement element = new Holding();

    private final static String defaultProvider = "DE-000";

    private final static Map<String, String> sigel2isil =
            ValueMapFactory.getAssocStringMap("/org/xbib/analyzer/pica/zdb/","sigel2isil");

    private final static Map<String, Map<String, List<String>>> product2isil =
            ValueMapFactory.getMap("/org/xbib/analyzer/pica/zdb/","product2isil");


    Holding() {
    }

    public static MABElement getInstance() {
        return element;
    }

    @Override
    public boolean fields(ElementBuilder<FieldCollection, String, MABElement, MABContext> builder,
                       FieldCollection fields, String value) {
        boolean servicecreated = false;
        for (Field field : fields) {
            switch (field.subfieldId()) {
                case "a":
                    createLibraryIdentifier(builder, field.data());
                    break;
                case "b": {
                    builder.context().getAccess().setCallNumber(field.data());
                    break;
                }
                case "c": {
                    builder.context().getAccess().setShelfMark(field.data());
                    break;
                }
                case "d": {
                    builder.context().getAccess().setDescription(field.data());
                    break;
                }
                case "e":
                    createItemService(builder, field.data());
                    servicecreated = true;
                    break;
            }
        }
        if (!servicecreated) {
            createItemService(builder, null);
        }
        return false;
    }

    private String createLibraryIdentifier(ElementBuilder<FieldCollection, String, MABElement, MABContext> builder, String value) {
        boolean libraryIdentifierResolved = false;
        if (product2isil.containsKey(value)) {
            for (String isil : product2isil.get(value).get("authorized")) {
                createISIL(builder, isil, value);
            }
            createISIL(builder, value, null);
            libraryIdentifierResolved = true;
        }
        String isil = sigel2isil.get(value);
        if (isil != null) {
            createISIL(builder, isil, null);
            libraryIdentifierResolved = true;
        } else {
            logger.warn("no ISIL found for {}", value);
        }
        if (!libraryIdentifierResolved) {
            throw new IllegalStateException("no ISIL for "+value+", can't continue");
        }
        return isil;
    }

    private void createISIL(ElementBuilder<FieldCollection, String, MABElement, MABContext> builder, String isil, String provider) {
        builder.context().resource()
                .add(XBIB_IDENTIFIER_AUTHORITY_ISIL, isil);
        if (provider == null) {
            provider = defaultProvider;
        }
        Authority authority = Authority.hbz;
        if (provider.startsWith("DE-")) {
            authority = Authority.ISIL;
        }
        builder.context()
                .access(new Access()
                        .name(provider, authority)
                        .library(new Library()
                                .library(isil, Authority.ISIL)));
    }

    private void createItemService(ElementBuilder<FieldCollection, String, MABElement, MABContext> builder, String itemStatus) {
        String format = "mab"; //b.context().getFormat();
        //boolean continuing = b.context().getContinuing();
        if (itemStatus == null) {
            // default service
            builder.context().getAccess().service(Service.INTER_LIBRARY_LOAN);
            builder.context().getAccess().service(Service.COPY);
            return;
        }
        if ("keine ILL".equals(itemStatus) || "keine Fernleihe".equals(itemStatus) || "Nicht ausleihbar".equals(itemStatus)) {
            builder.context().getAccess().service(Service.RESTRICTED);
        } else if ("Neuerwerbung".equals(itemStatus) || "In Bearbeitung".equals(itemStatus) || "Bestellt".equals(itemStatus) || "Neuerwerbungen".equals(itemStatus)) {
            builder.context().getAccess().service(Service.IN_PREPARATION);
        } else if ("Buchbinder".equals(itemStatus)) {
            builder.context().getAccess().service(Service.MAINTENANCE);
        } else if ("Ausgeschieden".equals(itemStatus) || "Ausgesondert".equals(itemStatus) || "Verlust".equals(itemStatus) || "Vermisst".equals(itemStatus) || "Löschen".equals(itemStatus)) {
            builder.context().getAccess().service(Service.WITHDRAWN);
        } else if ("Praesenzbestand".equals(itemStatus) || itemStatus.startsWith("Präsenz")) {
            builder.context().getAccess().service(Service.COPY);
        } else {
            builder.context().getAccess().service(Service.INTER_LIBRARY_LOAN);
            builder.context().getAccess().service(Service.COPY);
        }
        // well, it's not a "book", but what then?
        List<Item> l = builder.context().getAccess().getItems();
        if (l.isEmpty()) {
            return;
        }
        Item item = builder.context().getAccess().getItems().getLast();
        item.type(ItemType.UNKNOWN);
        if (format == null) {
            return;
        }
        // adjust transport item
        if ("microform".equals(format)) {
            item.type(ItemType.MICROFORM);
        } else if ("game".equals(format)) {
            item.type(ItemType.GAME);
        } else if ("map".equals(format)) {
            item.type(ItemType.MAP);
        } else if ("audio".equals(format)) {
            item.type(ItemType.AUDIO);
        } else if ("electronic".equals(format)) {
            item.type(ItemType.ELECTRONIC_RESOURCE);
        } else if ("online".equals(format)) {
            // change transport/delivery preference if an online item is cataloged
            item.type(ItemType.ELECTRONIC_RESOURCE);
            item.transport(TransportMethod.ELECTRONIC);
            item.delivery(DeliveryMethod.ELECTRONIC);
        } else if (format.matches(".*video.*") && format.matches(".*audio.*")) {
            item.type(ItemType.AUDIO_VISUAL);
        } else if (format.matches(".*video.*")) {
            item.type(ItemType.VIDEO);
        }
    }
}
