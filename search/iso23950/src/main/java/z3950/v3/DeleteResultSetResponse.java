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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:05 UTC
 */

//----------------------------------------------------------------

package z3950.v3;
import asn1.*;


//================================================================
/**
 * Class for representing a <code>DeleteResultSetResponse</code> from <code>Z39-50-APDU-1995</code>
 *
 * <pre>
 * DeleteResultSetResponse ::=
 * SEQUENCE {
 *   referenceId ReferenceId OPTIONAL
 *   deleteOperationStatus [0] IMPLICIT DeleteSetStatus
 *   deleteListStatuses [1] IMPLICIT ListStatuses OPTIONAL
 *   numberNotDeleted [34] IMPLICIT INTEGER OPTIONAL
 *   bulkStatuses [35] IMPLICIT ListStatuses OPTIONAL
 *   deleteMessage [36] IMPLICIT InternationalString OPTIONAL
 *   otherInfo OtherInformation OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class DeleteResultSetResponse extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a DeleteResultSetResponse.
 */

public
DeleteResultSetResponse()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a DeleteResultSetResponse from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
DeleteResultSetResponse(BEREncoding ber, boolean check_tag)
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
  // DeleteResultSetResponse should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun DeleteResultSetResponse: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;

  // Decoding: referenceId ReferenceId OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun DeleteResultSetResponse: incomplete");
  }
  p = ber_cons.elementAt(part);

  try {
    s_referenceId = new ReferenceId(p, true);
    part++; // yes, consumed
  } catch (ASN1Exception e) {
    s_referenceId = null; // no, not present
  }

  // Decoding: deleteOperationStatus [0] IMPLICIT DeleteSetStatus

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun DeleteResultSetResponse: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 0 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun DeleteResultSetResponse: bad tag in s_deleteOperationStatus\n");

  s_deleteOperationStatus = new DeleteSetStatus(p, false);
  part++;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_deleteListStatuses = null;
  s_numberNotDeleted = null;
  s_bulkStatuses = null;
  s_deleteMessage = null;
  s_otherInfo = null;

  // Decoding: deleteListStatuses [1] IMPLICIT ListStatuses OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 1 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_deleteListStatuses = new ListStatuses(p, false);
    part++;
  }

  // Decoding: numberNotDeleted [34] IMPLICIT INTEGER OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 34 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_numberNotDeleted = new ASN1Integer(p, false);
    part++;
  }

  // Decoding: bulkStatuses [35] IMPLICIT ListStatuses OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 35 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_bulkStatuses = new ListStatuses(p, false);
    part++;
  }

  // Decoding: deleteMessage [36] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 36 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_deleteMessage = new InternationalString(p, false);
    part++;
  }

  // Decoding: otherInfo OtherInformation OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  try {
    s_otherInfo = new OtherInformation(p, true);
    part++; // yes, consumed
  } catch (ASN1Exception e) {
    s_otherInfo = null; // no, not present
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun DeleteResultSetResponse: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the DeleteResultSetResponse.
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
 * Returns a BER encoding of DeleteResultSetResponse, implicitly tagged.
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

  int num_fields = 1; // number of mandatories
  if (s_referenceId != null)
    num_fields++;
  if (s_deleteListStatuses != null)
    num_fields++;
  if (s_numberNotDeleted != null)
    num_fields++;
  if (s_bulkStatuses != null)
    num_fields++;
  if (s_deleteMessage != null)
    num_fields++;
  if (s_otherInfo != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;

  // Encoding s_referenceId: ReferenceId OPTIONAL

  if (s_referenceId != null) {
    fields[x++] = s_referenceId.ber_encode();
  }

  // Encoding s_deleteOperationStatus: DeleteSetStatus 

  fields[x++] = s_deleteOperationStatus.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);

  // Encoding s_deleteListStatuses: ListStatuses OPTIONAL

  if (s_deleteListStatuses != null) {
    fields[x++] = s_deleteListStatuses.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
  }

  // Encoding s_numberNotDeleted: INTEGER OPTIONAL

  if (s_numberNotDeleted != null) {
    fields[x++] = s_numberNotDeleted.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 34);
  }

  // Encoding s_bulkStatuses: ListStatuses OPTIONAL

  if (s_bulkStatuses != null) {
    fields[x++] = s_bulkStatuses.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 35);
  }

  // Encoding s_deleteMessage: InternationalString OPTIONAL

  if (s_deleteMessage != null) {
    fields[x++] = s_deleteMessage.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 36);
  }

  // Encoding s_otherInfo: OtherInformation OPTIONAL

  if (s_otherInfo != null) {
    fields[x++] = s_otherInfo.ber_encode();
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the DeleteResultSetResponse. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  if (s_referenceId != null) {
    str.append("referenceId ");
    str.append(s_referenceId);
    outputted++;
  }

  if (0 < outputted)
    str.append(", ");
  str.append("deleteOperationStatus ");
  str.append(s_deleteOperationStatus);
  outputted++;

  if (s_deleteListStatuses != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("deleteListStatuses ");
    str.append(s_deleteListStatuses);
    outputted++;
  }

  if (s_numberNotDeleted != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("numberNotDeleted ");
    str.append(s_numberNotDeleted);
    outputted++;
  }

  if (s_bulkStatuses != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("bulkStatuses ");
    str.append(s_bulkStatuses);
    outputted++;
  }

  if (s_deleteMessage != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("deleteMessage ");
    str.append(s_deleteMessage);
    outputted++;
  }

  if (s_otherInfo != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("otherInfo ");
    str.append(s_otherInfo);
    outputted++;
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public ReferenceId s_referenceId; // optional
public DeleteSetStatus s_deleteOperationStatus;
public ListStatuses s_deleteListStatuses; // optional
public ASN1Integer s_numberNotDeleted; // optional
public ListStatuses s_bulkStatuses; // optional
public InternationalString s_deleteMessage; // optional
public OtherInformation s_otherInfo; // optional

} // DeleteResultSetResponse

//----------------------------------------------------------------
//EOF