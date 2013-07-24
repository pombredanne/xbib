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

import java.io.IOException;
import java.util.Stack;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.xbib.query.cql.SyntaxException;

/**
 * Build sort in JSON syntax from abstract syntax tree
 * 
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class SortGenerator implements Visitor {

    private final XContentBuilder builder;

    private final Stack<ESModifier> modifiers;

    public SortGenerator() throws IOException {
        this.builder = XContentFactory.jsonBuilder();
        this.modifiers = new Stack<>();
    }

    public void start() throws IOException {
        builder.startArray();
    }

    public void end() throws IOException {
        builder.endArray();
    }

    public XContentBuilder getResult() throws IOException {
        builder.close();
        return builder;
    }

    @Override
    public void visit(Token node) {
    }

    @Override
    public void visit(ESName node) {
        try {
            if (modifiers.isEmpty()) {
                builder.value(node.getName().toString());
            } else {
                builder.startObject().field(node.getName().toString()).startObject();
                while (!modifiers.isEmpty()) {
                    ESModifier mod = modifiers.pop();
                    switch (mod.getName().toString()) {
                        case "ascending":
                            builder.field("order", "asc");
                            break;
                        case "descending":
                            builder.field("order", "desc");
                            break;
                        default:
                            builder.field(mod.getName().toString(), mod.getTerm());
                            break;
                    }
                }
                builder.endObject();
                builder.endObject();
            }
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(ESModifier node) {
        modifiers.push(node);
    }

    @Override
    public void visit(Operator node) {
    }

    @Override
    public void visit(ESExpression node) {
        Operator op = node.getOperator();
        if (op == Operator.SORT) {
            for (Node arg : node.getArgs()) {
                arg.accept(this);
            }
        }
    }

}
