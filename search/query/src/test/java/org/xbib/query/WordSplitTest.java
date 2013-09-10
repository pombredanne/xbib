package org.xbib.query;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.util.Filter;
import org.xbib.strings.encode.QuotedStringTokenizer;
import org.xbib.strings.encode.UnterminatedQuotedStringException;

public class WordSplitTest {
    
    private final Logger logger = LoggerFactory.getLogger("test");

    @Test
    public void testParseQuotedString() throws Exception {
        String msg = //"Hey, there - what about this?";
        "Heute spielen wir den Boß - Wo geht's denn hier zum Film?";
        List<String> l = parseQuot(msg);
        logger.info("token msg = " + msg + " l = " + l.size());
        for (String s : l) {
            logger.info("token = '" + s + "'");
        }
    }

    @Test
    public void testWordSplit() throws Exception {
        String msg = //"Hey, there - what about \"this\" in Köln? Köln* 2010-2012";
        "Heute spielen wir den Boß - Wo geht's denn hier zum Film?";
        for (String s : splitWord(msg)) {
            logger.info("split word = '" + s + "'");
        }
    }
    
    private List<String> parseQuot(String s) {
        LinkedList l = new LinkedList();
        try {
            Filter.filter(new QuotedStringTokenizer(s, " \t\n\r\f", "\"", '\\', false), l, isWordPred);
        } catch (UnterminatedQuotedStringException e) {
        }
        return l;
    }
    
    private List<String> parseQuotedString(String value) {
        List<String> result = new ArrayList();
        QuotedStringTokenizer tokenizer = new QuotedStringTokenizer(value);
        try {
            while (tokenizer.hasMoreTokens()) {
                result.add(tokenizer.nextToken());
            }
        } catch (IllegalArgumentException e) {
            //
        }
        return result;
    }

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
