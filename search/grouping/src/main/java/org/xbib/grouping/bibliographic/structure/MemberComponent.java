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
package org.xbib.grouping.bibliographic.structure;

import org.xbib.grouping.bibliographic.GroupDomain;
import org.xbib.grouping.bibliographic.GroupKeyComponent;

import java.util.LinkedHashSet;

/**
 * A bibliographic member component can denote more precisely an
 * expression, manfestation, or item, which serves as a member of
 * an author/title cluster.
 * <p/>
 * A member statement is typically a volume title, an edition statement,
 * or a reference of an article in a journal.
 * <p/>
 * Because the characteristics
 * of the member classes are so different, they are not encoded except
 * that all characters not being a number or a letter are removed in order
 * to ensure a valid URI construction.
 * <p/>
 * The usage of member components depends on application specific tasks.
 * For accomplishing this mor easily, a domain parameter has been added.
 * The default domain is "G" for generic member.
 *
 */
public class MemberComponent extends LinkedHashSet<String>
        implements GroupKeyComponent<String> {

    private char delimiter = '/';

    public GroupDomain getDomain() {
        return GroupDomain.GENERIC;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public boolean isUsable() {
        return !isEmpty();
    }

    /**
     * Add generic member component
     *
     * @param value
     */
    @Override
    public boolean add(String value) {
        return this.add(GroupDomain.GENERIC, value);
    }

    public boolean add(GroupDomain domain, Comparable c) {
        if (c == null || domain == null) {
            return false;
        }
        // strip any character which is not a number or letter 
        // from a member statement to ensure a valid URI
        String s = c.toString().replaceAll("[^\\p{L}\\p{N}]", "");
        return s.length() > 0 ? super.add(domain + s) : false;
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        for (String s : this) {
            sb.append(s).append(delimiter);
        }
        int len = sb.length();
        if (len > 0) {
            sb.deleteCharAt(len - 1);
        }
        return sb.toString();
    }
}
