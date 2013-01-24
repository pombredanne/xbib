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
 * ASN.1 UTCTime
 * <p>
 *
 * The <code>UTCTime<code> type denotes a "coordinated universal time"
 * or Greenwich Mean Time (GMT) value. The value includes the local
 * time precise to either minutes or seconds, and an offset from GMT
 * in hours and minutes.
 * It can take any of the following form:
 *   YYMMDDhhmmZ
 *   YYMMDDhhmm+hh'mm'
 *   YYMMDDhhmm-hh'mm'
 *   YYMMDDhhmmssZ
 *   YYMMDDhhmmss+hh'mm'
 *   YYMMDDhhmmss-hh'mm'
 *   YYMMDDhhmmZ
 *   YYMMDDhhmmZ
 *
 * @version	$Release$ $Date$
 * @author	Hoylen Sue <h.sue@ieee.org>
 */

//----------------------------------------------------------------

public final class ASN1UTCTime extends ASN1VisibleString
{
  //----------------------------------------------------------------
  /**
   * This constant is the UNIVERSAL tag value for UTCTime.
   */

public static final int TAG = 0x17;

  /**
   * Constructor for an UTCTime object. It sets the tag to the
   * default value of UNIVERSAL 23 (0x17).
   *
   * @param value The string value.
   */

public 
ASN1UTCTime(String value)
  {
    super(value);
  }

  //----------------------------------------------------------------
  /**
   * Constructor for a UTCTime object from a BER encoding.
   *
   * @param ber The BER encoding to use.
   * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
   * @exception	ASN1Exception If the BER encoding is incorrect.
   */

public
ASN1UTCTime(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
  {
    super(ber, false);
    
    if (check_tag) {
      if (ber.tag_get() != TAG || 
	  ber.tag_type_get() != BEREncoding.UNIVERSAL_TAG)
	throw new ASN1EncodingException
	  ("ASN.1 UTCTime: bad BER: tag=" + ber.tag_get() + 
	   " expected " + TAG + "\n");
    }
  }

  //----------------------------------------------------------------
  /**
   * Returns a BER encoding with no implicit tag.
   *
   * @return	The BER encoding
   * @exception	ASN1Exception when the object is invalid and cannot be encoded.
   */

public BEREncoding
ber_encode()
       throws ASN1Exception
  {
    return ber_encode(BEREncoding.UNIVERSAL_TAG, TAG);
  }

} // ASN1UTCTime

//----------------------------------------------------------------
/*
  $Log$
  */
//----------------------------------------------------------------
//EOF
