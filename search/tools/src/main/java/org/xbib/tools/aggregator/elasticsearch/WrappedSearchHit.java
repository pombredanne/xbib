package org.xbib.tools.aggregator.elasticsearch;

import org.elasticsearch.search.SearchHit;

public class WrappedSearchHit {

    private final SearchHit hit;

    public WrappedSearchHit(SearchHit hit) {
        this.hit = hit;
    }

    public SearchHit hit() {
        return hit;
    }
}
