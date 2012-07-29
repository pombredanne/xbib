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

//----------------------------------------------------------------
/**
 * Representation for ASN.1 IA5String.
 *
 * The <code>IA5String<code> type denotes an arbitary string of IA5
 * characters. IA5 stands for International Alphabet 5, which is
 * the same as ASCII. The character set includes non-printing control
 * characters. An IA5String can be of any length, including zero.
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

public final class ASN1IA5String extends ASN1OctetString
{
  /**
   * This constant is the ASN.1 UNIVERSAL tag value for IA5String.
   */

public static final int TAG = 0x16;

  //----------------------------------------------------------------
  /**
   * Constructor for a IA5String object. It sets the tag to the
   * default value of UNIVERSAL 22 (0x16).
   */

public 
ASN1IA5String(String value)
  {
    super(value);
  }

  //----------------------------------------------------------------
  /**
   * Constructor for a IA5String object from a primitive BER encoding.
   *
   * @param ber The BER encoding to use.
   * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
   * @exception	ASN1Exception If the BER encoding is incorrect.
   */

public
ASN1IA5String(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
  {
    super(ber, false);

    if (check_tag) {
      if (ber.tag_get() != TAG || 
	  ber.tag_type_get() != BEREncoding.UNIVERSAL_TAG)
	throw new ASN1EncodingException
	  ("ASN.1 IA5String: bad BER: tag=" + ber.tag_get() + 
	   " expected " + TAG + "\n");
    }
  }

  //----------------------------------------------------------------
  /**
   * Returns a BER encoding with no implicit tag.
   *
   * @return	The BER encoding
   * @exception	ASN1Exception when the object is invalid
   *		and cannot be encoded.
   */

public BEREncoding
ber_encode()
       throws ASN1Exception
  {
    return ber_encode(BEREncoding.UNIVERSAL_TAG, TAG);
  }

  /*
   * ber_decode()
   * set()
   * get() 
   * toString()
   * are inherited from base class
   */

} // ASN1IA5String

//----------------------------------------------------------------
/*
  $Log$
  */
//----------------------------------------------------------------
//EOF
