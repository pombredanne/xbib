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
package org.xbib.marc;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * A linked list of ISO 2709 fields
 */
public class FieldCollection extends LinkedList<Field> {

    public final static FieldCollection FORMAT_KEY = new FieldCollection("FORMAT");

    public final static FieldCollection TYPE_KEY = new FieldCollection("TYPE");

    public final static FieldCollection LEADER_KEY = new FieldCollection("LEADER");

    public FieldCollection() {
        super();
    }

    private FieldCollection(String tag) {
        this();
        super.add(new Field(tag));
    }

    public void format() {
        int s = size();
        String[] tags = new String[s];
        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < s; i++) {
            sb.append(tags[i]);
        }
    }

    /**
     * Build a pattern of this field collection for matching
     * @param map
     */
    public void makePattern(Map<String, String[]> map) {
        StringBuilder pattern = new StringBuilder();
        // walk through sorted designators
        String tag = null;
        String[] ind = null;
        String sub = null;
        for (Field field : this) {
            if (tag == null) {
                tag = field.tag();
            }
            int l = field.indicator() != null ? field.indicator().length() : 0;
            if (ind == null && l > 0) {
                ind = new String[field.indicator().length()];
                for (int i = 0; i < l; i++) {
                    ind[i] = field.indicator().substring(i, i + 1);
                }
            }
            if (sub == null) {
                sub = field.subfieldId();
            }
            if (!tag.equals(field.tag())) {
                // unequal tags are very unlikely when parsing MARC
                switchToNextTag(map, pattern, tag, ind, sub);
                tag = field.tag();
                ind = null;
                sub = null;
            } else {
                // new indicator?
                if (ind != null) {
                    for (int i = 0; i < l; i++) {
                        char ch = field.indicator().charAt(i);
                        int pos = ind[i].indexOf(ch);
                        if (pos < 0) {
                            ind[i] = ind[i] + ch;
                        }
                    }
                }
                // new subfield id?
                if (sub != null && sub.indexOf(field.subfieldId()) < 0) {
                    sub = sub + field.subfieldId();
                }
            }
        }
        // last tag
        if (tag != null) {
            switchToNextTag(map, pattern, tag, ind, sub);
        }
    }

    private void switchToNextTag(Map<String, String[]> map,
                                 StringBuilder pattern, String tag, String[] ind, String sub) {
        if (pattern.length() > 0) {
            pattern.append('|');
        }
        pattern.append(tag);
        String p = pattern.toString();
        // merge with pattern map, if any
        if (map != null) {
            int l = ind != null ? ind.length : 0;
            String[] v = new String[l+1];
            if (ind != null) {
                for (int i = 0; i < l; i++) {
                    v[i] = ind[i];
                }
            }
            if (sub != null) {
                v[l] = sub;
            }
            if (!map.containsKey(p)) {
                map.put(p, v);
            } else {
                // melt
                String[] s = map.get(p);
                if (s != null) {
                    // melt indicators
                    if (ind != null) {
                        for (int i = 0; i < l; i++) {
                            if (s[i].indexOf(ind[i]) < 0) {
                                s[i] += ind[i];
                            }
                        }
                    }
                    // melt subfield
                    if (sub != null) {
                        for (int i = 0; i < sub.length(); i++) {
                            if (s[l].indexOf(sub.charAt(i)) < 0) {
                                s[l] += sub.charAt(i);
                            }
                        }
                    }
                    map.put(p,s);
                } else {
                    map.put(p,v);
                }
            }
        }
    }

    public String toSpec() {
        Map<String,String[]> m = new TreeMap();
        makePattern(m);
        StringBuilder sb = new StringBuilder();
        for (String k : m.keySet()) {
            sb.append(k);
            String[] values = m.get(k);
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    String v = values[i];
                    sb.append('$');
                    if (v != null) {
                        // sort characters is slow
                        char[] ch = v.toCharArray();
                        Arrays.sort(ch);
                        sb.append(ch);
                    }
                }
            }
        }
        return sb.toString();
    }

    public String toString() {
        return toSpec();
    }

}
