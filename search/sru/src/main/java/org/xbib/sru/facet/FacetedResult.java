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
package org.xbib.sru.facet;

import org.xbib.facet.Facet;
import org.xbib.facet.FacetTerm;
import org.xbib.xml.XMLUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Faceted result for SearchRetrieve
 *
 * TODO need better XML handling here
 *
 */
public class FacetedResult {

    private List<Facet> facets = new ArrayList();

    public FacetedResult add(Facet facet) {
        facets.add(facet);
        return this;
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<facet:facets>");
        for (Facet facet : facets) {
            sb.append(toXML(facet));
        }
        sb.append("</facet:facets>");
        return sb.toString();
    }

    public String toXML(Facet facet) {
        StringBuilder sb = new StringBuilder();
        sb.append("<facet:facet>");
        if (facet.getDisplayLabel() != null) {
            sb.append("<facet:facetDisplaylabel>").append(XMLUtil.escape(facet.getDisplayLabel())).append("</facet:facetDisplaylabel>");
        }
        if (facet.getDescription() != null) {
            sb.append("<facet:facetDescription>").append(XMLUtil.escape(facet.getDescription())).append("</facet:facetDescription>");
        }
        if (facet.getIndex() != null) {
            sb.append("<facet:index>").append(XMLUtil.escape(facet.getIndex())).append("</facet:index>");
        }
        if (facet.getRelation() != null) {
            sb.append("<facet:relation>").append(XMLUtil.escape(facet.getRelation())).append("</facet:relation>");
        }
        sb.append("<facet:terms>");
        for (FacetTerm term : facet.getTerms()) {
            sb.append(toXML(term));
        }
        sb.append("</facet:terms>");
        sb.append("</facet:facet>");
        return sb.toString();
    }

    public String toXML(FacetTerm term) {
        StringBuilder sb = new StringBuilder();
        sb.append("<facet:term>");
        if (term.getActualTerm() != null) {
            sb.append("<facet:actualterm>").append(XMLUtil.escape(term.getActualTerm())).append("</facet:actualterm>");
        }
        if (term.getCount() != null) {
            sb.append("<facet:count>").append(Long.toString(term.getCount())).append("</facet:count>");
        }
        if (term.getQuery() != null) {
            sb.append("<facet:query>").append(XMLUtil.escape(term.getQuery())).append("</facet:query>");
        }
        if (term.getRequestUrl() != null) {
            sb.append("<facet:requestUrl>").append(XMLUtil.escape(term.getRequestUrl())).append("</facet:requestUrl>");
        }
        sb.append("</facet:term>");
        return sb.toString();
    }


}
