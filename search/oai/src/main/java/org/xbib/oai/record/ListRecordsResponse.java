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
package org.xbib.oai.record;

import org.xbib.date.DateUtil;
import org.xbib.oai.DefaultOAIResponse;
import org.xbib.oai.exceptions.BadArgumentException;
import org.xbib.oai.exceptions.BadResumptionTokenException;
import org.xbib.oai.exceptions.NoRecordsMatchException;
import org.xbib.oai.exceptions.OAIException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Writer;

public class ListRecordsResponse extends DefaultOAIResponse<ListRecordsResponse> {

    ListRecordsRequest request;

    public ListRecordsResponse(ListRecordsRequest request) {
        super(request);
        this.request = request;
    }

    public ListRecordsResponse(ListRecordsServerRequest request) {
        super(request);
    }

    @Override
    public ListRecordsResponse to(Writer writer) throws IOException {
        try {
            StreamResult streamResult = new StreamResult(writer);
            getTransformer().setResult(streamResult).transform();
        } catch (TransformerException e) {
            throw new IOException(e);
        }
        if ("noRecordsMatch".equals(getError())) {
            throw new NoRecordsMatchException("metadataPrefix=" + request.getMetadataPrefix()
                    + ",set=" + request.getSet()
                    + ",from=" + DateUtil.formatDateISO(request.getFrom())
                    + ",until=" + DateUtil.formatDateISO(request.getUntil()));
        } else if ("badResumptionToken".equals(getError())) {
            throw new BadResumptionTokenException(request.getResumptionToken());
        } else if ("badArgument".equals(getError())) {
            throw new BadArgumentException();
        } else if (getError() != null) {
            throw new OAIException(getError());
        }
        return this;
    }

}
