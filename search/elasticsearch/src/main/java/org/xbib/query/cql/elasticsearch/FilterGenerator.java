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

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.xbib.query.cql.SyntaxException;
import org.xbib.strings.encode.QuotedStringTokenizer;

import java.io.IOException;

/**
 * Build query in JSON syntax from abstract syntax tree
 *
 */
public class FilterGenerator implements Visitor {

    private final XContentBuilder builder;

    public FilterGenerator() throws IOException {
        this.builder = XContentFactory.jsonBuilder();
    }

    public FilterGenerator(XContentBuilder builder) throws IOException {
        this.builder = builder;
    }

    public FilterGenerator start() throws IOException {
        builder.startObject();
        return this;
    }

    public FilterGenerator end() throws IOException {
        builder.endObject();
        return this;
    }

    public FilterGenerator startFilter() throws IOException {
        builder.startObject("filter");
        return this;
    }

    public FilterGenerator endFilter() throws IOException {
        builder.endObject();
        return this;
    }

    public XContentBuilder getResult() throws IOException {
        builder.close();
        return builder;
    }

    @Override
    public void visit(Token node) {
        try {
            builder.value(node.toString().getBytes());
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(ESName node) {
        try {
            builder.value(node.toString().getBytes());
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(ESModifier node) {
        try {
            builder.value(node.toString().getBytes());
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(Operator node) {
        try {
            builder.value(node.toString().getBytes());
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(ESExpression node) {
        if (!node.isVisible()) {
            return;
        }
        try {
            Operator op = node.getOperator();
            switch (op.getArity()) {
                case 2: {
                    Node arg1 = node.getArg1();
                    Node arg2 = node.getArgs().length > 1 ? node.getArg2() : null;
                    boolean visible = false;
                    for (Node arg : node.getArgs()) {
                        visible = visible || arg.isVisible();
                    }
                    if (!visible) {
                        return;
                    }
                    Token tok2 = arg2 instanceof Token ? (Token) arg2 : null;
                    switch (op) {
                        case EQUALS: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            builder.startObject(tok2.isBoundary() ? "prefix" : "term").field(field, value)
                                    .endObject();
                            break;
                        }
                        case NOT_EQUALS: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            builder.startObject("not")
                                    .startObject(tok2.isBoundary() ? "prefix" : "term").field(field, value)
                                    .endObject().endObject();
                            break;
                        }
                        case ALL: {
                            boolean phrase = arg2 instanceof Token && ((Token) arg2).isProtected();
                            String field = arg1.toString();
                            String value = arg2.toString();
                            if (phrase) {
                                builder.startArray("and");
                                QuotedStringTokenizer qst = new QuotedStringTokenizer(value);
                                while (qst.hasMoreTokens()) {
                                    builder.startObject().startObject("term").field(field, qst.nextToken()).endObject().endObject();
                                }
                                builder.endArray();
                            } else {
                                builder.startObject(tok2.isBoundary() ? "prefix" : "term").field(field, value)
                                        .endObject();
                            }
                            break;
                        }
                        case ANY: {
                            boolean phrase = arg2 instanceof Token && ((Token) arg2).isProtected();
                            String field = arg1.toString();
                            String value = arg2.toString();
                            if (phrase) {
                                builder.startArray("or");
                                QuotedStringTokenizer qst = new QuotedStringTokenizer(value);
                                while (qst.hasMoreTokens()) {
                                    builder.startObject().startObject("term").field(field, qst.nextToken()).endObject().endObject();
                                }
                                builder.endArray();
                            } else {
                                builder.startObject(tok2.isBoundary() ? "prefix" : "term").field(field, value)
                                        .endObject();
                            }
                            break;
                        }
                        case RANGE_GREATER_THAN: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            builder.startObject("range").startObject(field)
                                    .field("from", value)
                                    .field("include_lower", false)
                                    .endObject().endObject();
                            break;
                        }
                        case RANGE_GREATER_OR_EQUAL: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            builder.startObject("range").startObject(field)
                                    .field("from", value)
                                    .field("include_lower", true)
                                    .endObject().endObject();
                            break;
                        }
                        case RANGE_LESS_THAN: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            builder.startObject("range").startObject(field)
                                    .field("to", value)
                                    .field("include_upper", false)
                                    .endObject().endObject();
                            break;
                        }
                        case RANGE_LESS_OR_EQUALS: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            builder.startObject("range").startObject(field)
                                    .field("to", value)
                                    .field("include_upper", true)
                                    .endObject().endObject();
                            break;
                        }
                        case RANGE_WITHIN: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            String[] s = value.split(" ");
                            builder.startObject("range").startObject(field).
                                    field("from", s[0])
                                    .field("to", s[1])
                                    .field("include_lower", true)
                                    .field("include_upper", true)
                                    .endObject().endObject();
                            break;
                        }
                        case AND: {
                            // short expression
                            if (arg2 == null) {
                                if (arg1.isVisible()) {
                                    arg1.accept(this);
                                }
                            } else {
                                builder.startObject("bool");
                                if (arg1.isVisible()) {
                                    builder.startObject("must");
                                    arg1.accept(this);
                                    builder.endObject();
                                }
                                if (arg2.isVisible()) {
                                    builder.startObject("must");
                                    arg2.accept(this);
                                    builder.endObject();
                                }
                                builder.endObject();
                            }
                            break;
                        }
                        case OR: {
                            // short expression
                            if (arg2 == null) {
                                if (arg1.isVisible()) {
                                    arg1.accept(this);
                                }
                            } else {
                                builder.startObject("bool");
                                if (arg1.isVisible()) {
                                    builder.startObject("should");
                                    arg1.accept(this);
                                    builder.endObject();
                                }
                                if (arg2 != null && arg2.isVisible()) {
                                    builder.startObject("should");
                                    arg2.accept(this);
                                    builder.endObject();
                                }
                                builder.endObject();
                            }
                            break;
                        }
                        case ANDNOT: {
                            builder.startObject("bool");
                            if (arg1.isVisible()) {
                                builder.startObject("must");
                                arg1.accept(this);
                                builder.endObject();
                            }
                            if (arg2.isVisible()) {
                                builder.startObject("must_not");
                                arg2.accept(this);
                                builder.endObject();
                            }
                            builder.endObject();
                            break;
                        }
                        case PROX: {
                            String field = arg1.toString();
                            // we assume a
                            // default of 10
                            // words is enough
                            // for proximity
                            String value = arg2.toString() + "~10";
                            builder.startObject("field").field(field, value).endObject();
                            break;
                        }
                        case TERM_FILTER: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            builder.startObject("term").field(field, value).endObject();
                            break;
                        }
                        case QUERY_FILTER: {
                            builder.startObject("query");
                            arg1.accept(this);
                            builder.endObject();
                            break;
                        }
                        default:
                            throw new IllegalArgumentException(
                                    "unable to translate operator while building elasticsearch query: " + op);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            throw new SyntaxException("internal error while building elasticsearch query", e);
        }
    }

}
