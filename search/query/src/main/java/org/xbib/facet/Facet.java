package org.xbib.facet;

import java.util.ArrayList;
import java.util.List;

public class Facet {

    private String displayLabel;

    private String description;

    private String index;

    private String relation;

    private List<FacetTerm> terms = new ArrayList();

    public Facet(String displayLabel, String description, String index, String relation) {
        this.displayLabel = displayLabel;
        this.description = description;
        this.index = index;
        this.relation = relation;
        this.terms = new ArrayList();
    }

    public void add(FacetTerm term) {
        terms.add(term);
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public String getDescription() {
        return description;
    }

    public String getIndex() {
        return index;
    }

    public String getRelation() {
        return relation;
    }

    public List<FacetTerm> getTerms() {
        return terms;
    }

}
