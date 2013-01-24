/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Affero General Public License as published 
 * by the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses 
 * or write to the Free Software Foundation, Inc., 51 Franklin Street, 
 * Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * The interactive user interfaces in modified source and object code 
 * versions of this program must display Appropriate Legal Notices, 
 * as required under Section 5 of the GNU Affero General Public License.
 * 
 * In accordance with Section 7(b) of the GNU Affero General Public 
 * License, these Appropriate Legal Notices must retain the display of the 
 * "Powered by xbib" logo. If the display of the logo is not reasonably 
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.io.iso23950;

import asn1.ASN1Exception;
import asn1.ASN1External;
import asn1.ASN1GeneralString;
import asn1.ASN1Integer;
import asn1.ASN1ObjectIdentifier;
import asn1.BEREncoding;
import java.io.IOException;
import java.util.LinkedList;
import z3950.v3.ElementSetNames;
import z3950.v3.InternationalString;
import z3950.v3.NamePlusRecord;
import z3950.v3.PDU;
import z3950.v3.PresentRequest;
import z3950.v3.PresentRequest_recordComposition;
import z3950.v3.PresentResponse;
import z3950.v3.PresentStatus;
import z3950.v3.ResultSetId;

/**
 *  Present operation for Z39.50
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class PresentOperation {

    private long millis;
    private int nReturned;
    private int status;
    private String resultSetName;
    private String elementSetName;
    public int offset;
    public int length;
    public String preferredRecordSyntax;


    public PresentOperation(String resultSetName, String elementSetName,
            String preferredRecordSyntax, int offset, int length) {
        this.resultSetName = resultSetName;
        this.elementSetName = elementSetName;
        this.preferredRecordSyntax = preferredRecordSyntax;
        this.offset = offset;
        this.length = length;
    }

    public void execute(ZSession session, RecordHandler handler) throws IOException {
        PresentRequest pr = new PresentRequest();
        pr.s_resultSetId = new ResultSetId();
        pr.s_resultSetId.value = new InternationalString();
        pr.s_resultSetId.value.value = new ASN1GeneralString(resultSetName);
        pr.s_resultSetStartPoint = new ASN1Integer(offset);
        pr.s_numberOfRecordsRequested = new ASN1Integer(length);
        pr.s_recordComposition = new PresentRequest_recordComposition();
        pr.s_recordComposition.c_simple = new ElementSetNames();
        pr.s_recordComposition.c_simple.c_genericElementSetName = new InternationalString();
        pr.s_recordComposition.c_simple.c_genericElementSetName.value = new ASN1GeneralString(elementSetName);
        pr.s_preferredRecordSyntax = new ASN1ObjectIdentifier(makeOID(preferredRecordSyntax));
        PDU pdu = new PDU();
        pdu.c_presentRequest = pr;
        this.millis = System.currentTimeMillis();
        session.getConnection().writePDU(pdu);
        pdu = session.getConnection().readPDU();
        this.millis = System.currentTimeMillis() - millis;
        PresentResponse response = pdu.c_presentResponse;
        this.nReturned = response.s_numberOfRecordsReturned != null
                ? response.s_numberOfRecordsReturned.get() : 0;
        this.status = response.s_presentStatus.value != null
                ? response.s_presentStatus.value.get() : 0;
        if (status == PresentStatus.E_success) {
            for (int n = 0; n < nReturned; n++) {
                NamePlusRecord nr = response.s_records.c_responseRecords[n];
                try {
                    if (nr.s_record.c_retrievalRecord != null) {
                        ASN1External asn1External = new ASN1External(nr.s_record.c_retrievalRecord.ber_encode(), true);
                        Record record = new Record(offset + n, asn1External.c_octetAligned.get_bytes());
                        if (handler != null) {
                            handler.receivedRecord(record);
                        }                        
                    } else if (nr.s_record.c_surrogateDiagnostic != null) {
                        ASN1External asn1External = new ASN1External(nr.s_record.c_surrogateDiagnostic.c_defaultFormat.ber_encode(), true);
                        ErrorRecord record = new ErrorRecord(offset + n, asn1External.c_octetAligned.get_bytes());
                        if (handler != null) {
                            handler.receivedRecord(record);
                        }
                    }
                } catch (ASN1Exception e) {
                    throw new IOException("Present error: " + e.getMessage());
                }
            }
        } else {
            throw createIOExceptionFrom(status, nReturned, response);
        }
    }

    public long getMillis() {
        return millis;
    }

    public int getStatus() {
        return status;
    }

    public int getNumRecordsReturned() {
        return nReturned;
    }

    private int[] makeOID(String str) throws NumberFormatException {
        String[] s = str.split("\\.");
        int[] a = new int[s.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = Integer.parseInt(s[i]);
        }
        return a;
    }

    private IOException createIOExceptionFrom(int status, int nReturned, PresentResponse response) {
        String message = null;
        IOException ex;
        switch (status) {
            case 1:
                message = "Some records were not returned (request was terminated by access control)";
                break;
            case 2:
                message = "Some records were not returned (message size is too small)";
                break;
            case 3:
                message = "Some records were not returned (request was terminated by resource control, at origin request)";
                break;
            case 4:
                message = "Some records were not returned (request was terminated by resource control, by the target)";
                break;
            case 5:
                message = "no records were returned (one or more non-surrogate diagnostics were returned)";
                break;
        }
        LinkedList<BEREncoding> diag = new LinkedList();
        try {
            if (response.s_records.c_multipleNonSurDiagnostics != null) {
                for (int i = 0; i < response.s_records.c_multipleNonSurDiagnostics.length; i++) {
                    diag.add(response.s_records.c_multipleNonSurDiagnostics[i].c_defaultFormat.ber_encode());
                }
            } else if (response.s_records.c_nonSurrogateDiagnostic != null) {
                diag.add(response.s_records.c_nonSurrogateDiagnostic.ber_encode()  );
            }
            ex = new IOException("Status = " + status + " nReturned = " + nReturned + " message = " + message + " diag = " + diag);
        } catch (ASN1Exception e) {
            ex = new IOException("Status = " + status + " nReturned = " + nReturned + " message = " + message + " diag = not available");
        }
        return ex;
    }
}
