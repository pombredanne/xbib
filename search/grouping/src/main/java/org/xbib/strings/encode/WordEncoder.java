package org.xbib.strings.encode;

import org.xbib.util.Filter;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;


public class WordEncoder {


    private final static Pattern word = Pattern.compile("[\\P{IsWord}]");

    private List<String> splitWord(String s) {
        LinkedList l = new LinkedList();
        Filter.filter(s.split(word.pattern(), 0), l, emptyPred);
        return l;
    }

    private final static EmptyPredicate emptyPred = new EmptyPredicate();

    static class EmptyPredicate implements Filter.Predicate<String,String> {

        @Override
        public String apply(String s) {
            return s == null || s.length() == 0 ? null : s;
        }

    }

    private final static IsWordPredicate isWordPred = new IsWordPredicate();

    static class IsWordPredicate implements Filter.Predicate<String,String> {

        @Override
        public String apply(String s) {
            return s == null || s.length() == 0 || word.matcher(s).matches() ? null : s;
        }
    }
}
