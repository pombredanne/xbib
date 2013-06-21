package org.xbib.facet;

public class FacetTerm {

    private String actualTerm;

    private String query;

    private String requestUrl;

    private Long count;

    public FacetTerm(String actualTerm, long count, String query, String requestUrl) {
        this.actualTerm = actualTerm;
        this.query = query;
        this.requestUrl = requestUrl;
        this.count = count;
    }
    
    public String getActualTerm() {
        return actualTerm;
    }
    
    public String getQuery() {
        return query;
    }
    
    public String getRequestUrl() {
        return requestUrl;
    }
    
    public Long getCount() {
        return count;
    }

}
