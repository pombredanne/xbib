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
 * Representation for an ASN.1 OBJECT DESCRIPTOR.
 *
 * The ASN.1 OBJECT DESCRIPTOR consists of a human-readable text
 * which serves to describe an information object.
 *
 * According to clause 35.3 of the standard,
 * ObjectDescriptor ::= [UNIVERSAL 7] IMPLICIT GraphicString
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

//----------------------------------------------------------------

public final class ASN1ObjectDescriptor extends ASN1GraphicString
{
  //----------------------------------------------------------------
  /**
   * This constant is the UNIVERSAL tag value for ObjectDescriptor.
   */

public static final int TAG = 0x07;

  /**
   * Constructor for an ObjectDescriptor object. It sets the tag to the
   * default value of UNIVERSAL 7, and the descriptor to the given value.
   */

public 
ASN1ObjectDescriptor(String descriptor)
  {
    super(descriptor);
  }

  //----------------------------------------------------------------
  /**
   * Constructor for a ObjectDescriptor object from a primitive BER encoding.
   *
   * @param ber_enc The BER encoding to use.
   * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
   * @exception	ASN1Exception if the BER encoding is incorrect.
   */

public
ASN1ObjectDescriptor(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
  {
    super(ber, false);

    if (check_tag) {
      if (ber.tag_get() != TAG || 
	  ber.tag_type_get() != BEREncoding.UNIVERSAL_TAG)
	throw new ASN1EncodingException
	  ("ASN.1 ObjectDescriptor: bad BER: tag=" + ber.tag_get() + 
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

  /* ber_encode()
   * ber_decode()
   * set()
   * get() 
   * toString()
   * are inherited
   */

} // ASN1ObjectDescriptor

//----------------------------------------------------------------
/*
  $Log$
  */
//----------------------------------------------------------------
//EOF
