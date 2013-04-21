package org.xbib.tools.aggregator.elasticsearch.zdb.entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Work extends Expression {

    private Set<Manifestation> works;

    private Map<String, Expression> expressions;

    private Set<Manifestation> manifestations;

    public Work(Manifestation manifestation) {
        super(null, manifestation);
        this.works = new HashSet();
        this.expressions = new HashMap();
        this.manifestations = new HashSet();
        manifestations.add(manifestation);
    }

    public void addNeighbor(Manifestation manifestation) {
        manifestation.map().put("isWorkOf", id());
        works.add(manifestation);
    }

    public Set<Manifestation> getNeighbors() {
        return works;
    }

    public void setExpressions(Map<String,Expression> expressions) {
        this.expressions = expressions;
    }

    public Map<String,Expression> getExpressions() {
        return expressions;
    }

    public void addManifestation(Manifestation manifestation) {
        manifestations.add(manifestation);
    }

    public Set<Manifestation> getManifestations() {
        return manifestations;
    }

    public Set<String> allIDs() {
        Set<String> result = new HashSet();
        result.add(id());
        for (Manifestation m : works) {
            result.add(m.id());
        }
        Collection<Expression> set = expressions.values();
        for (Expression e : set) {
            result.addAll(e.allIds());
        }
        return result;
    }

    public Set<String> allTargetIDs() {
        Set<String> result = new HashSet();
        result.add(targetID());
        for (Manifestation m : works) {
            result.add(m.targetID());
        }
        Collection<Expression> set = expressions.values();
        for (Expression e : set) {
            result.addAll(e.allTargetIds());
        }
        return result;
    }
}