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
 * BERConstructed
 *
 * This class represents a BER encoded ASN.1 object which is
 * constructed from component BER encodings.
 * <p>
 *
 * Generally it is used to store the BER encoding of constructed types
 * (i.e. SEQUENCE, SEQUENCE OF, SET, and SET OF) The end-of-content
 * octets, if required, must be added to the end of the elements by
 * the creator.
 *
 * @see asn1.BEREncoding
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

//----------------------------------------------------------------

public class BERConstructed extends BEREncoding
{
  //----------------------------------------------------------------
  /**
   * Constructor for a non-primitive BEREncoding.
   *
   * @param asn1_class The tag type.
   * @param tag The tag number.
   * @param elements The components making up the constructed BER.
   * @exception ASN1Exception If tag or tag type is invalid
   * @see asn1.BEREncoding#UNIVERSAL_TAG
   * @see asn1.BEREncoding#APPLICATION_TAG
   * @see asn1.BEREncoding#CONTEXT_SPECIFIC_TAG
   * @see asn1.BEREncoding#PRIVATE_TAG
   */

  public 
  BERConstructed(int asn1_class, int tag, BEREncoding elements[])
       throws ASN1Exception
  {
    int content_length = 0;
    for (int index = 0; index < elements.length; index++) {
      content_length += elements[index].i_total_length;
    }

    init(asn1_class, /* constructed */ true, tag, content_length);
    content_elements = elements;
  }

  //----------------------------------------------------------------
  /*
   * This method outputs the encoded octets for this object
   * to the output stream.
   *
   * Note: the output is not flushed, so you <strong>must</strong>  explicitly
   * flush the output stream after calling this method to ensure that
   * the data has been written out.
   *
   * @param	dest - OutputStream to write encoding to.
   */

public void
output(OutputStream dest)
       throws java.io.IOException
  {
    output_head(dest);

    for (int index = 0; index < content_elements.length; index++)
      content_elements[index].output(dest);
  }

  //----------------------------------------------------------------
  /**
   * This method returns the number of BER encoded elements that this
   * object is made up of to be returned.
   */

public int
number_components()
  {
    return content_elements.length;
  }

  //----------------------------------------------------------------
  /**
   * This method allows the elements of the BER encoding to be examined.
   * @param	index - the index of the BER object required,
   *            it must be in the range, [0, number_components() - 1]
   */

public BEREncoding
elementAt(int index)
  {
    return content_elements[index];
  }

  //----------------------------------------------------------------
  /**
   * Returns a new String object representing this ASN.1 object's value. 
   */

public String
toString()
  {
    StringBuffer str = new StringBuffer("[");
    switch (i_tag_type) {
    case BEREncoding.UNIVERSAL_TAG:
      str.append("UNIVERSAL ");
      break;
    case BEREncoding.APPLICATION_TAG:
      str.append("APPLICATION ");
      break;
    case BEREncoding.CONTEXT_SPECIFIC_TAG:
      str.append("CONTEXT SPECIFIC ");
      break;
    case BEREncoding.PRIVATE_TAG:
      str.append("PRIVATE ");
      break;
    }
    str.append(String.valueOf(i_tag) + "]{");

    for (int x = 0; x < content_elements.length; x++) {
      if (x != 0)
        str.append(',');

      str.append(content_elements[x].toString());
    }
    
    str.append('}');

    return new String(str);
  }

  //----------------------------------------------------------------
  /**
   * This protected method is used to implement the "get_encoding" method.
   */

protected int
i_encoding_get(int offset, byte data[])
  {
    offset = i_get_head(offset, data);

    for (int index = 0; index < content_elements.length; index++)
      offset = content_elements[index].i_encoding_get(offset, data);

    return offset;
  }

  //----------------------------------------------------------------
  /**
   * This variable stores the BER encoding elements which make 
   * up this constucted BER encoding (in order).
   */

  private BEREncoding content_elements[];
}

//----------------------------------------------------------------
/*
  $Log$
  */
//----------------------------------------------------------------
//EOF
