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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:21 UTC
 */



package z3950.RS_Explain;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;
import z3950.v3.OtherInformation;
import z3950.v3.StringOrNumeric;



/**
 * Class for representing a <code>TagSetInfo_elements</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * TagSetInfo_elements ::=
 * SEQUENCE {
 *   elementname [1] IMPLICIT InternationalString
 *   nicknames [2] IMPLICIT SEQUENCE OF InternationalString OPTIONAL
 *   elementTag [3] EXPLICIT StringOrNumeric
 *   description [4] IMPLICIT HumanString OPTIONAL
 *   dataType [5] EXPLICIT PrimitiveDataType OPTIONAL
 *   otherTagInfo OtherInformation OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class TagSetInfo_elements extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a TagSetInfo_elements.
     */

    public TagSetInfo_elements() {
    }



    /**
     * Constructor for a TagSetInfo_elements from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public TagSetInfo_elements(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        super(ber, check_tag);
    }



    /**
     * Initializing object from a BER encoding.
     * This method is for internal use only. You should use
     * the constructor that takes a BEREncoding.
     *
     * @param ber       the BER to decode.
     * @param check_tag if the tag should be checked.
     * @throws ASN1Exception if the BER encoding is bad.
     */

    public void
    ber_decode(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        // TagSetInfo_elements should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun TagSetInfo_elements: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;
        BERConstructed tagged;

        // Decoding: elementname [1] IMPLICIT InternationalString

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun TagSetInfo_elements: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun TagSetInfo_elements: bad tag in s_elementname\n");
        }

        s_elementname = new InternationalString(p, false);
        part++;

        // Decoding: nicknames [2] IMPLICIT SEQUENCE OF InternationalString OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun TagSetInfo_elements: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_nicknames = new InternationalString[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_nicknames[n] = new InternationalString(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: elementTag [3] EXPLICIT StringOrNumeric

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun TagSetInfo_elements: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 3 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun TagSetInfo_elements: bad tag in s_elementTag\n");
        }

        try {
            tagged = (BERConstructed) p;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun TagSetInfo_elements: bad BER encoding: s_elementTag tag bad\n");
        }
        if (tagged.number_components() != 1) {
            throw new ASN1EncodingException
                    ("Zebulun TagSetInfo_elements: bad BER encoding: s_elementTag tag bad\n");
        }

        s_elementTag = new StringOrNumeric(tagged.elementAt(0), true);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_description = null;
        s_dataType = null;
        s_otherTagInfo = null;

        // Decoding: description [4] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_description = new HumanString(p, false);
            part++;
        }

        // Decoding: dataType [5] EXPLICIT PrimitiveDataType OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 5 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun TagSetInfo_elements: bad BER encoding: s_dataType tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun TagSetInfo_elements: bad BER encoding: s_dataType tag bad\n");
            }

            s_dataType = new PrimitiveDataType(tagged.elementAt(0), true);
            part++;
        }

        // Decoding: otherTagInfo OtherInformation OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        try {
            s_otherTagInfo = new OtherInformation(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_otherTagInfo = null; // no, not present
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun TagSetInfo_elements: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the TagSetInfo_elements.
     *
     * @exception ASN1Exception Invalid or cannot be encoded.
     * @return The BER encoding.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        return ber_encode(BEREncoding.UNIVERSAL_TAG, ASN1Sequence.TAG);
    }



    /**
     * Returns a BER encoding of TagSetInfo_elements, implicitly tagged.
     *
     * @param tag_type The type of the implicit tag.
     * @param tag      The implicit tag.
     * @return The BER encoding of the object.
     * @exception ASN1Exception When invalid or cannot be encoded.
     * @see asn1.BEREncoding#UNIVERSAL_TAG
     * @see asn1.BEREncoding#APPLICATION_TAG
     * @see asn1.BEREncoding#CONTEXT_SPECIFIC_TAG
     * @see asn1.BEREncoding#PRIVATE_TAG
     */

    public BEREncoding
    ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        // Calculate the number of fields in the encoding

        int num_fields = 2; // number of mandatories
        if (s_nicknames != null) {
            num_fields++;
        }
        if (s_description != null) {
            num_fields++;
        }
        if (s_dataType != null) {
            num_fields++;
        }
        if (s_otherTagInfo != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;
        BEREncoding enc[];

        // Encoding s_elementname: InternationalString

        fields[x++] = s_elementname.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_nicknames: SEQUENCE OF OPTIONAL

        if (s_nicknames != null) {
            f2 = new BEREncoding[s_nicknames.length];

            for (p = 0; p < s_nicknames.length; p++) {
                f2[p] = s_nicknames[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, f2);
        }

        // Encoding s_elementTag: StringOrNumeric

        enc = new BEREncoding[1];
        enc[0] = s_elementTag.ber_encode();
        fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, enc);

        // Encoding s_description: HumanString OPTIONAL

        if (s_description != null) {
            fields[x++] = s_description.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
        }

        // Encoding s_dataType: PrimitiveDataType OPTIONAL

        if (s_dataType != null) {
            enc = new BEREncoding[1];
            enc[0] = s_dataType.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, enc);
        }

        // Encoding s_otherTagInfo: OtherInformation OPTIONAL

        if (s_otherTagInfo != null) {
            fields[x++] = s_otherTagInfo.ber_encode();
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the TagSetInfo_elements.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        str.append("elementname ");
        str.append(s_elementname);
        outputted++;

        if (s_nicknames != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("nicknames ");
            str.append("{");
            for (p = 0; p < s_nicknames.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_nicknames[p]);
            }
            str.append("}");
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("elementTag ");
        str.append(s_elementTag);
        outputted++;

        if (s_description != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("description ");
            str.append(s_description);
            outputted++;
        }

        if (s_dataType != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("dataType ");
            str.append(s_dataType);
            outputted++;
        }

        if (s_otherTagInfo != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("otherTagInfo ");
            str.append(s_otherTagInfo);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public InternationalString s_elementname;
    public InternationalString s_nicknames[]; // optional
    public StringOrNumeric s_elementTag;
    public HumanString s_description; // optional
    public PrimitiveDataType s_dataType; // optional
    public OtherInformation s_otherTagInfo; // optional

} // TagSetInfo_elements


//EOF
