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
import org.xbib.date.DateUtil;
import org.xbib.query.cql.*;
import org.xbib.query.cql.Visitor;
import org.xbib.query.cql.elasticsearch.model.Facet;
import org.xbib.query.cql.elasticsearch.model.Filter;
import org.xbib.query.cql.elasticsearch.model.QueryModel;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Stack;

/**
 * Generate Elasticsearch QueryModel DSL from CQL abstract syntax tree
 *
 */
public class ESQueryGenerator implements Visitor {

    /**
     * the default resource bundle
     */
    private static final ResourceBundle DEFAULT_BUNDLE =
            ResourceBundle.getBundle("org.xbib.query.cql.elasticsearch.default");

    private ResourceBundle bundle;

    private QueryModel model;

    private Stack<Node> stack;

    private int from;

    private int size;

    private SourceGenerator sourceGen;

    private QueryGenerator queryGen;

    private SortGenerator sortGen;

    private FilterGenerator filterGen;

    private XContentBuilder filter;

    private XContentBuilder facets;

    /**
     * Default constructor
     */
    public ESQueryGenerator() {
        this(DEFAULT_BUNDLE);
    }

    /**
     * Constructor with custom resource bundle
     *
     * @param bundle
     */
    public ESQueryGenerator(ResourceBundle bundle) {
        this.bundle = bundle;
        reset();
    }

    public final ESQueryGenerator reset() {
        this.model = new QueryModel(bundle);
        this.stack = new Stack();
        try {
            this.sourceGen = new SourceGenerator();
            this.queryGen = new QueryGenerator();
        } catch (IOException e) {
            // something weird went wrong
        }
        return this;
    }

    public QueryModel getModel() {
        return model;
    }

    public ESQueryGenerator setFrom(String from) {
        try {
            if (from != null && from.length() > 0) {
                int n = Integer.parseInt(from);
                if (n >= 0) {
                    this.from = n;
                }
            }
        } catch (Exception e) {
        }
        return this;
    }

    public ESQueryGenerator setFrom(int from) {
        this.from = from;
        return this;
    }
    
    public ESQueryGenerator setSize(String size) {
        try {
            if (size != null && size.length() > 0) {
                int n = Integer.parseInt(size);
                if (n >= 0) {
                    this.size = n;
                }
            }
        } catch (Exception e) {
        }
        return this;
    }

    public ESQueryGenerator setSize(int size) {
        this.size = size;
        return this;
    }

    public ESQueryGenerator setFilter(XContentBuilder filter) {
        this.filter = filter;
        return this;
    }

    public ESQueryGenerator setFacets(XContentBuilder facets) {
        this.facets = facets;
        return this;
    }

    /**
     * Return only Elasticsearch query string
     *
     * @return
     * @throws IOException
     */
    public String getQueryResult() throws IOException {
        return queryGen.getResult().string();
    }

    public String getSourceResult() throws IOException {
        return sourceGen.getResult();
    }

    @Override
    public void visit(SortedQuery node) {
        try {
            if (node.getSortSpec() != null) {
                node.getSortSpec().accept(this);
            }
            // build query
            queryGen.start();
            node.getQuery().accept(this);
            // check for a filter after query node was being accepted
            ESExpression filternode = model.getFilterExpression();
            if (filternode != null) {
                queryGen.startFiltered();
            }
            Node querynode = stack.pop();
            if (querynode instanceof Token) {
                querynode = new ESExpression(Operator.ALL, new ESName(model.getFieldOfIndex("cql.allIndexes")), querynode);
            }
            queryGen.visit((ESExpression) querynode);
            // filter must precede sort
            filterGen = null;
            if (filternode != null) {
                queryGen.end();
                filterGen = new FilterGenerator(queryGen.getResult());
                filterGen.startFilter();
                filterGen.visit(filternode);
                filterGen.endFilter();
                queryGen.end();
            }
            queryGen.end();
            // important, add optional sort after filter
            ESExpression sortnode = model.getSort();
            sortGen = null;
            if (sortnode != null) {
                sortGen = new SortGenerator();
                sortGen.start();
                sortGen.visit(sortnode);
                sortGen.end();
            }
            sourceGen.build(queryGen,
                    from,
                    size,
                    sortGen != null ? sortGen.getResult() : null,
                    filter,
                    facets);
        } catch (IOException e) {
            throw new SyntaxException("unable to build a valid query from " + node + " , reason: " + e.getMessage(), e);
        }
    }

    @Override
    public void visit(SortSpec node) {
        if (node.getSingleSpec() != null) {
            node.getSingleSpec().accept(this);
        }
        if (node.getSortSpec() != null) {
            node.getSortSpec().accept(this);
        }
    }

    @Override
    public void visit(SingleSpec node) {
        if (node.getIndex() != null) {
            node.getIndex().accept(this);
        }
        if (node.getModifierList() != null) {
            node.getModifierList().accept(this);
        }
        if (!stack.isEmpty()) {
            model.addSort(stack);
        }
    }

    @Override
    public void visit(Query node) {
        for (PrefixAssignment assignment : node.getPrefixAssignments()) {
            assignment.accept(this);
        }
        if (node.getScopedClause() != null) {
            node.getScopedClause().accept(this);
        }
    }

    @Override
    public void visit(PrefixAssignment node) {
        node.getPrefix().accept(this);
        node.getURI().accept(this);
    }

    @Override
    public void visit(ScopedClause node) {
        if (node.getScopedClause() != null) {
            node.getScopedClause().accept(this);
        }
        node.getSearchClause().accept(this);
        if (node.getBooleanGroup() != null) {
            node.getBooleanGroup().accept(this);
        }
        // format disjunctive or conjunctive filters
        if (node.getSearchClause().getIndex() != null
                && model.isFilterContext(node.getSearchClause().getIndex().getContext())) {
            // assume that each operator-less filter is a conjunctive filter
            BooleanOperator op = node.getBooleanGroup() != null
                    ? node.getBooleanGroup().getOperator() : BooleanOperator.AND;
            String filtername = node.getSearchClause().getIndex().getName();
            Operator filterop = comparitorToES(node.getSearchClause().getRelation().getComparitor());
            Node filterterm = termToESwithoutWildCard(node.getSearchClause().getTerm());
            Filter<Node> filter = new Filter<>(filtername, filterterm, filterop);
            if (op == BooleanOperator.AND) {
                model.addConjunctiveFilter(filter);
            } else if (op == BooleanOperator.OR) {
                model.addDisjunctiveFilter(filter);
            }
        }
        // evaluate expression
        if (!stack.isEmpty() && stack.peek() instanceof Operator) {
            Operator op = (Operator) stack.pop();
            if (!stack.isEmpty()) {
                Node esnode = stack.pop();
                // add default context if node is a literal without a context
                if (esnode instanceof Token && TokenType.STRING.equals(esnode.getType())) {
                    esnode = new ESExpression(Operator.ALL, new ESName(model.getFieldOfIndex("cql.allIndexes")), esnode);
                }
                if (stack.isEmpty()) {
                    // unary expression
                    throw new IllegalArgumentException("unary expression not allowed, op=" + op + " node=" + esnode);
                } else {
                    // binary expression
                    Node esnode2 = stack.pop();
                    // add default context if node is a literal without context
                    if (esnode2 instanceof Token && TokenType.STRING.equals(esnode2.getType())) {
                        esnode2 = new ESExpression(Operator.ALL, new ESName(model.getFieldOfIndex("cql.allIndexes")), esnode2);
                    }
                    esnode = new ESExpression(op, esnode2, esnode);
                }
                stack.push(esnode);
            }
        }
    }

    @Override
    public void visit(SearchClause node) {
        if (node.getQuery() != null) {
            // CQL query in parenthesis
            node.getQuery().accept(this);
        }
        if (node.getTerm() != null) {
            node.getTerm().accept(this);
        }
        if (node.getIndex() != null) {
            node.getIndex().accept(this);
            String context = node.getIndex().getContext();
            // format options and facets
            if (model.isOptionContext(context)) {
                model.addOption(node.getIndex().getName(), node.getTerm().getValue());
            } else if (model.isFacetContext(context)) {
                Facet<String> facet = new Facet<>(node.getIndex().getName(), Facet.Type.TERMS, node.getTerm().getValue());
                model.addFacet(facet);
            }
        }
        if (node.getRelation() != null) {
            node.getRelation().accept(this);
            if (node.getRelation().getModifierList() != null && node.getIndex() != null) {
                // stack layout: op, list of modifiers, modifiable index
                Node op = stack.pop();
                StringBuilder sb = new StringBuilder();
                Node modifier = stack.pop();
                while (modifier instanceof ESModifier) {
                    Node modifierName = ((ESModifier) modifier).getName();
                    if (modifierName.toString().startsWith("mod.")) {
                        sb.append('.').append(modifier.toString());
                        modifier = stack.pop();
                    } else {
                        break;
                    }
                }
                // push modified index to stack
                String modifiable = model.getESModifier(modifier.toString() + sb.toString());
                stack.push(new ESName(modifiable));
                stack.push(op);
            }
        }
        // evaluate expression
        if (!stack.isEmpty() && stack.peek() instanceof Operator) {
            Operator op = (Operator) stack.pop();
            Node arg1 = stack.pop();
            Node arg2 = stack.pop();
            // fold two expressions if they have the same operator
            boolean fold = arg1.isVisible() && arg2.isVisible()
                    && arg2 instanceof ESExpression
                    && ((ESExpression) arg2).getOperator().equals(op);
            ESExpression expression = fold
                    ? new ESExpression((ESExpression) arg2, arg1)
                    : new ESExpression(op, arg1, arg2);
            stack.push(expression);
        }
    }

    @Override
    public void visit(BooleanGroup node) {
        if (node.getModifierList() != null) {
            node.getModifierList().accept(this);
        }
        stack.push(booleanToES(node.getOperator()));
    }

    @Override
    public void visit(Relation node) {
        if (node.getModifierList() != null) {
            node.getModifierList().accept(this);
        }
        stack.push(comparitorToES(node.getComparitor()));
    }

    @Override
    public void visit(ModifierList node) {
        for (Modifier modifier : node.getModifierList()) {
            modifier.accept(this);
        }
    }

    @Override
    public void visit(Modifier node) {
        Node term = null;
        if (node.getTerm() != null) {
            node.getTerm().accept(this);
            term = stack.pop();
        }
        node.getName().accept(this);
        Node name = stack.pop();
        stack.push(new ESModifier(name, term));
    }

    @Override
    public void visit(Term node) {
        stack.push(termToES(node));
    }

    @Override
    public void visit(Identifier node) {
        stack.push(new ESName(node.getValue()));
    }

    @Override
    public void visit(Index node) {
        String context = node.getContext();
        String name = model.getFieldOfIndex(context != null ? context + "." + node.getName() : node.getName());
        ESName esname = new ESName(name, model.getVisibility(context));
        esname.setType(model.getESType(name));
        stack.push(esname);
    }

    @Override
    public void visit(SimpleName node) {
        stack.push(new ESName(model.getFieldOfIndex(node.getName())));
    }

    private Node termToES(Term node) {
        if (node.isLong()) {
            return new Token(Long.parseLong(node.getValue()));
        } else if (node.isFloat()) {
            return new Token(Double.parseDouble(node.getValue()));
        } else if (node.isIdentifier()) {
            return new Token(node.getValue());
        } else if (node.isDate()) {
            return new Token(DateUtil.parseDateISO(node.getValue()));
        } else if (node.isString()) {
            return new Token(node.getValue());
        }
        return null;
    }

    private Node termToESwithoutWildCard(Term node) {
        return node.isString() || node.isIdentifier()
                ? new Token(node.getValue().replaceAll("\\*", ""))
                : termToES(node);
    }

    private Operator booleanToES(BooleanOperator bop) {
        Operator op = null;
        switch (bop) {
            case AND:
                op = Operator.AND;
                break;
            case OR:
                op = Operator.OR;
                break;
            case NOT:
                op = Operator.ANDNOT;
                break;
            case PROX:
                op = Operator.PROX;
                break;
            default:
                throw new IllegalArgumentException("untranslated CQL operator: " + bop);
        }
        return op;
    }

    private Operator comparitorToES(Comparitor op) {
        Operator esop = null;
        switch (op) {
            case EQUALS:
                esop = Operator.EQUALS;
                break;
            case GREATER:
                esop = Operator.RANGE_GREATER_THAN;
                break;
            case GREATER_EQUALS:
                esop = Operator.RANGE_GREATER_OR_EQUAL;
                break;
            case LESS:
                esop = Operator.RANGE_LESS_THAN;
                break;
            case LESS_EQUALS:
                esop = Operator.RANGE_LESS_OR_EQUALS;
                break;
            case NOT_EQUALS:
                esop = Operator.NOT_EQUALS;
                break;
            case WITHIN:
                esop = Operator.RANGE_WITHIN;
                break;
            case ADJ:
                esop = Operator.PHRASE;
                break;
            case ALL:
                esop = Operator.ALL;
                break;
            case ANY:
                esop = Operator.ANY;
                break;
            default:
                throw new IllegalArgumentException("unable to translate CQL comparitor: " + op);
        }
        return esop;
    }
}
