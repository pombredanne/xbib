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
 * ASN1EncodingException
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

//----------------------------------------------------------------

public class ASN1EncodingException extends ASN1Exception
{
  public ASN1EncodingException()
  {
    super("ASN.1 encoding exception");
  }

  public ASN1EncodingException(String message)
  {
    super(message);
  }
}

//----------------------------------------------------------------
/*
  $Log$
  */
//----------------------------------------------------------------
//EOF
