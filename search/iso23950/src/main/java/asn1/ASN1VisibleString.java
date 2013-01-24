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
 * ASN.1 VisibleString
 *
 * The <code>VisibleString<code> type denotes an arbitary string
 * of Visible characters. It is also known as ISO646String, or 
 * InternationalString.
 * This type is a string type.
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

//----------------------------------------------------------------

public class ASN1VisibleString extends ASN1OctetString
{
  /**
   * This constant is the ASN.1 UNIVERSAL tag value for VisibleString.
   */

public static final int TAG = 0x1a;

  //----------------------------------------------------------------
  /**
   * Constructor for a VisibleString object. It sets the tag to the
   * default value of UNIVERSAL 26 (0x1a). */

public 
ASN1VisibleString(String value)
  {
    super(value);
  }

  //----------------------------------------------------------------
  /**
   * Constructor for a VisibleString object from a primitive BER encoding.
   *
   * @param ber The BER encoding to use.
   * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
   * @exception	ASN1Exception If the BER encoding is incorrect.
   */

public
ASN1VisibleString(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
  {
    super(ber, false);

    if (check_tag) {
      if (ber.tag_get() != TAG || 
	  ber.tag_type_get() != BEREncoding.UNIVERSAL_TAG)
	throw new ASN1EncodingException
	  ("ASN.1 VisibleString: bad BER: tag=" + ber.tag_get() + 
	   " expected " + TAG + "\n");
    }
  }

  //----------------------------------------------------------------
  /**
   * Encode with no explicit tag.
   *
   * @return	The BER encoding
   * @exception	ASN1Exception If the BER encoding is incorrect.
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

} // ASN1VisibleString

//----------------------------------------------------------------
/*
  $Log$
  */
//----------------------------------------------------------------
//EOF
