package org.xbib.elasticsearch.tools.aggregate.zdb.entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Work extends Edition {

    // all manifestations belonging to this work, also expressions
    private final TreeSet<Manifestation> manifestations;

    private Set<Edition> editions;

    public Work(Manifestation manifestation) {
        super(null, manifestation);
        this.manifestations = new TreeSet();
        manifestations.add(manifestation);
    }

    public void setEditions(Set<Edition> editions) {
        this.editions = editions;
    }

    public Set<Edition> getEditions() {
        return editions;
    }

    public void addManifestation(Manifestation manifestation) {
        manifestations.add(manifestation);
    }

    public TreeSet<Manifestation> getManifestations() {
        return manifestations;
    }

    public Set<String> allIDs() {
        Set<String> result = new TreeSet();
        result.add(id());
        for (Manifestation m : manifestations) {
            result.add(m.id());
        }
        return result;
    }

    public Set<String> allTargetIDs() {
        Set<String> result = new TreeSet();
        result.add(externalID());
        for (Manifestation m : manifestations) {
            result.add(m.externalID());
        }
        return result;
    }

    public Set<Integer> allDates() {
        Set<Integer> result = new TreeSet();
        for (Manifestation m : manifestations) {
            result.addAll(toList(m.firstDate(), m.lastDate()));
        }
        return result;
    }

    private List<Integer> toList(Integer firstDate, Integer lastDate) {
        List<Integer> list = new ArrayList();
        if (firstDate == null) {
            return list;
        }
        if (lastDate == null) {
            lastDate = currentYear;
        }
        for (int i = firstDate; i <= lastDate; i++) {
            list.add(i);
        }
        return list;
    }

    private final static class WorkComparator implements Comparator<Work> {

        @Override
        public int compare(Work w1, Work w2) {
            if (w1 == w2) {
                return 0;
            }
            if (w1 == null) {
                return -1;
            }
            if (w2 == null) {
                return +1;
            }
            Integer d1 = w1.firstDate() == null ? currentYear : w1.firstDate();
            Integer d2 = w2.firstDate() == null ? currentYear : w2.firstDate();
            return d1.compareTo(d2);
        }
    }

    private final static WorkComparator workComparator = new WorkComparator();

    public static Comparator<Work> getWorkComparator() {
        return workComparator;
    }

}