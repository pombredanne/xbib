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
 *  Abstract syntax tree of CQL - Boolean Group
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class BooleanGroup extends AbstractNode {

    private BooleanOperator op;
    private ModifierList modifiers;

    BooleanGroup(BooleanOperator op, ModifierList modifiers) {
        this.op = op;
        this.modifiers = modifiers;
    }

    BooleanGroup(BooleanOperator op) {
        this.op = op;
    }

    public BooleanOperator getOperator() {
        return op;
    }

    public ModifierList getModifierList() {
        return modifiers;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return op != null && modifiers != null ? op + modifiers.toString()
                : op != null ? op.toString() : null;
    }
}
