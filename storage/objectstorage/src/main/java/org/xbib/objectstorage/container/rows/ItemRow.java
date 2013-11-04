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
package org.xbib.objectstorage.container.rows;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ItemRow {

    private String dateRequested;

    private String dateOfLastTransition;

    private String mostRecentService;

    private String dateOfMostRecentService;

    private String mostRecentServiceNote;

    private String serviceType;

    private String state;

    private String requestType;

    private String supplier;

    private String supplierName;

    private String itemTitle;

    private String itemReferenceSource;

    private String itemReferenceCode;

    private String requester;

    private String requesterName;

    public void setDateRequested(String dateRequested) {
        this.dateRequested = dateRequested;
    }

    public String getDateRequested() {
        return dateRequested;
    }

    public void setDateOfLastTransition(String dateOfLastTransition) {
        this.dateOfLastTransition = dateOfLastTransition;
    }

    public String getDateOfLastTransition() {
        return dateOfLastTransition;
    }

    public void setMostRecentService(String mostRecentService) {
        this.mostRecentService = mostRecentService;
    }

    public String getMostRecentService() {
        return mostRecentService;
    }

    public void setDateOfMostRecentService(String dateOfMostRecentService) {
        this.dateOfMostRecentService = dateOfMostRecentService;
    }

    public String getDateOfMostRecentService() {
        return dateOfMostRecentService;
    }

    public void setMostRecentServiceNote(String mostRecentServiceNote) {
        this.mostRecentServiceNote = mostRecentServiceNote;
    }

    public String getMostRecentServiceNote() {
        return mostRecentServiceNote;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemReferenceSource(String itemReferenceSource) {
        this.itemReferenceSource = itemReferenceSource;
    }

    public String getItemReferenceSource() {
        return itemReferenceSource;
    }

    public void setItemReferenceCode(String itemReferenceCode) {
        this.itemReferenceCode = itemReferenceCode;
    }

    public String getItemReferenceCode() {
        return itemReferenceCode;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterName() {
        return requesterName;
    }

}
