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
 * Representation of an ASN.1 SET.
 * <p>
 *
 * The <code>SET</code> type denotes an ordered collection
 * of one or more types. The SET OF type denotes an ordered
 * collection of zero or more occurances of a given type.
 * <p>
 *
 * This class is available for the generic handling of ASN.1 
 * definitions. However, specialised ASN.1 productions will usually
 * use their own encoding for SETs directly.
 * <p>
 *
 * For DER encoding, DEFAULTs are not included and all the elements
 * are sorted according to tag number.
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

//----------------------------------------------------------------

public final class ASN1Set extends ASN1Any
{
  /**
   * This constant value is the ASN.1 UNIVERSAL tag value indicating
   * a SET or a SET OF type.
   */

public static final int TAG = 0x11;

  //----------------------------------------------------------------
  /**
   * Constructor for an ASN.1 SET object. The tag is set to the
   * default of UNIVERSAL 17 (0x11) and the elements to the given array.
   *
   * @param element_array Elements in the set.
   */

public 
ASN1Set(ASN1Any element_array[])
  {
    elements = element_array;
  }

  //----------------------------------------------------------------
  /**
   * Constructor for a SET object from a BER encoding.
   *
   * @param ber The BER encoding to use.
   * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
   * @exception	ASN1Exception If the BER encoding is incorrect.
   */

public
ASN1Set(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
  {
    super(ber, check_tag);
  }

  //----------------------------------------------------------------
  /**
   * Method for initializing the object from a BER encoding.
   *
   * @param ber_enc The BER encoding to use.
   * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
   * @exception	ASN1Exception If the BER encoding is incorrect.
   */

public void
ber_decode(BEREncoding ber_enc, boolean check_tag)
       throws ASN1Exception
  {
    if (check_tag) {
      if (ber_enc.tag_get() != TAG || 
	  ber_enc.tag_type_get() != BEREncoding.UNIVERSAL_TAG)
	throw new ASN1EncodingException
	  ("ASN.1 SET: bad BER: tag=" + ber_enc.tag_get() +
	   " expected " + TAG + "\n");
    }

    if (ber_enc instanceof BERPrimitive)
      throw new ASN1EncodingException("ASN.1 SET: bad form, primitive");

    BERConstructed ber = (BERConstructed) ber_enc;

    int len = ber.number_components();
    
    elements = new ASN1Any[len];

    for (int x = 0; x < len; x++)
      elements[x] = ASN1Decoder.toASN1(ber.elementAt(x));
  }
   
  //----------------------------------------------------------------
  /**
   * Returns a BER encoding of the SET.
   *
   * @return	The BER encoding of the SET
   * @exception	ASN1Exception when the SET is invalid and cannot be encoded.
   */

public BEREncoding
ber_encode()
       throws ASN1Exception
  {
    return ber_encode(BEREncoding.UNIVERSAL_TAG, TAG);
  }

  //----------------------------------------------------------------
  /**
   * Returns a BER encoding of the SET.
   *
   * @param tag_type The type of the implcit tag
   * @param tag The implicit tag number
   * @return	The BER encoding of the SET
   * @exception	ASN1Exception when the SET is invalid and cannot be encoded.
   */

public BEREncoding
ber_encode(int tag_type, int tag)
       throws ASN1Exception
  {
    int len = elements.length;
    BEREncoding encodings[] = new BEREncoding[len];
    
    for (int index = 0; index < len; index++)
      encodings[index] = elements[index].ber_encode();
      
    return new BERConstructed(tag_type, tag, encodings);
  }

  //----------------------------------------------------------------
  /**
   * Method to set the SET's elements.
   *
   * @param element_array  an array of ASN.1 objects.
   */

public void
set(ASN1Any element_array[])
  {
    elements = element_array;
  }

  //----------------------------------------------------------------
  /**
   * Method to get the elements of the SET.
   *
   * @return	an array containing the SET's elements.
   */

public ASN1Any[]
get()
  {
    return elements;
  }

  //----------------------------------------------------------------
  /**
   * Returns a new String object representing this ASN.1 object's value. 
   */

public String
toString()
  {
    StringBuffer str = new StringBuffer("{");

    for (int index = 0; index < elements.length; index++) {
      if (index != 0)
	str.append(", ");

      str.append(elements[index].toString());
    }

    str.append('}');

    return new String(str);
  }

  //----------------------------------------------------------------
  /**
   * The values of the SET are stored in this array.
   */

private ASN1Any elements[];

} // ASN1Set

//----------------------------------------------------------------
/*
  $Log$
  */
//----------------------------------------------------------------
//EOF
