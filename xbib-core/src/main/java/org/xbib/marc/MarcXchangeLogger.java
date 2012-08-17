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
package org.xbib.marc;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;


public class LoggingMarcXchangeListener implements MarcXchangeListener {

    private static final Logger logger = LoggerFactory.getLogger(LoggingMarcXchangeListener.class.getName());

    @Override
    public void beginRecord(String format, String type) {
        logger.info("begin record format={0} type = {1}", new Object[]{format, type});
    }

    @Override
    public void endRecord() {
        logger.info("end record");
    }

    @Override
    public void leader(String label) {
        logger.info("leader = {0}", label);
    }

    @Override
    public void trailer(String trailer) {
        logger.info("trailer = {0}", trailer);
    }
    
    @Override
    public void beginControlField(FieldDesignator designator) {
        logger.info("begin control field = {0}", designator);
    }

    @Override
    public void endControlField(FieldDesignator designator) {
        logger.info("end control field = {0} {1}", new Object[]{designator, designator != null ? designator.getData() : null});
    }

    @Override
    public void beginDataField(FieldDesignator designator) {
        logger.info("begin data field = {0}", designator);
    }

    @Override
    public void endDataField(FieldDesignator designator) {
        logger.info("end data field = {0} {1}", new Object[]{designator, designator != null ? designator.getData() : null});
    }

    @Override
    public void beginSubField(FieldDesignator designator) {
        logger.info("begin sub field = {0}", designator);
    }

    @Override
    public void endSubField(FieldDesignator designator) {
        logger.info("end sub field = {0} {1}", new Object[]{designator, designator != null ? designator.getData() : null});
    }
}
