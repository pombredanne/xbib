/*
 * $Id$
 *
 * Copyright (C) 1996, Hoylen Sue.  All Rights Reserved.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  Refer to
 * the supplied license for more details.
 */

package asn1;

import java.io.*;

//----------------------------------------------------------------
/**
 * This class represents a primitive ASN.1 object encoded
 * according to the Basic Encoding Rules.
 * <p>
 *
 * <em>Information technology -
 * Open Systems Interconnection -
 * Specification of basic encoding rules for Abstract Syntax Notation
 * One (ASN.1)</em>
 * AS 3626-1991
 * ISO/IEC 8825:1990
 *
 * @see asn1.BEREncoding
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

//----------------------------------------------------------------

public class BERPrimitive extends BEREncoding
{
  //----------------------------------------------------------------
  /**
   * Constructor.
   * Note that the contents is int[] because this is the internal
   * representation, which can only be used by the ASN.1 standard object
   * classes. It is not intended that higher level classes create
   * BERPrimitives directly.
   *
   * @see asn1.BEREncoding#UNIVERSAL_TAG
   * @see asn1.BEREncoding#APPLICATION_TAG
   * @see asn1.BEREncoding#CONTEXT_SPECIFIC_TAG
   * @see asn1.BEREncoding#PRIVATE_TAG
   */

BERPrimitive(int asn1_class, int tag, int contents[])
       throws ASN1Exception
  {
    init(asn1_class, /* constructed */ false, tag, contents.length);
    contents_octets = contents;
  };

  //----------------------------------------------------------------
  /**
   * This method allows the content octets to be examined.
   * Once again, only the ASN.1 standard objects should be using this.
   */

int[]
peek()
  {
    return contents_octets;
  }

  //----------------------------------------------------------------
  /**
   * This method outputs the encoded octets to the destination OutputStream.
   *
   * Note: the output is not flushed, so you <strong>must</strong>  explicitly
   * flush the output stream after calling this method to ensure that
   * the data has been written out.
   *
   * @param	dest - OutputStream to write encoding to.
   */

public void 
output(OutputStream dest)
       throws java.io.IOException
  {
    output_head(dest);
    output_bytes(contents_octets, dest);
  }

  //----------------------------------------------------------------
  /**
   * Returns a new String object representing this BER encoded
   * ASN.1 object's value. 
   */

public String
toString()
  {
    StringBuffer str = new StringBuffer("[");
    switch (i_tag_type) {
    case BEREncoding.UNIVERSAL_TAG:
      str.append("UNIVERSAL ");
      break;
    case BEREncoding.APPLICATION_TAG:
      str.append("APPLICATION ");
      break;
    case BEREncoding.CONTEXT_SPECIFIC_TAG:
      str.append("CONTEXT SPECIFIC ");
      break;
    case BEREncoding.PRIVATE_TAG:
      str.append("PRIVATE ");
      break;
    }
    str.append(String.valueOf(i_tag) + "] '");

    for (int x = 0; x < contents_octets.length; x++) {
      // Dump each octet in hex

      int octet = contents_octets[x];
      char hex[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		     'a', 'b', 'c', 'd', 'e', 'f' };

      str.append(hex[((octet >> 4) & 0x0f)]);
      str.append(hex[(octet & 0x0f)]);
    }
    
    str.append("'H");

    return new String(str);
  }

  //----------------------------------------------------------------
  /**
   * This protected method is used to implement the "get_encoding" method.
   */

protected int
i_encoding_get(int offset, byte data[])
  {
    offset = i_get_head(offset, data);

    for (int n = 0; n < contents_octets.length; n++)
      data[offset++] = (byte) contents_octets[n];

    return offset;
  }

  //----------------------------------------------------------------
  /**
   * The octets of the encoding are stored in this array.
   * They are internally stored as int[] for efficiency over byte[].
   */

private int contents_octets[];
  
} // BERPrimitive

//----------------------------------------------------------------
/*
  $Log$
  */
//----------------------------------------------------------------
//EOF
