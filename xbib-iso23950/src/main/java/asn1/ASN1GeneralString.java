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
 * ASN.1 GeneralString
 *
 * The <code>GeneralString<code> type denotes an arbitary string
 * of General characters.
 * This type is a string type.
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

//----------------------------------------------------------------

public class ASN1GeneralString extends ASN1OctetString
{
  /**
   * This constant is the UNIVERSAL tag value for GeneralString.
   */

public static final int TAG = 0x1B;

  //----------------------------------------------------------------
  /**
   * Constructor for a GeneralString object. It sets the tag to the
   * default value of UNIVERSAL 27 (0x1B).
   *
   * @param value The string value.
   */

public 
ASN1GeneralString(String value)
  {
    super(value);
  }

  //----------------------------------------------------------------
  /**
   * Constructor for a GeneralString object from a primitive BER encoding.
   *
   * @exception	ASN1Exception if the BER encoding is incorrect.
   */

public
ASN1GeneralString(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
  {
    super(ber, false);

    if (check_tag) {
      if (ber.tag_get() != TAG || 
	  ber.tag_type_get() != BEREncoding.UNIVERSAL_TAG)
	throw new ASN1EncodingException
	  ("ASN.1 GeneralString: bad BER: tag=" + ber.tag_get() + 
	   " expected " + TAG + "\n");
    }
  }

  //----------------------------------------------------------------
  /**
   * Encode with no explicit tag.
   *
   * @exception	ASN1Exception if the BER encoding cannot be made.
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

}

//----------------------------------------------------------------
/*
  $Log$
  */
//----------------------------------------------------------------
//EOF
