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
package org.xbib.query.cql.elasticsearch.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Stack;
import org.xbib.query.cql.SyntaxException;
import org.xbib.query.cql.elasticsearch.ESExpression;
import org.xbib.query.cql.elasticsearch.ESName;
import org.xbib.query.cql.elasticsearch.Node;
import org.xbib.query.cql.elasticsearch.Operator;
import org.xbib.query.cql.elasticsearch.TokenType;
import org.xbib.query.cql.model.CQLQueryModel;

/**
 * Elasticsearch query model
 *
 */
public final class QueryModel {

    /** the definition bundle for this query model */
    private final ResourceBundle bundle;

    private final Map<String, String> options;

    private final Map<String, ESExpression> conjunctivefilters;

    private final Map<String, ESExpression> disjunctivefilters;

    /** the FQL facets */
    private final List<Facet<String>> facets;

    private ESExpression sortexpr;

    /**
     * Constructor
     * @param bundle the bundle with Elasticsearch definitions
     */
    public QueryModel(ResourceBundle bundle) {
        this.bundle = bundle;
        this.options = new HashMap<>();
        this.conjunctivefilters = new HashMap<>();
        this.disjunctivefilters = new HashMap<>();
        this.facets = new LinkedList<>();
    }

    /**
     * Method for mapping index keys to Elasticsearch. Only valid mappings
     * are returned, otherwise an error is thrown.
     * @param index the index
     * @return the mapped field from the index
     */
    public String getFieldOfIndex(String index) {
        try {
            return bundle.getString(index);
        } catch (MissingResourceException e) {
            throw new SyntaxException("unknown index: " + index, e);
        }
    }

    /**
     * Determine if the key has a type. Default type is string.
     *
     * @param key the key to check
     * @return the type of the key
     */
    public TokenType getESType(String key) {
        try {
            String type = bundle.getString("estype." + key);
            if ("datetime".equals(type)) {
                return TokenType.DATETIME;
            }
            if ("int".equals(type)) {
                return TokenType.INT;
            }
            if ("float".equals(type)) {
                return TokenType.FLOAT;
            }
            return TokenType.STRING;
        } catch (MissingResourceException e) {
            return TokenType.STRING;
        }
    }

    /**
     * Modifiy an ES index term
     *
     * @param modifiable the modifiable index term, appended by modification terms
     * @return the modified ES index term
     */
    public String getESModifier(String modifiable) {
        try {
            return bundle.getString(modifiable);
        } catch (MissingResourceException e) {
            throw new SyntaxException("unknown modifiable key: " + modifiable, e);
        }
    }

    /**
     * Get expression visibility of a given context
     * @param context
     * @return
     */
    public boolean getVisibility(String context) {
        return !CQLQueryModel.isFacetContext(context)
                && !CQLQueryModel.isFilterContext(context)
                && !CQLQueryModel.isOptionContext(context);
    }


    /**
     * Check if this context is the facet context
     * @param context
     * @return
     */
    public boolean isFacetContext(String context) {
        return CQLQueryModel.isFacetContext(context);
    }

    /**
     * Check if this context is the filter context
     * @param context
     * @return
     */
    public boolean isFilterContext(String context) {
        return CQLQueryModel.isFilterContext(context);
    }

    /**
     * Check if this context is the option context
     * @param context
     * @return
     */
    public boolean isOptionContext(String context) {
        return CQLQueryModel.isOptionContext(context);
    }

    /**
     * Add option. An option is mapped to Elasticsearch JSON.
     * @param key
     * @param value
     */
    public void addOption(String key, String value) {
        try {
            options.put(bundle.getString("option." + key), value);
        } catch (MissingResourceException e) {
            throw new SyntaxException("illegal option: " + key, e);
        }
    }    

    public void addConjunctiveFilter(Filter<Node> filter) {
        addFilter(conjunctivefilters, filter);
    }

    public void addDisjunctiveFilter(Filter<Node> filter) {
        addFilter(disjunctivefilters, filter);
    }
    /**
     * Get filter expression.
     * Only one filter expression is allowed per query.
     * First, build conjunctive and disjunctive filter terms.
     * If both are null, there is no filter at all.
     * Otherwise, combine conjunctive and disjunctive filter terms with a
     * disjunction, and apply filter function, and return this expression.
     *
     * @return a single filter expression or null if there are no filter terms
     */
    public ESExpression getFilterExpression() {
        ESExpression conjunctiveclause = null;
        if (!conjunctivefilters.isEmpty()) {
            conjunctiveclause = new ESExpression(Operator.AND,
                    conjunctivefilters.values().toArray(new Node[conjunctivefilters.size()]));
        }
        ESExpression disjunctiveclause = null;
        if (!disjunctivefilters.isEmpty()) {
            disjunctiveclause = new ESExpression(Operator.OR,
                    disjunctivefilters.values().toArray(new Node[disjunctivefilters.size()]));
        }
        if (conjunctiveclause == null && disjunctiveclause == null) {
            return null;
        }
        return new ESExpression(Operator.OR, conjunctiveclause, disjunctiveclause);
    }
    
    /**
     * Add a facet to
     * @param facet the facet to add
     */
    public void addFacet(Facet<String> facet) {
        try {
            facet.setName(bundle.getString("facet." + facet.getName()));
        } catch (MissingResourceException e) {
            throw new SyntaxException("invalid facet name: " + facet.getName(), e);
        }
        if ("on".equals(facet.getValue().replaceAll("\"", ""))) {
            facets.add(facet);
        } else if ("off".equals(facet.getValue().replaceAll("\"", ""))) {
            facets.remove(facet);
        } else {
            throw new SyntaxException("invalid facet value: " + facet.getValue());
        }
    }
    
    /**
     * Get facets
     * @return
     */
    public List<Facet<String>> getFacets() {
        return facets;
    }
    
    /**
     * Add sort expression
     *
     * @param indexAndModifier the index with modifiers
     */
    public void addSort(Stack<Node> indexAndModifier) {
        this.sortexpr = new ESExpression(Operator.SORT,
                reverse(indexAndModifier).toArray(new Node[indexAndModifier.size()]));
    }
    
    /**
     * Get sort expression
     * @return
     */
    public ESExpression getSort() {
        return sortexpr;
    }
    
    private int getIntValue(String intKey, int defaultValue) {
        try {
            String key = bundle.getString(intKey);
            return options.containsKey(key) ? Integer.parseInt(options.get(key)) : defaultValue;
        } catch (MissingResourceException e) {
            throw new SyntaxException("unable to get key for " + intKey);
        }
    }

    /**
     * Helper method to add a filter
     * @param filters the filter list
     * @param filter the filter
     */
    private void addFilter(Map<String, ESExpression> filters, Filter<Node> filter) {
        // hack for "location" filter
        if ("location".equals(filter.getName())) {
            ESExpression expr1 = new ESExpression(Operator.EQUALS, new ESName("_type"), filter.getValue());
            ESExpression expr2 = new ESExpression(Operator.EQUALS, new ESName("dc:identifier.xbib:identifierAuthorityISIL"), filter.getValue());
            filters.put(filter.getName(), new ESExpression(Operator.OR, expr1, expr2));
            return;
        }
        try {
            filter.setName(bundle.getString("filter." + filter.getName()));
        } catch (MissingResourceException e) {
            throw new SyntaxException("illegal filter: " + filter.getName(), e);
        }
        ESName name = new ESName(filter.getName());
        name.setType(getESType(filter.getName()));
        ESExpression expr = new ESExpression(filter.getFilterOperation(), name, filter.getValue());
        filters.put(filter.getName(), expr);
    }
    
    /**
     * Helper method to reverse an expression stack 
     * @param in the stack to reverse
     * @return the reversed stack
     */
    private Stack<Node> reverse(Stack<Node> in) {
        Stack<Node> out = new Stack<Node>();
        while (!in.empty()) {
            out.push(in.pop());
        }
        return out;
    }    
}
