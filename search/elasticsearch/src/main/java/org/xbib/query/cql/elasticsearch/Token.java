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
package org.xbib.query.cql.elasticsearch;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.xbib.date.DateUtil;
import org.xbib.query.Filter;
import org.xbib.query.QuotedStringTokenizer;
import org.xbib.query.UnterminatedQuotedStringException;
import org.xbib.query.cql.SyntaxException;

/**
 * Elasticsearch query tokens
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class Token implements Node {

    public enum TokenClass {

        NORMAL, ALL, WILDCARD, BOUNDARY, PROTECTED
    }
    private TokenType type;
    private String value;
    private Boolean booleanvalue;
    private Long longvalue;
    private Double doublevalue;
    private Date datevalue;
    private List<Date> dates;
    private List<String> values;
    private final EnumSet<TokenClass> tokenClass;

    public Token(String value) {
        this.value = value;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
        this.type = TokenType.STRING;
        // if this string is equal to true/false or on/off or yes/no, convert silently to bool
        switch (value) {
            case "true":
            case "yes":
            case "on":
                this.booleanvalue = true;
                this.value = null;
                this.type = TokenType.BOOL;
                break;
            case "false":
            case "no":
            case "off":
                this.booleanvalue = false;
                this.value = null;
                this.type = TokenType.BOOL;
                break;
        }
        if (this.value != null) {
            // protected?
            if (value.startsWith("\"") && value.endsWith("\"")) {
                this.value = value.substring(1, value.length() - 1).replaceAll("\\\\\"", "\"");
                this.values = parseQuot(this.value);
                tokenClass.add(TokenClass.PROTECTED);
            }
            // wildcard?
            if (this.value.indexOf('*') >= 0 || this.value.indexOf('?') >= 0) {
                tokenClass.add(TokenClass.WILDCARD);
                // all?
                if (this.value.length() == 1) {
                    tokenClass.add(TokenClass.ALL);
                }
                this.value = value.toLowerCase(); // wildcard does not analyze, but lowercase is expected
            }
            // prefix?
            if (this.value.charAt(0) == '^') {
                tokenClass.add(TokenClass.BOUNDARY);
                this.value = this.value.substring(1);
            }
        }        
    }

    public Token(Boolean value) {
        this.booleanvalue = value;
        this.type = TokenType.BOOL;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
    }

    public Token(Long value) {
        this.longvalue = value;
        this.type = TokenType.INT;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
    }

    public Token(Double value) {
        this.doublevalue = value;
        this.type = TokenType.FLOAT;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
    }

    public Token(Date value) {
        this.datevalue = value;
        // this will enforce dates to get formatted as long values (years)
        this.longvalue = Long.parseLong(DateUtil.formatDate(datevalue, "yyyy"));
        this.type = TokenType.DATETIME;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
    }

    public String getString() {        
        return value;
    }

    public Boolean getBoolean() {
        return booleanvalue;
    }

    public Long getInteger() {
        return longvalue;
    }

    public Double getFloat() {
        return doublevalue;
    }

    public Date getDate() {
        return datevalue;
    }

    public List<Date> getDates() {
        return dates;
    }

    public List<String> getStringList() {
        return values;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (booleanvalue != null) {
            sb.append(booleanvalue);
        } else if (longvalue != null) {
            sb.append(longvalue);
        } else if (doublevalue != null) {
            sb.append(doublevalue);
        } else if (datevalue != null) {
            sb.append(DateUtil.formatDateISO(datevalue));
        } else if (value != null) {
            sb.append(value);
        }
        return sb.toString();
    }

    public boolean isProtected() {
        return tokenClass.contains(TokenClass.PROTECTED);
    }

    public boolean isBoundary() {
        return tokenClass.contains(TokenClass.BOUNDARY);
    }

    public boolean isWildcard() {
        return tokenClass.contains(TokenClass.WILDCARD);
    }

    public boolean isAll() {
        return tokenClass.contains(TokenClass.ALL);
    }

    private List<String> parseQuot(String s) {
        LinkedList l = new LinkedList();
        try {
            Filter.filter(new QuotedStringTokenizer(s, " \t\n\r\f", "\"", '\\', false), l, isWordPred);
        } catch (UnterminatedQuotedStringException e) {
        }
        return l;
    }

    private static class IsWordPredicate implements Filter.Predicate<String, String> {

        @Override
        public String apply(String s) {
            return s == null || s.length() == 0 || word.matcher(s).matches() ? null : s;
        }
    }
    private final static IsWordPredicate isWordPred = new IsWordPredicate();
    private final static Pattern word = Pattern.compile("[\\P{IsWord}]");
}
