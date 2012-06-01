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

import java.util.ArrayList;
import java.util.List;

/**
 * Modifier list. This is a recursive data structure with a Modifier and optionally a ModifierList.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ModifierList extends AbstractNode {

    private List<Modifier> modifierList = new ArrayList();

    public ModifierList(ModifierList modifiers, Modifier modifier) {
        modifierList.addAll(modifiers.modifierList);
        modifierList.add(modifier);
    }

    public ModifierList(Modifier modifier) {
        modifierList.add(modifier);
    }

    public List<Modifier> getModifierList() {
        return modifierList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Modifier m : modifierList) sb.append(m.toString());
        return sb.toString();
    }

}
