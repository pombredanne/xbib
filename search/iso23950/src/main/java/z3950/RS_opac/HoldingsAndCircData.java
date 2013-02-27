/*
 * $Source$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 1998, Hoylen Sue.  All Rights Reserved.
 * <h.sue@ieee.org>
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  Refer to
 * the supplied license for more details.
 *
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:22 UTC
 */

//----------------------------------------------------------------

package z3950.RS_opac;
import asn1.*;
import z3950.v3.InternationalString;

//================================================================
/**
 * Class for representing a <code>HoldingsAndCircData</code> from <code>RecordSyntax-opac</code>
 *
 * <pre>
 * HoldingsAndCircData ::=
 * SEQUENCE {
 *   typeOfRecord [1] IMPLICIT InternationalString OPTIONAL
 *   encodingLevel [2] IMPLICIT InternationalString OPTIONAL
 *   format [3] IMPLICIT InternationalString OPTIONAL
 *   receiptAcqStatus [4] IMPLICIT InternationalString OPTIONAL
 *   generalRetention [5] IMPLICIT InternationalString OPTIONAL
 *   completeness [6] IMPLICIT InternationalString OPTIONAL
 *   dateOfReport [7] IMPLICIT InternationalString OPTIONAL
 *   nucCode [8] IMPLICIT InternationalString OPTIONAL
 *   localLocation [9] IMPLICIT InternationalString OPTIONAL
 *   shelvingLocation [10] IMPLICIT InternationalString OPTIONAL
 *   callNumber [11] IMPLICIT InternationalString OPTIONAL
 *   shelvingData [12] IMPLICIT InternationalString OPTIONAL
 *   copyNumber [13] IMPLICIT InternationalString OPTIONAL
 *   publicNote [14] IMPLICIT InternationalString OPTIONAL
 *   reproductionNote [15] IMPLICIT InternationalString OPTIONAL
 *   termsUseRepro [16] IMPLICIT InternationalString OPTIONAL
 *   enumAndChron [17] IMPLICIT InternationalString OPTIONAL
 *   volumes [18] IMPLICIT SEQUENCE OF Volume OPTIONAL
 *   circulationData [19] IMPLICIT SEQUENCE OF CircRecord OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class HoldingsAndCircData extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a HoldingsAndCircData.
 */

public
HoldingsAndCircData()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a HoldingsAndCircData from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
HoldingsAndCircData(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
{
  super(ber, check_tag);
}

//----------------------------------------------------------------
/**
 * Initializing object from a BER encoding.
 * This method is for internal use only. You should use
 * the constructor that takes a BEREncoding.
 *
 * @param ber the BER to decode.
 * @param check_tag if the tag should be checked.
 * @exception ASN1Exception if the BER encoding is bad.
 */

public void
ber_decode(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
{
  // HoldingsAndCircData should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun HoldingsAndCircData: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_typeOfRecord = null;
  s_encodingLevel = null;
  s_format = null;
  s_receiptAcqStatus = null;
  s_generalRetention = null;
  s_completeness = null;
  s_dateOfReport = null;
  s_nucCode = null;
  s_localLocation = null;
  s_shelvingLocation = null;
  s_callNumber = null;
  s_shelvingData = null;
  s_copyNumber = null;
  s_publicNote = null;
  s_reproductionNote = null;
  s_termsUseRepro = null;
  s_enumAndChron = null;
  s_volumes = null;
  s_circulationData = null;

  // Decoding: typeOfRecord [1] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 1 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_typeOfRecord = new InternationalString(p, false);
    part++;
  }

  // Decoding: encodingLevel [2] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 2 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_encodingLevel = new InternationalString(p, false);
    part++;
  }

  // Decoding: format [3] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 3 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_format = new InternationalString(p, false);
    part++;
  }

  // Decoding: receiptAcqStatus [4] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 4 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_receiptAcqStatus = new InternationalString(p, false);
    part++;
  }

  // Decoding: generalRetention [5] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 5 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_generalRetention = new InternationalString(p, false);
    part++;
  }

  // Decoding: completeness [6] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 6 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_completeness = new InternationalString(p, false);
    part++;
  }

  // Decoding: dateOfReport [7] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 7 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_dateOfReport = new InternationalString(p, false);
    part++;
  }

  // Decoding: nucCode [8] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 8 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_nucCode = new InternationalString(p, false);
    part++;
  }

  // Decoding: localLocation [9] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 9 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_localLocation = new InternationalString(p, false);
    part++;
  }

  // Decoding: shelvingLocation [10] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 10 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_shelvingLocation = new InternationalString(p, false);
    part++;
  }

  // Decoding: callNumber [11] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 11 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_callNumber = new InternationalString(p, false);
    part++;
  }

  // Decoding: shelvingData [12] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 12 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_shelvingData = new InternationalString(p, false);
    part++;
  }

  // Decoding: copyNumber [13] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 13 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_copyNumber = new InternationalString(p, false);
    part++;
  }

  // Decoding: publicNote [14] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 14 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_publicNote = new InternationalString(p, false);
    part++;
  }

  // Decoding: reproductionNote [15] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 15 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_reproductionNote = new InternationalString(p, false);
    part++;
  }

  // Decoding: termsUseRepro [16] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 16 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_termsUseRepro = new InternationalString(p, false);
    part++;
  }

  // Decoding: enumAndChron [17] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 17 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_enumAndChron = new InternationalString(p, false);
    part++;
  }

  // Decoding: volumes [18] IMPLICIT SEQUENCE OF Volume OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 18 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_volumes = new Volume[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_volumes[n] = new Volume(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Decoding: circulationData [19] IMPLICIT SEQUENCE OF CircRecord OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 19 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_circulationData = new CircRecord[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_circulationData[n] = new CircRecord(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun HoldingsAndCircData: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the HoldingsAndCircData.
 *
 * @exception	ASN1Exception Invalid or cannot be encoded.
 * @return	The BER encoding.
 */

public BEREncoding
ber_encode()
       throws ASN1Exception
{
  return ber_encode(BEREncoding.UNIVERSAL_TAG, ASN1Sequence.TAG);
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of HoldingsAndCircData, implicitly tagged.
 *
 * @param tag_type	The type of the implicit tag.
 * @param tag	The implicit tag.
 * @return	The BER encoding of the object.
 * @exception	ASN1Exception When invalid or cannot be encoded.
 * @see asn1.BEREncoding#UNIVERSAL_TAG
 * @see asn1.BEREncoding#APPLICATION_TAG
 * @see asn1.BEREncoding#CONTEXT_SPECIFIC_TAG
 * @see asn1.BEREncoding#PRIVATE_TAG
 */

public BEREncoding
ber_encode(int tag_type, int tag)
       throws ASN1Exception
{
  // Calculate the number of fields in the encoding

  int num_fields = 0; // number of mandatories
  if (s_typeOfRecord != null)
    num_fields++;
  if (s_encodingLevel != null)
    num_fields++;
  if (s_format != null)
    num_fields++;
  if (s_receiptAcqStatus != null)
    num_fields++;
  if (s_generalRetention != null)
    num_fields++;
  if (s_completeness != null)
    num_fields++;
  if (s_dateOfReport != null)
    num_fields++;
  if (s_nucCode != null)
    num_fields++;
  if (s_localLocation != null)
    num_fields++;
  if (s_shelvingLocation != null)
    num_fields++;
  if (s_callNumber != null)
    num_fields++;
  if (s_shelvingData != null)
    num_fields++;
  if (s_copyNumber != null)
    num_fields++;
  if (s_publicNote != null)
    num_fields++;
  if (s_reproductionNote != null)
    num_fields++;
  if (s_termsUseRepro != null)
    num_fields++;
  if (s_enumAndChron != null)
    num_fields++;
  if (s_volumes != null)
    num_fields++;
  if (s_circulationData != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;
  BEREncoding f2[];
  int p;

  // Encoding s_typeOfRecord: InternationalString OPTIONAL

  if (s_typeOfRecord != null) {
    fields[x++] = s_typeOfRecord.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
  }

  // Encoding s_encodingLevel: InternationalString OPTIONAL

  if (s_encodingLevel != null) {
    fields[x++] = s_encodingLevel.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
  }

  // Encoding s_format: InternationalString OPTIONAL

  if (s_format != null) {
    fields[x++] = s_format.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
  }

  // Encoding s_receiptAcqStatus: InternationalString OPTIONAL

  if (s_receiptAcqStatus != null) {
    fields[x++] = s_receiptAcqStatus.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
  }

  // Encoding s_generalRetention: InternationalString OPTIONAL

  if (s_generalRetention != null) {
    fields[x++] = s_generalRetention.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);
  }

  // Encoding s_completeness: InternationalString OPTIONAL

  if (s_completeness != null) {
    fields[x++] = s_completeness.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);
  }

  // Encoding s_dateOfReport: InternationalString OPTIONAL

  if (s_dateOfReport != null) {
    fields[x++] = s_dateOfReport.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);
  }

  // Encoding s_nucCode: InternationalString OPTIONAL

  if (s_nucCode != null) {
    fields[x++] = s_nucCode.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 8);
  }

  // Encoding s_localLocation: InternationalString OPTIONAL

  if (s_localLocation != null) {
    fields[x++] = s_localLocation.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 9);
  }

  // Encoding s_shelvingLocation: InternationalString OPTIONAL

  if (s_shelvingLocation != null) {
    fields[x++] = s_shelvingLocation.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 10);
  }

  // Encoding s_callNumber: InternationalString OPTIONAL

  if (s_callNumber != null) {
    fields[x++] = s_callNumber.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 11);
  }

  // Encoding s_shelvingData: InternationalString OPTIONAL

  if (s_shelvingData != null) {
    fields[x++] = s_shelvingData.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 12);
  }

  // Encoding s_copyNumber: InternationalString OPTIONAL

  if (s_copyNumber != null) {
    fields[x++] = s_copyNumber.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 13);
  }

  // Encoding s_publicNote: InternationalString OPTIONAL

  if (s_publicNote != null) {
    fields[x++] = s_publicNote.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 14);
  }

  // Encoding s_reproductionNote: InternationalString OPTIONAL

  if (s_reproductionNote != null) {
    fields[x++] = s_reproductionNote.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 15);
  }

  // Encoding s_termsUseRepro: InternationalString OPTIONAL

  if (s_termsUseRepro != null) {
    fields[x++] = s_termsUseRepro.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 16);
  }

  // Encoding s_enumAndChron: InternationalString OPTIONAL

  if (s_enumAndChron != null) {
    fields[x++] = s_enumAndChron.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 17);
  }

  // Encoding s_volumes: SEQUENCE OF OPTIONAL

  if (s_volumes != null) {
    f2 = new BEREncoding[s_volumes.length];

    for (p = 0; p < s_volumes.length; p++) {
      f2[p] = s_volumes[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 18, f2);
  }

  // Encoding s_circulationData: SEQUENCE OF OPTIONAL

  if (s_circulationData != null) {
    f2 = new BEREncoding[s_circulationData.length];

    for (p = 0; p < s_circulationData.length; p++) {
      f2[p] = s_circulationData[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 19, f2);
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the HoldingsAndCircData. 
 */

public String
toString()
{
  int p;
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  if (s_typeOfRecord != null) {
    str.append("typeOfRecord ");
    str.append(s_typeOfRecord);
    outputted++;
  }

  if (s_encodingLevel != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("encodingLevel ");
    str.append(s_encodingLevel);
    outputted++;
  }

  if (s_format != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("format ");
    str.append(s_format);
    outputted++;
  }

  if (s_receiptAcqStatus != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("receiptAcqStatus ");
    str.append(s_receiptAcqStatus);
    outputted++;
  }

  if (s_generalRetention != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("generalRetention ");
    str.append(s_generalRetention);
    outputted++;
  }

  if (s_completeness != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("completeness ");
    str.append(s_completeness);
    outputted++;
  }

  if (s_dateOfReport != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("dateOfReport ");
    str.append(s_dateOfReport);
    outputted++;
  }

  if (s_nucCode != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("nucCode ");
    str.append(s_nucCode);
    outputted++;
  }

  if (s_localLocation != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("localLocation ");
    str.append(s_localLocation);
    outputted++;
  }

  if (s_shelvingLocation != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("shelvingLocation ");
    str.append(s_shelvingLocation);
    outputted++;
  }

  if (s_callNumber != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("callNumber ");
    str.append(s_callNumber);
    outputted++;
  }

  if (s_shelvingData != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("shelvingData ");
    str.append(s_shelvingData);
    outputted++;
  }

  if (s_copyNumber != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("copyNumber ");
    str.append(s_copyNumber);
    outputted++;
  }

  if (s_publicNote != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("publicNote ");
    str.append(s_publicNote);
    outputted++;
  }

  if (s_reproductionNote != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("reproductionNote ");
    str.append(s_reproductionNote);
    outputted++;
  }

  if (s_termsUseRepro != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("termsUseRepro ");
    str.append(s_termsUseRepro);
    outputted++;
  }

  if (s_enumAndChron != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("enumAndChron ");
    str.append(s_enumAndChron);
    outputted++;
  }

  if (s_volumes != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("volumes ");
    str.append("{");
    for (p = 0; p < s_volumes.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_volumes[p]);
    }
    str.append("}");
    outputted++;
  }

  if (s_circulationData != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("circulationData ");
    str.append("{");
    for (p = 0; p < s_circulationData.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_circulationData[p]);
    }
    str.append("}");
    outputted++;
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public InternationalString s_typeOfRecord; // optional
public InternationalString s_encodingLevel; // optional
public InternationalString s_format; // optional
public InternationalString s_receiptAcqStatus; // optional
public InternationalString s_generalRetention; // optional
public InternationalString s_completeness; // optional
public InternationalString s_dateOfReport; // optional
public InternationalString s_nucCode; // optional
public InternationalString s_localLocation; // optional
public InternationalString s_shelvingLocation; // optional
public InternationalString s_callNumber; // optional
public InternationalString s_shelvingData; // optional
public InternationalString s_copyNumber; // optional
public InternationalString s_publicNote; // optional
public InternationalString s_reproductionNote; // optional
public InternationalString s_termsUseRepro; // optional
public InternationalString s_enumAndChron; // optional
public Volume s_volumes[]; // optional
public CircRecord s_circulationData[]; // optional

} // HoldingsAndCircData

//----------------------------------------------------------------
//EOF