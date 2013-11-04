package org.xbib.fsa.moore.levenshtein;

import org.xbib.fsa.moore.Automaton;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A Suggester based on Levenshtein automaton method.
 *
 * This suggester uses three Levenshtein automata, with edit distances of
 * 0, 1, and 2.
 *
 * First, the suggester contructs a dictionary automaton from a given
 * dictionary.
 *
 * This initially automaton can be extended on-the-fly with more input
 * by the add() method.
 *
 * Suggestions are obtained by the method getSuggestionsFor().
 *
 * The word list is built by using Levenshtein automata in a loop,
 * where the loop continues to the next automaton until at least one
 * suggestion is found.
 *
 * If an input word is contained in the dictionary, it will also be returned by
 * getSuggestionsFor() but only if the paramater "exact" is set to true.
 * If the paramater "exact" is set to false, the input word itself will not
 * be returned as a suggestion.
 *
 * Edit distances of greater than 2 are generally too expensive to detect.
 * Therefore, such Levenshtein automata are not used by this Suggester.
 */
public class LevenshteinAutomatonSuggester {

    /**
     * Trivial Levenshtein automaton with an edit distance of 0.
     * This automaton will recognize exact matches.
     */
    private final LevenshteinAutomaton ls0 = new LevenshteinAutomaton(0);
    /**
     * This automaton will recognize matches with an edit distance of 1.
     */
    private final LevenshteinAutomaton ls1 = new LevenshteinAutomaton(1);
    /**
     * This automaton will recognize matches with an edit distance of 2.
     */
    private final LevenshteinAutomaton ls2 = new LevenshteinAutomaton(2);
    /**
     * This automaton will recognize matches with an edit distance of 3.
     */
    private final LevenshteinAutomaton ls3 = new LevenshteinAutomaton(3);
    /**
     * The dictionary
     */
    //private Dictionary dictionary;
    /**
     * The dictionary automaton constructed form the dictionary.
     */
    private DictionaryAutomaton automaton;

    public LevenshteinAutomatonSuggester(Iterable<CharSequence> sequence) {
        //this.dictionary = dict;
        this.automaton = new DictionaryAutomaton(sequence);
    }

    public Automaton getDictionaryAutomaton() {
        return automaton;
    }

    /**
     * Adding input to the dictionary automaton.
     *
     * @param input
     */
    public void add(CharSequence input) {
        automaton.add(input);
    }

    /**
     * Get suggestions for a given input.
     *
     * @param input
     * @param withexact
     * @return
     */
    public Collection<CharSequence> getSuggestionsFor(CharSequence input, boolean withexact, int maxlevel) {
        Collection<CharSequence> suggestions = new ArrayList();
        if (withexact) {
            suggestions.addAll(ls0.recognize(input, automaton));
        }
        if (suggestions.isEmpty() && maxlevel > 0) {
            suggestions.addAll(ls1.recognize(input, automaton));
            if (suggestions.isEmpty() && maxlevel > 1) {
                suggestions.addAll(ls2.recognize(input, automaton));
                if (suggestions.isEmpty() && maxlevel > 2) {
                    suggestions.addAll(ls3.recognize(input, automaton));
                }
            }
        }
        return suggestions;
    }
}
