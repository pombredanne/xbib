/* Generated By:JJTree&JavaCC: Do not edit this line. JSONPathCompilerConstants.java */
package org.xbib.io.json.javacc;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface JSONPathCompilerConstants {

    /**
     * End of File.
     */
    int EOF = 0;
    /**
     * RegularExpression Id.
     */
    int OPEN_ARRAY = 5;
    /**
     * RegularExpression Id.
     */
    int CLOSE_ARRAY = 6;
    /**
     * RegularExpression Id.
     */
    int LETTER = 7;
    /**
     * RegularExpression Id.
     */
    int CNAME = 8;
    /**
     * RegularExpression Id.
     */
    int DIGIT = 9;
    /**
     * RegularExpression Id.
     */
    int DIGIT19 = 10;
    /**
     * RegularExpression Id.
     */
    int OTHER = 11;
    /**
     * RegularExpression Id.
     */
    int DOT = 12;
    /**
     * RegularExpression Id.
     */
    int OR = 13;
    /**
     * RegularExpression Id.
     */
    int AND = 14;
    /**
     * RegularExpression Id.
     */
    int NOT = 15;
    /**
     * RegularExpression Id.
     */
    int OP = 16;
    /**
     * RegularExpression Id.
     */
    int INTEGER = 17;
    /**
     * RegularExpression Id.
     */
    int NUMBER = 18;
    /**
     * RegularExpression Id.
     */
    int FRAC = 19;
    /**
     * RegularExpression Id.
     */
    int EXP = 20;
    /**
     * RegularExpression Id.
     */
    int EXPONENT = 21;
    /**
     * RegularExpression Id.
     */
    int IDENTIFIER = 22;

    /**
     * Lexical state.
     */
    int DEFAULT = 0;

    /**
     * Literal token values.
     */
    String[] tokenImage = {
            "<EOF>",
            "\" \"",
            "\"\\t\"",
            "\"\\n\"",
            "\"\\r\"",
            "\"[\"",
            "\"]\"",
            "<LETTER>",
            "<CNAME>",
            "<DIGIT>",
            "<DIGIT19>",
            "\"-\"",
            "\".\"",
            "\"||\"",
            "\"&&\"",
            "\"!\"",
            "<OP>",
            "<INTEGER>",
            "<NUMBER>",
            "<FRAC>",
            "<EXP>",
            "<EXPONENT>",
            "<IDENTIFIER>",
            "\"(\"",
            "\")\"",
    };

}
