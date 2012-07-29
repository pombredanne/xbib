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
 * Representation for an ASN.1 NumericString.
 *
 * The <code>NumericString<code> type denotes an arbitary string
 * of Numeric characters (digits and space).
 * This type is a string type.
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

//----------------------------------------------------------------

public class ASN1NumericString extends ASN1OctetString
{
  /**
   * This constant is the ASN.1 UNIVERSAL tag value for NumericString.
   */

public static final int TAG = 0x12;

  //----------------------------------------------------------------
  /**
   * Constructor for an ASN.1 NumericString object. It sets the tag to the
   * default value of UNIVERSAL 18 (0x12). */

public 
ASN1NumericString(String value)
  {
    super(value);
  }

  //----------------------------------------------------------------
  /**
   * Constructor for a NumericString object from a primitive BER encoding.
   *
   * @param ber_enc The BER encoding to use.
   * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
   * @exception	ASN1Exception If the BER encoding is incorrect.
   */

public
ASN1NumericString(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
  {
    super(ber, false);

    if (check_tag) {
      if (ber.tag_get() != TAG || 
	  ber.tag_type_get() != BEREncoding.UNIVERSAL_TAG)
	throw new ASN1EncodingException
	  ("ASN.1 NumericString: bad BER: tag=" + ber.tag_get() + 
	   " expected " + TAG + "\n");
    }
  }

  //----------------------------------------------------------------
  /**
   * Encodes
   *
   * @exception	ASN1Exception if the BER encoding cannot be formed.
   */

public BEREncoding
ber_encode()
       throws ASN1Exception
  {
    return ber_encode(BEREncoding.UNIVERSAL_TAG, TAG);
  }

  /*
   * ber_encode()
   * ber_decode()
   * set()
   * get() 
   * toString()
   * are inherited from base class
   */

} // ASN1NumericString

//----------------------------------------------------------------
/*
  $Log$
  */
//----------------------------------------------------------------
//EOF
