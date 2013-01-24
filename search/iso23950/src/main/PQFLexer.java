/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Affero General Public License as published 
 * by the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses 
 * or write to the Free Software Foundation, Inc., 51 Franklin Street, 
 * Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * The interactive user interfaces in modified source and object code 
 * versions of this program must display Appropriate Legal Notices, 
 * as required under Section 5 of the GNU Affero General Public License.
 * 
 * In accordance with Section 7(b) of the GNU Affero General Public 
 * License, these Appropriate Legal Notices must retain the display of the 
 * "Powered by xbib" logo. If the display of the logo is not reasonably 
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.io.iso23950.pqf;

import java.io.IOException;

class PQFLexer implements PQFTokens {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int STRING2 = 2;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1, 1
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\0\1\35\1\1\2\0\1\2\22\0\1\35\1\0\1\23\5\0"+
    "\2\22\4\0\1\22\1\0\12\24\2\0\1\27\1\25\1\26\1\0"+
    "\1\3\33\0\1\34\4\0\1\4\1\0\1\33\1\6\1\13\1\0"+
    "\1\30\1\0\1\16\1\0\1\17\1\31\1\14\1\5\1\7\1\21"+
    "\1\0\1\10\1\12\1\11\1\32\1\15\1\20\uff88\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\2\0\1\1\2\2\10\1\1\3\1\4\1\5\2\6"+
    "\1\1\1\7\1\10\1\11\1\10\14\1\1\6\1\1"+
    "\1\12\3\1\1\13\5\1\1\14\5\1\1\15\1\1"+
    "\1\16\1\1\1\17\3\1\1\20\3\1\1\21\1\22"+
    "\3\1\1\23\10\1\1\24\1\25";

  private static int [] zzUnpackAction() {
    int [] result = new int[81];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\36\0\74\0\74\0\132\0\170\0\226\0\264"+
    "\0\322\0\360\0\u010e\0\u012c\0\u014a\0\u0168\0\u0186\0\u0168"+
    "\0\u01a4\0\u01c2\0\u01e0\0\u0168\0\u0168\0\u0168\0\u01fe\0\u021c"+
    "\0\u023a\0\u0258\0\u0276\0\u0294\0\u02b2\0\u02d0\0\u02ee\0\u030c"+
    "\0\u032a\0\u0348\0\u0366\0\u0168\0\u0384\0\u0168\0\u03a2\0\u03c0"+
    "\0\u03de\0\74\0\u03fc\0\u041a\0\u0438\0\u0456\0\u0474\0\74"+
    "\0\u0492\0\u04b0\0\u04ce\0\u04ec\0\u050a\0\74\0\u0528\0\74"+
    "\0\u0546\0\74\0\u0564\0\u0582\0\u05a0\0\74\0\u05be\0\u05dc"+
    "\0\u05fa\0\u0618\0\74\0\u0636\0\u0654\0\u0672\0\74\0\u0690"+
    "\0\u06ae\0\u06cc\0\u06ea\0\u0708\0\u0726\0\u0744\0\u0762\0\74"+
    "\0\74";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[81];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\3\1\4\1\5\1\6\1\3\1\7\1\10\1\11"+
    "\2\3\1\12\2\3\1\13\1\3\1\14\1\3\1\15"+
    "\1\0\1\16\1\17\1\20\1\21\1\22\1\23\3\3"+
    "\1\0\1\24\23\25\1\26\10\25\1\27\1\25\22\3"+
    "\2\0\1\3\3\0\4\3\2\0\1\3\1\4\20\3"+
    "\2\0\1\3\3\0\4\3\2\0\4\3\1\30\1\31"+
    "\1\3\1\32\1\3\1\33\1\34\7\3\2\0\1\3"+
    "\3\0\4\3\2\0\22\3\2\0\1\3\3\0\2\3"+
    "\1\35\1\3\2\0\4\3\1\36\15\3\2\0\1\3"+
    "\3\0\4\3\2\0\16\3\1\37\3\3\2\0\1\3"+
    "\3\0\4\3\2\0\11\3\1\40\10\3\2\0\1\3"+
    "\3\0\4\3\2\0\7\3\1\41\12\3\2\0\1\3"+
    "\3\0\4\3\2\0\5\3\1\42\14\3\2\0\1\3"+
    "\3\0\4\3\2\0\10\3\1\43\11\3\2\0\1\3"+
    "\3\0\4\3\40\0\22\3\2\0\1\17\3\0\4\3"+
    "\27\0\1\44\35\0\2\44\7\0\13\3\1\45\6\3"+
    "\2\0\1\3\3\0\4\3\25\0\1\46\12\0\5\3"+
    "\1\47\3\3\1\50\10\3\2\0\1\3\3\0\4\3"+
    "\2\0\7\3\1\51\12\3\2\0\1\3\3\0\4\3"+
    "\2\0\10\3\1\52\11\3\2\0\1\3\3\0\4\3"+
    "\2\0\13\3\1\53\6\3\2\0\1\3\3\0\4\3"+
    "\2\0\13\3\1\54\6\3\2\0\1\3\3\0\4\3"+
    "\2\0\14\3\1\55\5\3\2\0\1\3\3\0\1\3"+
    "\1\56\2\3\2\0\11\3\1\57\10\3\2\0\1\3"+
    "\3\0\4\3\2\0\6\3\1\60\13\3\2\0\1\3"+
    "\3\0\4\3\2\0\10\3\1\61\11\3\2\0\1\3"+
    "\3\0\4\3\2\0\16\3\1\62\3\3\2\0\1\3"+
    "\3\0\4\3\2\0\7\3\1\63\12\3\2\0\1\3"+
    "\3\0\4\3\2\0\16\3\1\64\3\3\2\0\1\3"+
    "\3\0\4\3\2\0\5\3\1\65\14\3\2\0\1\3"+
    "\3\0\4\3\2\0\6\3\1\66\13\3\2\0\1\3"+
    "\3\0\4\3\2\0\11\3\1\67\10\3\2\0\1\3"+
    "\3\0\4\3\2\0\11\3\1\70\10\3\2\0\1\3"+
    "\3\0\4\3\2\0\10\3\1\71\11\3\2\0\1\3"+
    "\3\0\4\3\2\0\11\3\1\72\10\3\2\0\1\3"+
    "\3\0\4\3\2\0\13\3\1\73\6\3\2\0\1\3"+
    "\3\0\4\3\2\0\22\3\2\0\1\3\3\0\1\3"+
    "\1\60\2\3\2\0\13\3\1\74\6\3\2\0\1\3"+
    "\3\0\4\3\2\0\16\3\1\75\3\3\2\0\1\3"+
    "\3\0\4\3\2\0\6\3\1\76\13\3\2\0\1\3"+
    "\3\0\4\3\2\0\20\3\1\77\1\3\2\0\1\3"+
    "\3\0\4\3\2\0\15\3\1\100\4\3\2\0\1\3"+
    "\3\0\4\3\2\0\13\3\1\101\6\3\2\0\1\3"+
    "\3\0\4\3\2\0\10\3\1\102\11\3\2\0\1\3"+
    "\3\0\4\3\2\0\14\3\1\103\5\3\2\0\1\3"+
    "\3\0\4\3\2\0\10\3\1\104\11\3\2\0\1\3"+
    "\3\0\4\3\2\0\11\3\1\105\10\3\2\0\1\3"+
    "\3\0\4\3\2\0\5\3\1\106\14\3\2\0\1\3"+
    "\3\0\4\3\2\0\5\3\1\107\14\3\2\0\1\3"+
    "\3\0\4\3\2\0\4\3\1\110\15\3\2\0\1\3"+
    "\3\0\4\3\2\0\10\3\1\111\11\3\2\0\1\3"+
    "\3\0\4\3\2\0\12\3\1\112\7\3\2\0\1\3"+
    "\3\0\4\3\2\0\16\3\1\113\3\3\2\0\1\3"+
    "\3\0\4\3\2\0\16\3\1\114\3\3\2\0\1\3"+
    "\3\0\4\3\2\0\22\3\2\0\1\3\3\0\1\60"+
    "\3\3\2\0\11\3\1\115\10\3\2\0\1\3\3\0"+
    "\4\3\2\0\4\3\1\56\15\3\2\0\1\3\3\0"+
    "\4\3\2\0\13\3\1\116\6\3\2\0\1\3\3\0"+
    "\4\3\2\0\22\3\2\0\1\3\3\0\3\3\1\60"+
    "\2\0\14\3\1\117\5\3\2\0\1\3\3\0\4\3"+
    "\2\0\13\3\1\120\6\3\2\0\1\3\3\0\4\3"+
    "\2\0\11\3\1\121\10\3\2\0\1\3\3\0\4\3"+
    "\2\0\13\3\1\60\6\3\2\0\1\3\3\0\4\3"+
    "\2\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[1920];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\2\0\13\1\1\11\1\1\1\11\3\1\3\11\15\1"+
    "\1\11\1\1\1\11\53\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[81];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /* user code: */
    private Object yylval;
    private int token;
    private StringBuilder sb = new StringBuilder();
      
    public int getToken() {        
        return token;
    }
    
    public int nextToken() {
        try {
            token = yylex();
        } catch (IOException ex) {
        }
        return token;
    }
    
    public Object getSemantic() {
        return yylval;
    }
    
    public int getLine() {
        return yyline;
    }
    
    public int getColumn() {
        return yycolumn;
    }
    


  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  PQFLexer(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  PQFLexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 96) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead-zzStartRead);

      /* translate stored positions */
      zzEndRead-= zzStartRead;
      zzCurrentPos-= zzStartRead;
      zzMarkedPos-= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzCurrentPos*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
    }

    /* finally: fill the buffer with new input */
    int numRead = zzReader.read(zzBuffer, zzEndRead,
                                            zzBuffer.length-zzEndRead);

    if (numRead > 0) {
      zzEndRead+= numRead;
      return false;
    }
    // unlikely but not impossible: read 0 characters, but not at end of stream    
    if (numRead == 0) {
      int c = zzReader.read();
      if (c == -1) {
        return true;
      } else {
        zzBuffer[zzEndRead++] = (char) c;
        return false;
      }     
    }

	// numRead < 0
    return true;
  }

    
  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * @param reader   the new input stream 
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEOFDone = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public int yylex() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      boolean zzR = false;
      for (zzCurrentPosL = zzStartRead; zzCurrentPosL < zzMarkedPosL;
                                                             zzCurrentPosL++) {
        switch (zzBufferL[zzCurrentPosL]) {
        case '\u000B':
        case '\u000C':
        case '\u0085':
        case '\u2028':
        case '\u2029':
          yyline++;
          yycolumn = 0;
          zzR = false;
          break;
        case '\r':
          yyline++;
          yycolumn = 0;
          zzR = true;
          break;
        case '\n':
          if (zzR)
            zzR = false;
          else {
            yyline++;
            yycolumn = 0;
          }
          break;
        default:
          zzR = false;
          yycolumn++;
        }
      }

      if (zzR) {
        // peek one character ahead if it is \n (if we have counted one line too much)
        boolean zzPeek;
        if (zzMarkedPosL < zzEndReadL)
          zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        else if (zzAtEOF)
          zzPeek = false;
        else {
          boolean eof = zzRefill();
          zzEndReadL = zzEndRead;
          zzMarkedPosL = zzMarkedPos;
          zzBufferL = zzBuffer;
          if (eof) 
            zzPeek = false;
          else 
            zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        }
        if (zzPeek) yyline--;
      }
      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = ZZ_LEXSTATE[zzLexicalState];


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 8: 
          { sb.append(yytext());
          }
        case 22: break;
        case 3: 
          { yybegin(STRING2); 
        sb.setLength(0);
          }
        case 23: break;
        case 15: 
          { yylval = yytext();
        return SET;
          }
        case 24: break;
        case 17: 
          { yylval = yytext();
        return ATTR;
          }
        case 25: break;
        case 10: 
          { sb.append("\"");
          }
        case 26: break;
        case 21: 
          { yylval = yytext();
        return ATTRSET;
          }
        case 27: break;
        case 9: 
          { yybegin(YYINITIAL);
        yylval = sb.toString();
        return CHARSTRING2;
          }
        case 28: break;
        case 4: 
          { yylval = Integer.parseInt(yytext());
        return INTEGER;
          }
        case 29: break;
        case 16: 
          { yylval = yytext();
        return VOID;
          }
        case 30: break;
        case 20: 
          { yylval = yytext();
        return PRIVATE;
          }
        case 31: break;
        case 12: 
          { yylval = yytext();
        return TERMTYPE;
          }
        case 32: break;
        case 6: 
          { yylval = yytext();
        return OPERATORS;
          }
        case 33: break;
        case 1: 
          { yylval = yytext();          
        return CHARSTRING1;
          }
        case 34: break;
        case 11: 
          { yylval = yytext();
        return OR;
          }
        case 35: break;
        case 18: 
          { yylval = yytext();
        return TERM;
          }
        case 36: break;
        case 19: 
          { yylval = yytext();
        return KNOWN;
          }
        case 37: break;
        case 14: 
          { yylval = yytext();
        return NOT;
          }
        case 38: break;
        case 5: 
          { yylval = yytext();
        return EQUALS;
          }
        case 39: break;
        case 13: 
          { yylval = yytext();
        return AND;
          }
        case 40: break;
        case 7: 
          { 
          }
        case 41: break;
        case 2: 
          { return NL;
          }
        case 42: break;
        default: 
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
              {     return 0; 
 }
          } 
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
