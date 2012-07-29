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
 * ASN.1 GeneralizedTime
 *
 * The <code>GeneralizedTime<code> type denotes a string corrsponding
 * to an ISO 8601 date string.
 * This type is a string type.
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

//----------------------------------------------------------------

public final class ASN1GeneralizedTime extends ASN1VisibleString
{
  //----------------------------------------------------------------
  /**
   * This constant is the UNIVERSAL tag value for GeneralizedTime.
   */

public static final int TAG = 0x18;

  /**
   * Constructor for an GeneralizedTime object. It sets the tag to the
   * default value of UNIVERSAL 24 (0x18). */

public 
ASN1GeneralizedTime(String value)
  {
    super(value);
  }

  //----------------------------------------------------------------
  /**
   * Constructor for a GeneralizedTime object from a BER encoding.
   *
   * @param ber The BER encoding to use.
   * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
   * @exception	ASN1Exception if the BER encoding is incorrect.
   */

public
ASN1GeneralizedTime(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
  {
    super(ber, false);

    if (check_tag) {
      if (ber.tag_get() != TAG || 
	  ber.tag_type_get() != BEREncoding.UNIVERSAL_TAG)
	throw new ASN1EncodingException
	  ("ASN.1 GeneralizedTime: bad BER: tag=" + ber.tag_get() + 
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

} // ASN1GeneralizedTime

//----------------------------------------------------------------
/*
  $Log$
  */
//----------------------------------------------------------------
//EOF
