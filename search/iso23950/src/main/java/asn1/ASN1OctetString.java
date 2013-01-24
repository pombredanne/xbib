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
 * Representation of an ASN.1 OCTET STRING.
 *
 * This class is used to store an ASN.1 OCTET STRING which is an
 * arbitary string of octets (eight-bit values). An OCTET STRING
 * can have any length including zero. The type is a string type.
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

//----------------------------------------------------------------

public class ASN1OctetString extends ASN1Any
{
  /*
   * This constant is the ASN.1 UNIVERSAL tag value for OCTET STRING.
   */

public static final int TAG = 0x04;

  //----------------------------------------------------------------
  /**
   * Constructor for an OCTET STRING object. The tag is set to the
   * default of UNIVERSAL 4, and its value to the given bytes.
   */

public
ASN1OctetString(byte data[])
  {
    octets = new byte[data.length];
    for (int i = 0; i<data.length; i++) {
        octets[i] = data[i];
    }
  }

  //----------------------------------------------------------------
  /**
   * Constructor for an OCTET STRING object. The tag is set to the
   * default of UNIVERSAL 4, and its value to the lower bytes of the
   * characters of the given string.
   */

public
ASN1OctetString(String str)
  {
    try {
        octets = str.getBytes("ISO-8859-1");
    } catch (UnsupportedEncodingException ex) {
        octets = str.getBytes();
    }
  }

  //----------------------------------------------------------------
  /**
   * Constructor for a OCTET STRING object from a primitive BER encoding.
   *
   * @param ber The BER encoding to use.
   * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
   * @exception	ASN1Exception If the BER encoding is incorrect.
   */

public
ASN1OctetString(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
  {
    super(ber, check_tag); // superclass will call ber_decode
  }

  //----------------------------------------------------------------
  /**
   * Method for initializing the object from a BER encoding.
   *
   * @param ber_enc The BER encoding to use.
   * @param check_tag If true, it checks the tag. Does nothing for ASN1Any.
   * @exception	ASN1EncodingException If the BER cannot be decoded.
   */

public void
ber_decode(BEREncoding ber_enc, boolean check_tag)
       throws ASN1EncodingException
  {
    if (check_tag) {
      if (ber_enc.tag_get() != TAG ||
	  ber_enc.tag_type_get() != BEREncoding.UNIVERSAL_TAG)
	throw new ASN1EncodingException
	  ("ASN.1 OCTET STRING: bad BER: tag=" + ber_enc.tag_get() +
	   " expected " + TAG + "\n");
    }

    if (ber_enc instanceof BERPrimitive) {
      BERPrimitive ber = (BERPrimitive) ber_enc;

      int encoding[] = ber.peek();

      StringBuffer buf = new StringBuffer(encoding.length);

      for (int x = 0; x < encoding.length; x++)
	buf.append((char) (encoding[x] & 0x00ff));

    try {
        octets = new String(buf).getBytes("ISO-8859-1");
    } catch (UnsupportedEncodingException ex) {
        octets = new String(buf).getBytes();
    }

    } else {
      // not implemented yet ???
      throw new ASN1EncodingException
	("ASN.1 OCTET STRING: decode from constructed NOT IMPLEMENTED YET");
    }
  }

  //----------------------------------------------------------------

  /**
   * Makes a BER encoding of the OCTET STRING.
   *
   * OCTET STRINGs can have a primitive encoding and a constructed
   * encoding. This implemented performs the primitive encoding (which
   * is the DER form).
   *
   * @return	The BER encoding of the OCTET STRING
   * @exception	ASN1Exception when the OCTET STRING is invalid
   *		and cannot be encoded.
   */

public BEREncoding
ber_encode()
       throws ASN1Exception
  {
    return ber_encode(BEREncoding.UNIVERSAL_TAG, TAG);
  }

  //----------------------------------------------------------------

  /**
   * Makes a BER encoding of the OCTET STRING.
   *
   * OCTET STRINGs can have a primitive encoding and a constructed
   * encoding. This implemented performs the primitive encoding (which
   * is the DER form).
   *
   * @return	The BER encoding of the OCTET STRING
   * @exception	ASN1Exception when the OCTET STRING is invalid
   *		and cannot be encoded.
   */

public BEREncoding
ber_encode(int tag_type, int tag)
       throws ASN1Exception
  {
    int size = octets.length;
    int encoding[] = new int[size];

    // Generate BER encoding of the Octet String

    for (int index = 0; index < size; index++)
      encoding[index] = octets[index] & 0x00ff;

    return new BERPrimitive(tag_type, tag, encoding);
  }

  //----------------------------------------------------------------
  /**
   * Method to set the OCTET STRING's value.
   *
   * @param new_val  the value to set the OCTET STRING to.
   * @return	the object.
   */

public ASN1OctetString
set(byte octet_array[])
  {
//    octets = new String(octet_array); // ??? should specify explicit encoding
      octets = new byte[octet_array.length];
      for (int i = 0; i<octet_array.length; i++) {
          octets[i] = octet_array[i];
      }
    return this;
  }

  //----------------------------------------------------------------
  /**
   * Method to set the OCTET STRING's value.
   *
   * @param	str  the value to set the OCTET STRING to.
   * @return	the object.
   */

public ASN1OctetString
set(String str)
  {
      try {
          octets = str.getBytes("ISO-8859-1");
      } catch (UnsupportedEncodingException ex) {
          octets = str.getBytes();
      }
    return this;
  }

  //----------------------------------------------------------------
  /**
   * Method to get the OCTET STRING's value as a String.
   *
   * @return	the OCTET STRING's current value.
   */

public String
get()
  {
    try {
        return new String(octets, "ISO-8859-1");
    } catch (UnsupportedEncodingException ex) {
        return new String(octets);
    }
  }

  //----------------------------------------------------------------
  /**
   * Method to get the OCTET STRING's value as an array of bytes.
   *
   * @return	the OCTET STRING's current value.
   */

public byte[]
get_bytes()
  {
      return octets;
  }

  //----------------------------------------------------------------

private static final char oct[] = { '0', '1', '2', '3', '4', '5', '6', '7' };

  /**
   * Returns a new String object representing this ASN.1 object's value.
   */

public String
toString()
  {
    int size = octets.length;
    // Buffer: make it big just in case everything needs to be encoded
    StringBuffer buf = new StringBuffer(32 + (size * 4));

    // Determine whether to use hexadecimal form or text form

    int printable = 0;
    int binary = 0;

    for (int x = 0; x < size; x++) {
      char octet = (char)octets[x];

      if ((' ' <= octet && octet <= '~') ||
	  octet == '\n')
	printable++;
      else
	binary++;
    }

    if (binary <= printable) {
      // Display as a printable string

      buf.append('"');

      for (int x = 0; x < size; x++) {
	char octet = (char)octets[x];

	if (' ' <= octet && octet <= '~') {
	  // Printable character

	  if (octet == '\\' || octet == '"' || octet == '\'')
	    buf.append('\\'); // escape character

	  buf.append(octet);

	} else if (octet == '\n') {
	  buf.append("\\n");
	} else if (octet == '\t') {
	  buf.append("\\t");
	} else if (octet == '\r') {
	  buf.append("\\r");
	} else if (octet == '\b') {
	  buf.append("\\b");
	} else if (octet == '\f') {
	  buf.append("\\f");

	} else {
	  // Unprintable characters, use octal escape

	  buf.append('\\');

	  buf.append(oct[((octet >> 6) & 0x07)]);
	  buf.append(oct[((octet >> 3) & 0x07)]);
	  buf.append(oct[(octet & 0x07)]);
	}
      }
      buf.append('"');

    } else {
      // Binary string, display as hex

      buf.append('\'');

      for (int x = 0; x < size; x++) {
	char octet = (char)octets[x];
	char hex[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		       'a', 'b', 'c', 'd', 'e', 'f' };

	buf.append(hex[((octet >> 4) & 0x0f)]);
	buf.append(hex[(octet & 0x0f)]);
      }

      buf.append("'H");
    }

    return new String(buf);
  }

  //================================================================
  /**
   * The values of the OCTET STRING are stored in this string. Only
   * the lower bytes are valid.
   */

private byte[] octets;

} // ASN1OctetString

//----------------------------------------------------------------
/*
  $Log$
  */
//----------------------------------------------------------------
//EOF
