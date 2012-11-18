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
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.xbib.query.cql.SyntaxException;

/**
 * Build ES query from abstract syntax tree
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class QueryGenerator implements Visitor {
    
    private final XContentBuilder builder;

    public QueryGenerator() throws IOException {
        this.builder = XContentFactory.jsonBuilder();
    }

    public void start() throws IOException {
        builder.startObject();
    }

    public void end() throws IOException {
        builder.endObject();
    }

    public void startFiltered() throws IOException {
        builder.startObject("filtered").startObject("query");
    }

    public void endFiltered() throws IOException {
        builder.endObject().endObject();
    }

    public XContentBuilder getBuilder() throws IOException {
        return builder;
    }

    public String getResult() throws IOException {
        builder.close();
        return builder.string();
    }

    @Override
    public void visit(Token token) {
        try {
            switch (token.getType()) {
                case BOOL:
                    builder.value(token.getBoolean());
                    break;
                case INT:
                    builder.value(token.getInteger());
                    break;
                case FLOAT:
                    builder.value(token.getFloat());
                    break;
                case DATETIME:
                    builder.value(token.getDate());
                    break;
                default:
                    builder.value(token.getString());
                    break;
            }
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(ESName node) {
        try {
            builder.value(node.toString());
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(ESModifier node) {
        try {
            builder.value(node.toString());
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(Operator node) {
        try {
            builder.value(node.toString());
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
                    Token tok2 = arg2 instanceof Token ? (Token) arg2 : null; // always a Token
                    boolean visible = false;
                    for (Node arg : node.getArgs()) {
                        visible = visible || arg.isVisible();
                    }
                    if (!visible) {
                        return;
                    }
                    switch (op) {
                        case EQUALS: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            // TODO nested search is a hack
                            if (field.startsWith("dc:subject.xbib:subject")) {
                                nestedSearch(field, "xbib:subjectAuthority", "xbib:subjectValue", value);
                            } else {
                                if (tok2.isProtected()) {
                                    if (tok2.getStringList().size() > 1) {
                                        builder.startObject("bool").startArray("must");
                                        for (String s : tok2.getStringList()) {
                                            builder.startObject().startObject("text").startObject(field).field("query", s).endObject().endObject().endObject();
                                        }
                                        builder.endArray().endObject();
                                    } else {
                                        builder.startObject("text").startObject(field).field("query", value).endObject().endObject();
                                    }
                                } else if (tok2.isAll()) {
                                    builder.startObject("match_all").endObject();
                                } else if (tok2.isWildcard()) {
                                    builder.startObject("wildcard").field(field, value).endObject();
                                } else if (tok2.isBoundary()) {
                                    builder.startObject("prefix").field(field, value).endObject();
                                } else {
                                    builder.startObject("text").startObject(field).field("query", value).endObject().endObject();
                                }
                            }
                            break;
                        }
                        case NOT_EQUALS: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            builder.startObject("bool").startObject("must_not");
                            if (tok2.isProtected()) {
                                if (tok2.getStringList().size() > 1) {
                                    builder.startObject("bool").startArray("must");
                                    for (String s : tok2.getStringList()) {
                                        builder.startObject().startObject("text").startObject(field).field("query", s).endObject().endObject().endObject();
                                    }
                                    builder.endArray().endObject();
                                } else {
                                    builder.startObject("text").startObject(field).field("query", value).endObject().endObject();
                                }
                            } else if (tok2.isAll()) {
                                builder.startObject("match_all").endObject();
                            } else if (tok2.isWildcard()) {
                                builder.startObject("wildcard").field(field, value).endObject();
                            } else if (tok2.isBoundary()) {
                                builder.startObject("prefix").field(field, value).endObject();
                            } else {
                                builder.startObject("text").startObject(field).field("query", value).endObject().endObject();
                            }
                            builder.endObject().endObject();
                            break;
                        }
                        case ALL: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            if (tok2.isProtected()) {
                                if (tok2.getStringList().size() > 1) {
                                    builder.startObject("bool").startArray("must");
                                    for (String s : tok2.getStringList()) {
                                        builder.startObject().startObject("text").startObject(field).field("query", s).endObject().endObject().endObject();
                                    }
                                    builder.endArray().endObject();
                                } else {
                                    builder.startObject("text").startObject(field).field("query", value).endObject().endObject();
                                }
                            } else if (tok2.isAll()) {
                                builder.startObject("match_all").endObject();
                            } else if (tok2.isWildcard()) {
                                builder.startObject("wildcard").field(field, value).endObject();
                            } else if (tok2.isBoundary()) {
                                builder.startObject("prefix").field(field, value).endObject();
                            } else {
                                builder.startObject("text").startObject(field).field("query", value).endObject().endObject();
                            }
                            break;
                        }
                        case ANY: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            if (tok2.isProtected()) {
                                if (tok2.getStringList().size() > 1) {
                                    builder.startObject("bool").startArray("should");
                                    for (String s : tok2.getStringList()) {
                                        builder.startObject().startObject("text").startObject(field).field("query", s).endObject().endObject().endObject();
                                    }
                                    builder.endArray().endObject();
                                } else {
                                    builder.startObject("text").startObject(field).field("query", value).endObject().endObject();
                                }
                            } else if (tok2.isAll()) {
                                builder.startObject("match_all").endObject();
                            } else if (tok2.isWildcard()) {
                                builder.startObject("wildcard").field(field, value).endObject();
                            } else if (tok2.isBoundary()) {
                                builder.startObject("prefix").field(field, value).endObject();
                            } else {
                                builder.startObject("text").startObject(field).field("query", value).endObject().endObject();
                            }
                            break;
                        }
                        case PHRASE: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            if (tok2.isProtected()) {                                    
                                    builder.startObject("text_phrase")
                                            .startObject(field)
                                            .field("query", tok2.getString())
                                            .field("slop", 0)
                                            .endObject()
                                            .endObject();                                
                            } else if (tok2.isAll()) {
                                builder.startObject("match_all").endObject();
                            } else if (tok2.isWildcard()) {
                                builder.startObject("wildcard").field(field, value).endObject();
                            } else if (tok2.isBoundary()) {
                                builder.startObject("prefix").field(field, value).endObject();
                            } else {
                                builder.startObject("text_phrase").startObject(field).field("query", value).field("slop", 0).endObject().endObject();
                            }
                            break;
                        }
                        case RANGE_GREATER_THAN: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            builder.startObject("range").startObject(field).field("from", value).field("include_lower", false).endObject().endObject();
                            break;
                        }
                        case RANGE_GREATER_OR_EQUAL: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            builder.startObject("range").startObject(field).field("from", value).field("include_lower", true).endObject().endObject();
                            break;
                        }
                        case RANGE_LESS_THAN: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            builder.startObject("range").startObject(field).field("to", value).field("include_upper", false).endObject().endObject();
                            break;
                        }
                        case RANGE_LESS_OR_EQUALS: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            builder.startObject("range").startObject(field).field("to", value).field("include_upper", true).endObject().endObject();
                            break;
                        }
                        case RANGE_WITHIN: {
                            String field = arg1.toString();
                            String value = arg2.toString();
                            String from;
                            String to;
                            if (!tok2.isProtected()) {
                                throw new IllegalArgumentException("range within: unable to derive range from a non-phrase: " + value);
                            }
                            if (tok2.getStringList().size() != 2) {
                                throw new IllegalArgumentException("range within: unable to derive range from a phrase of lenth not equals to 2: " + tok2.getStringList());
                            }
                            from = tok2.getStringList().get(0);
                            to = tok2.getStringList().get(1);
                            builder.startObject("range").startObject(field).field("from", from).field("to", to).field("include_lower", true).field("include_upper", false).endObject().endObject();
                            break;
                        }
                        case AND: {
                            if (arg2 == null) {
                                if (arg1.isVisible()) {
                                    arg1.accept(this);
                                }
                            } else if (arg1 == null) {
                                if (arg2.isVisible()) {
                                    arg2.accept(this);
                                }
                            } else {
                                if (arg1.isVisible() && arg2.isVisible()) {
                                    builder.startObject("bool");
                                    builder.startObject("must");
                                    if (arg1 instanceof Token) {
                                        // "must" : { "text" : { "_all" : <token> } }
                                        builder.startObject("text").field("_all");
                                        arg1.accept(this);
                                        builder.endObject();
                                    } else {                                    
                                        arg1.accept(this);
                                    }
                                    builder.endObject();
                                    builder.startObject("must");
                                    if (arg2 instanceof Token) {
                                        // "must" : { "text" : { "_all" : <token> } }
                                        builder.startObject("text").field("_all");
                                        arg2.accept(this);
                                        builder.endObject();
                                    } else {                                    
                                        arg2.accept(this);
                                    }
                                    builder.endObject();
                                    builder.endObject();
                                } else if (arg1.isVisible()) {
                                    arg1.accept(this);
                                } else if (arg2.isVisible()) {
                                    arg2.accept(this);
                                }
                            }
                            break;
                        }
                        case OR: {
                            // short expression
                            if (arg2 == null) {
                                if (arg1.isVisible()) {
                                    arg1.accept(this);
                                }
                            } else if (arg1 == null) {
                                if (arg2.isVisible()) {
                                    arg2.accept(this);
                                }
                            } else {
                                builder.startObject("bool");
                                if (arg1.isVisible()) {
                                    builder.startObject("should");
                                    if (arg1 instanceof Token) {
                                        // "should" : { "text" : { "_all" : <token> } }
                                        builder.startObject("text").field("_all");
                                        arg1.accept(this);
                                        builder.endObject();
                                    } else {                                    
                                        arg1.accept(this);
                                    }
                                    builder.endObject();
                                }
                                if (arg2 != null && arg2.isVisible()) {
                                    builder.startObject("should");
                                    if (arg2 instanceof Token) {
                                        // "should" : { "text" : { "_all" : <token> } }
                                        builder.startObject("text").field("_all");
                                        arg2.accept(this);
                                        builder.endObject();
                                    } else {                                    
                                        arg2.accept(this);
                                    }
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
                            String value = arg2.toString() + "~10"; // we assume a
                            // default of 10
                            // words is enough
                            // for proximity
                            builder.startObject("field").field(field, value).endObject();
                            break;
                        }
                        default:
                            throw new IllegalArgumentException(
                                    "unable to translate operator while building elasticsearch query: " + op);
                    }
                    break;
                }
                case 1: {
                    // unary operators
                    break;
                }
                case 0: {
                    // operators with infinite arity
                    switch (op) {
                        case FILTER: {
                            for (Node arg : node.getArgs()) {
                                arg.accept(this);
                            }
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new SyntaxException("internal error while building elasticsearch query", e);
        }
    }

    private void nestedSearch(String field, String termName, String textName, String text) throws IOException {
        int pos = field.indexOf("|");
        String path = pos > 0 ? field.substring(0, pos) : field;
        String term = pos > 0 ? field.substring(pos + 1) : "UNKNOWN";
        builder.startObject("nested").field("path", path).field("score_mode", "avg").startObject("query").startObject("bool").startArray("must").startObject().startObject("term").field(path + "." + termName, term).endObject().endObject().startObject().startObject("text").field(path + "." + textName, text).endObject().endObject().endArray().endObject().endObject().endObject();
    }
    
}
