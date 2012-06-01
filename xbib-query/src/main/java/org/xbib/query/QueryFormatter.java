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
package org.xbib.query;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Query formatter interface. Enables filter, facet, and option management
 * in a query language.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public interface QueryFormatter {

    /** Write a query with facet selected */
    String withFacet(String name, String value);

    /** Write a query with facet unselected */
    String withoutFacet(String name, String value);

    /** Write a query with a filter term (drill-down) */
    String withFilter(String name, String op, String value, String label);
    
    /** Write a query without a filter term (drill-up) */
    String withoutFilter(String name, String op, String value, String label);

    /** Write a query with option activated */
    String withOption(String name, String value);

    /** Write a query with option deactivated */
    String withoutOption(String name, String value);
    
    /** Write a query with filters, facets, options */    
    String writeFullQuery();

    /** Write a query without filters, facets, options */
    String writePlainQuery();

    /** Write a query with replacement terms */
    String writeSuggestedQuery(String original, String suggestion);

    /** Create a bread crumb with all filter expressions */
    TreeMap<String,Collection<Map<String,String>>> getFilterBreadcrumbs();

    /** Create a bread crumb with all facet expressions */
    TreeMap<String,Collection<Map<String,String>>> getFacetBreadcrumbs();

    /** Create a bread crumb with all option expressions */
    TreeMap<String,Collection<Map<String,String>>> getOptionBreadcrumbs();

}
