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
 * A field in ISO 2709 records.
 */
public class Field implements Comparable {

    public final static String ERROR_TAG = "___";

    public final static String NULL_TAG = "000";

    private String tag;

    private String indicator;

    private final int position;

    private final int length;

    private String subfieldId;

    private String data;

    public Field() {
        this(null, null, null);
    }

    public Field(String tag) {
        this(tag, null, null);
    }

    public Field(String tag, char ind1) {
        this(tag, Character.toString(ind1), null);
    }

    public Field(String tag, char ind1, char ind2) {
        this(tag, Character.toString(ind1) + Character.toString(ind2), null);
    }

    public Field(String tag, char ind1, char ind2, char code) {
        this(tag, Character.toString(ind1) + Character.toString(ind2),
                Character.toString(code));
    }

    public Field(String tag, String indicator) {
        this(tag, indicator, null);
    }

    public Field(String tag, String indicator, String subfieldId) {
        this.tag = tag;
        this.indicator = indicator;
        this.subfieldId = subfieldId;
        this.data = null;
        this.position = -1;
        this.length = -1;
    }

    public Field(String tag, int position, int length) {
        this.tag = tag;
        this.indicator = null;
        this.subfieldId = null;
        this.data = null;
        this.position = position;
        this.length = length;
    }

    public Field(Field field) {
        this.tag = field.tag();
        this.indicator = field.indicator();
        this.subfieldId = field.subfieldId();
        this.position = field.position();
        this.length = field.length();
    }

    /**
     * Create field, try to derive tag, indicators, and subfield ID from rawContent
     *
     * @param label
     * @param rawContent
     */
    public Field(RecordLabel label, String rawContent) {
        this.tag = rawContent.length() > 2 ? rawContent.substring(0, 3) : ERROR_TAG;
        if (isControlField()) {
            this.indicator = null;
            this.subfieldId = null;
            this.data = rawContent.substring(3);
        } else {
            int indlen = label.getIndicatorLength();
            this.indicator = rawContent.length() > 2 + indlen ? rawContent.substring(3, 3 + indlen) : null;
            this.subfieldId = null; // no subfield
            this.data = rawContent.length() > 2 + indlen ? rawContent.substring(3 + indlen) : null;
        }
        this.position = -1;
        this.length = -1;
    }

    /**
     * Create field from a given designator and a subfield
     *
     * @param label
     * @param designator
     * @param content
     * @param subfield
     */
    public Field(RecordLabel label, Field designator, String content, boolean subfield) {
        this.tag = designator.tag();
        this.position = designator.position();
        this.length = designator.length();
        int indlen = label.getIndicatorLength();
        int subfieldidlen = label.getSubfieldIdentifierLength();
        if (subfield) {
            this.indicator = designator.indicator();
            if (subfieldidlen > 1) {
                this.subfieldId = content.substring(0, subfieldidlen - 1);
                this.data = content.substring(subfieldidlen - 1);
            } else {
                // no subfield identifier length specified
                this.subfieldId = null;
                this.data = content;
            }
        } else {
            if (designator.isControlField()) {
                this.indicator = null;
                this.subfieldId = null;
                this.data = content;
            } else {
                this.indicator = content.substring(0, indlen);
                this.subfieldId = null;
                this.data = content.substring(indlen);
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

    /**
     * Set a tag for this designator.
     *
     * @param tag a tag
     * @return this Field object
     */
    public Field tag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * Get this designator's tag
     *
     * @return
     */
    public String tag() {
        return tag;
    }

    /**
     * Set a sequence of indicators for this designator
     *
     * @param indicator the sequence of indicators
     * @return this Field object
     */
    public Field indicator(String indicator) {
        this.indicator = indicator;
        return this;
    }

    /**
     * Get indicator sequence.
     *
     * @return the indicator sequence
     */
    public String indicator() {
        return indicator;
    }

    /**
     * Set the dessignatos's sub field identifier.
     *
     * @param subfieldId the subfield identifier
     * @return this Field object
     */
    public Field subfieldId(String subfieldId) {
        this.subfieldId = subfieldId;
        return this;
    }

    /**
     * Get designator's subfield identifier
     *
     * @return the subfield identifier
     */
    public String subfieldId() {
        return subfieldId;
    }

    /**
     * The position of the field of this designator in the record. The position
     * unit is measured in octets.
     *
     * @return the field position
     */
    public int position() {
        return position;
    }

    /**
     * Return the length of the field for this designator in the record. The
     * field length is measured in octets.
     *
     * @return the field length
     */
    public int length() {
        return length;
    }

    /**
     * Set data for a data field.
     *
     * @param data
     * @return this Field object
     */
    public Field data(String data) {
        this.data = data;
        return this;
    }

    /**
     * Get the field data.
     *
     * @return the data
     */
    public String data() {
        return data;
    }

    protected String getDesignator() {
        StringBuilder sb = new StringBuilder();
        sb.append(tag).append(indicator != null ? indicator : "")
                .append(subfieldId != null ? subfieldId : "");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tag).append(indicator != null ? indicator : "")
                .append(subfieldId != null ? subfieldId : "")
                .append(data != null ? "=" + data : "");
        return sb.toString();
    }

    @Override
    public int compareTo(Object o) {
        return getDesignator().compareTo(((Field) o).getDesignator());
    }
}
