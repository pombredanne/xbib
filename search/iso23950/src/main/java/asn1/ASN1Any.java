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
 * ASN1 ANY
 * <p>
 * 
 * The ANY type denotes an arbitary value of an arbitary type.
 * This class also serves as the base class for all ASN.1 classes.
 * <p>
 *
 * The ASN.1 syntax is defined in
 * <em>Information Technology - Open Systems Interconnection -
 * Specification of Abstract Syntax Notation One (ASN.1)</em>
 * AS 3625-1991
 * ISO/IEC 8824:1990
 * <p>
 *
 * The current implementation assumes values are limited to 32-bit
 * signed integers for tags, lengths, etc.
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

public class ASN1Any
{
  //----------------------------------------------------------------
  /**
   * Constructor for an ASN.1 ANY object.
   */

public
ASN1Any()
  {
  }

  //----------------------------------------------------------------
  /**
   * Constructor for an ASN.1 ANY object from a BER encoding.
   *
   * @param ber The BER encoding to use.
   * @param check_tag If true, it checks the tag. Does nothing for ASN1Any.
   * @exception	ASN1Exception if the BER encoding is incorrect.
   */

public
ASN1Any(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
  {
    // tag type and number will be set by ber_decode, which is a
    // virtual method.

    ber_decode(ber, check_tag);
  }

  //----------------------------------------------------------------
  /**
   * Method for initializing the object from a BER encoding.
   * All classes derived from this one must implement a version of this.
   *
   * This method will be overridden by derived types.
   *
   * @param ber The BER encoding to use.
   * @param check_tag If true, it checks the tag. Does nothing for ASN1Any.
   * @exception	ASN1Exception If the BER encoding is incorrect. 
   *            Never occurs for ASN1Any.
   */

public void
ber_decode(BEREncoding ber_enc, boolean check_tag)
       throws ASN1Exception
  {
    asn1any_ber = ber_enc;
  }


  //----------------------------------------------------------------
  /**
   * Constructs a BER encoding for this ASN.1 object.
   * This method is usually overridden by a subclass method.
   *
   * @exception	ASN1Exception If the object cannot be BER encoded. 
   */

public BEREncoding
ber_encode()
       throws ASN1Exception
  {
    if (asn1any_ber == null)
      throw new ASN1EncodingException("ASN.1 ANY: uninitialised");

    return asn1any_ber;
  }

  //----------------------------------------------------------------
  /**
   * Returns a BER encoding of ASN1Any, implicitly tagged.
   *
   * @param	tag_type The type of the implicit tag.
   * @param	tag The implicit tag number.
   * @return	The BER encoding of the object.
   * @exception	ASN1Exception when invalid or cannot be encoded.
   */

public BEREncoding
ber_encode(int tag_type, int tag)
       throws ASN1Exception
{
  if (asn1any_ber == null)
    throw new ASN1EncodingException("ASN.1 ANY: uninitialised");

  // Can't really do it, this method is really for overriding
  // in the subclasses.

  throw new ASN1EncodingException("ASN.1 ANY: cannot implicitly tag");
}

  //----------------------------------------------------------------
  /**
   * Returns a new String object representing this ASN.1 object's value. 
   *
   * @return A text string representation.
   */

public String
toString()
  {
    if (asn1any_ber == null)
      return "<empty ASN.1 ANY>";

    return asn1any_ber.toString();
  }

  //================================================================
  /* Hack to support creation of ASN1 ANY types from a BER and have
     it behave normally.  This is not used by any other ASN.1 subclasses.
     It is a waste of space in that respect. */

private BEREncoding asn1any_ber;

} // ASN1Any

//----------------------------------------------------------------
/*
  $Log$
  */
//----------------------------------------------------------------
//EOF
