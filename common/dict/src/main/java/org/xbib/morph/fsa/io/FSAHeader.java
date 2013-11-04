package org.xbib.morph.fsa.io;

/**
 * Standard FSA file header
 */
public final class FSAHeader {
    /**
     * FSA magic (4 bytes).
     */
    public final static int FSA_MAGIC = ('\\' << 24) | ('f' << 16) | ('s' << 8) | ('a');

    /**
     * Maximum length of the header block.
     */
    public static final int MAX_HEADER_LENGTH = 4 + 8;

    /**
     * FSA version number.
     */
    public byte version;

    /**
     * Filler character.
     */
    public byte filler;

    /**
     * Annotation character.
     */
    public byte annotation;

    /**
     * Goto field (may be a compound, depending on the automaton version).
     */
    public byte gtl;

}
