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
 * ASN.1 GraphicString
 *
 * The <code>GraphicString<code> type denotes an arbitary string
 * of Graphic characters.
 * This type is a string type.
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

//----------------------------------------------------------------

public class ASN1GraphicString extends ASN1OctetString
{
  /**
   * This constant is the UNIVERSAL tag value for GraphicString.
   */

public static final int TAG = 0x19;

  //----------------------------------------------------------------
  /**
   * Constructor for a GraphicString object. It sets the tag to the
   * default value of UNIVERSAL 25 (0x19). 
   *
   * @param value The string value
   */

public 
ASN1GraphicString(String value)
  {
    super(value);
  }

  //----------------------------------------------------------------
  /**
   * Constructor for a GraphicString object from a primitive BER encoding.
   *
   * @param ber The BER encoding to use.
   * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
   * @exception	ASN1Exception if the BER encoding is incorrect.
   */

public
ASN1GraphicString(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
  {
    super(ber, false);

    if (check_tag) {
      if (ber.tag_get() != TAG || 
	  ber.tag_type_get() != BEREncoding.UNIVERSAL_TAG)
	throw new ASN1EncodingException
	  ("ASN.1 GraphicString: bad BER: tag=" + ber.tag_get() + 
	   " expected " + TAG + "\n");
    }
  }

  /*
   * ber_encode()
   * ber_decode()
   * set()
   * get() 
   * toString()
   * are inherited from base class
   */

} // ASN1GraphicString

//----------------------------------------------------------------
/*
  $Log$
  */
//----------------------------------------------------------------
//EOF
