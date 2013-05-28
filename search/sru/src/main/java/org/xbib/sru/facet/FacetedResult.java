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

import org.xbib.xml.XMLUtil;

import java.util.ArrayList;
import java.util.List;

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
            sb.append(facet.toXML());
        }
        sb.append("</facet:facets>");
        return sb.toString();
    }

    public class Facet {

        private String displayLabel;

        private String description;

        private String index;

        private String relation;

        private List<Term> terms = new ArrayList();

        public Facet(String displayLabel, String description, String index, String relation) {
            this.displayLabel = displayLabel;
            this.description = description;
            this.index = index;
            this.relation = relation;
            this.terms = new ArrayList();
        }

        public void add(Term term) {
            terms.add(term);
        }

        public String toXML() {
            StringBuilder sb = new StringBuilder();
            sb.append("<facet:facet>");
            if (displayLabel != null) {
                sb.append("<facet:facetDisplaylabel>").append(XMLUtil.escape(displayLabel)).append("</facet:facetDisplaylabel>");
            }
            if (description != null) {
                sb.append("<facet:facetDescription>").append(XMLUtil.escape(description)).append("</facet:facetDescription>");
            }
            if (index != null) {
                sb.append("<facet:index>").append(XMLUtil.escape(index)).append("</facet:index>");
            }
            if (relation != null) {
                sb.append("<facet:relation>").append(XMLUtil.escape(relation)).append("</facet:relation>");
            }
            sb.append("<facet:terms>");
            for (Term term : terms) {
                sb.append(term.toXML());
            }
            sb.append("</facet:terms>");
            sb.append("</facet:facet>");
            return sb.toString();
        }
    }

    public class Term {

        private String actualTerm;

        private String query;

        private String requestUrl;

        private Long count;

        public Term(String actualTerm, long count, String query, String requestUrl) {
            this.actualTerm = actualTerm;
            this.query = query;
            this.requestUrl = requestUrl;
            this.count = count;
        }

        public String toXML() {
            StringBuilder sb = new StringBuilder();
            sb.append("<facet:term>");
            if (actualTerm != null) {
                sb.append("<facet:actualterm>").append(XMLUtil.escape(actualTerm)).append("</facet:actualterm>");
            }
            if (count != null) {
                sb.append("<facet:count>").append(Long.toString(count)).append("</facet:count>");
            }
            if (query != null) {
                sb.append("<facet:query>").append(XMLUtil.escape(query)).append("</facet:query>");
            }
            if (requestUrl != null) {
                sb.append("<facet:requestUrl>").append(XMLUtil.escape(requestUrl)).append("</facet:requestUrl>");
            }
            sb.append("</facet:term>");
            return sb.toString();
        }

    }
}
