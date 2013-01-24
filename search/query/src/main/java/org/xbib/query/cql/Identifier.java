/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 * 
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.xbib.query.cql;

/**
 * An Identifier is a SimpleName or a String in double quotes.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class Identifier extends AbstractNode {

    private String value;
    private boolean quoted;

    public Identifier(String value) {
        this.value = value;
        this.quoted = true;
    }

    public Identifier(SimpleName name) {
        this.value = name.getName();
        this.quoted = false;
    }

    public String getValue() {
        return value;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return value != null && quoted ? "\"" + value.replaceAll("\"", "\\\\\"") + "\"" : value;
    }
}
