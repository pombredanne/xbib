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
 * ASN1Exception
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

//----------------------------------------------------------------

public class ASN1Exception extends Exception
{
  public ASN1Exception()
  {
    super("ASN.1 exception");
  }

  public ASN1Exception(String message)
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
