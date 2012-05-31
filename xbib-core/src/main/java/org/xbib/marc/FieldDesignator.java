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

/**
 * A designator for content in ISO 2709 files
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class FieldDesignator {

    private final String tag;
    private final String indicator;
    private final int position;
    private final int length;
    private String subfieldId;
    private String data;

    public FieldDesignator(String tag) {
        this(tag, null, null);
    }

    public FieldDesignator(String tag, char ind1) {
        this(tag, Character.toString(ind1), null);
    }

    public FieldDesignator(String tag, char ind1, char ind2) {
        this(tag, Character.toString(ind1) + Character.toString(ind2), null);
    }

    public FieldDesignator(String tag, char ind1, char ind2, char code) {
        this(tag, Character.toString(ind1) + Character.toString(ind2),
            Character.toString(code));
    }

    public FieldDesignator(String tag, String indicator) {
        this(tag, indicator, null);
    }

    public FieldDesignator(String tag, String indicator, String subfieldId) {
        this.tag = tag;
        this.indicator = indicator;
        this.subfieldId = subfieldId;
        this.data = null;
        this.position = -1;
        this.length = -1;
    }

    public FieldDesignator(RecordLabel label) {
        this.tag = null;
        this.indicator = null;
        this.subfieldId = null;
        this.data = null;
        this.position = -1;
        this.length = -1;
    }

    public FieldDesignator(RecordLabel label, String tag, int position, int length) {
        this.tag = tag;
        this.indicator = null;
        this.subfieldId = null;
        this.data = null;
        this.position = position;
        this.length = length;
    }

    /**
     * Create field with tag and indicators in the data
     *
     * @param label
     * @param data
     */
    public FieldDesignator(RecordLabel label, String data) {
        this.tag = data.length() > 2 ? data.substring(0, 3) : null;
        if (isControlField()) {
            this.data = data.substring(3);
            this.indicator = null;
            this.subfieldId = null;
        } else {
            this.indicator = data.length() > 2 + label.getIndicatorLength() ? data.substring(3, 3 + label.getIndicatorLength()) : null;
            this.subfieldId = null; // no subfield
            this.data = data.length() > 2 + label.getIndicatorLength() ? data.substring(3 + label.getIndicatorLength()) : null;
        }
        this.position = -1;
        this.length = -1;
    }

    /**
     * Create field from a given designator and a subfield
     *
     * @param label
     * @param designator
     * @param data
     * @param subfield
     */
    public FieldDesignator(RecordLabel label, FieldDesignator designator, String data, boolean subfield) {
        this.tag = designator.getTag();
        this.position = designator.getPosition();
        this.length = designator.getLength();
        if (subfield) {
            this.indicator = designator.getIndicator();
            if (label.getSubfieldIdentifierLength() > 1) {
                this.subfieldId = data.substring(0, label.getSubfieldIdentifierLength() - 1);
                this.data = data.substring(label.getSubfieldIdentifierLength() - 2);
            } else {
                // no subfield identifier length specified
                this.subfieldId = null; // "a";
                this.data = data;
            }
        } else {
            if (designator.isControlField()) {
                this.data = data;
                this.indicator = null;
                this.subfieldId = null;
            } else {
                this.indicator = data.substring(0, label.getIndicatorLength());
                this.data = data.substring(label.getIndicatorLength());
                this.subfieldId = null;
            }
        }
    }

    public boolean isEmpty() {
        return tag == null || (data != null && data.isEmpty());
    }

    public final boolean isControlField() {
        return tag != null && tag.charAt(0) == '0' && tag.charAt(1) == '0';
    }

    public boolean isSubField() {
        return subfieldId != null;
    }

    public void setData(String data) {
        this.data = data;
    }
    
    public String getData() {
        return subfieldId != null && data != null && data.length() > 0 ? data.substring(1) : data;
    }
    
    public String getTag() {
        return tag;
    }

    public String getIndicator() {
        return indicator;
    }

    public void setSubfieldId(String subfieldId) {
        this.subfieldId = subfieldId;
    }
    
    public String getSubfieldId() {
        return subfieldId;
    }

    public int getPosition() {
        return position;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tag).append(indicator != null ? indicator : "").append(subfieldId != null ? subfieldId : "");
        return sb.toString();
    }
}
