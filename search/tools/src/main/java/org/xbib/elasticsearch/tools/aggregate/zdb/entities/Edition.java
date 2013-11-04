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
package org.xbib.elasticsearch.tools.aggregate.zdb.entities;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class Edition extends Manifestation {

    private final String key;

    private final TreeSet<Manifestation> manifestations;

    public Edition(String key, Manifestation manifestation) {
        super(manifestation.map());
        this.key = key;
        this.manifestations = new TreeSet(getIdComparator());
        manifestations.add(manifestation);
    }

    public String getKey() {
        return key;
    }

    public void addManifestation(Manifestation manifestation) {
        manifestations.add(manifestation);
    }

    public TreeSet<Manifestation> getManifestations() {
        return manifestations;
    }

    public Set<String> allIDs() {
        Set<String> ids = new TreeSet();
        ids.add(id());
        for (Manifestation m : manifestations) {
            ids.add(m.id());
        }
        return ids;
    }

    public Set<String> allTargetIDs() {
        Set<String> ids = new TreeSet();
        ids.add(externalID());
        for (Manifestation m : manifestations) {
            ids.add(m.externalID());
        }
        return ids;
    }

    private final static CurrentComparator currentComparator = new CurrentComparator();

    private static class CurrentComparator implements Comparator<Edition> {

        @Override
        public int compare(Edition e1, Edition e2) {
            if (e1 == e2) {
                return 0;
            }
            Integer i1 = e1.getManifestations().first().lastDate();
            if (i1 == null) i1 = currentYear;
            Integer i2 = e2.getManifestations().first().lastDate();
            if (i2 == null) i2 = currentYear;
            return i2.compareTo(i1);
        }
    }

    public static Comparator<Edition> getCurrentComparator() {
        return currentComparator;
    }
}



