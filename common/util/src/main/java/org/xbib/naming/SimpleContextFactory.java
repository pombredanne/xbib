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
package org.xbib.naming;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;

/**
 * The Java Naming and Directory Interface (JNDI) is an application
 * programming interface (API) that provides naming and directory
 * functionality to applications written using the Java programming language.
 * It is defined to be independent of any specific directory service
 * implementation. Thus a variety of directories - new, emerging, and already
 * deployed - can be accessed in a common way.
 * 
 * The JNDI architecture consists of an API and a service provider
 * interface (SPI). Java applications use the JNDI API to access a variety of
 * naming and directory services. The SPI enables a variety of naming and
 * directory services to be plugged in transparently, thereby allowing the
 * Java application using the JNDI API to access their services.
 * 
 * The javax.naming.spi package provides the means by which developers
 * of different naming/directory service providers can develop and hook up
 * their implementations so that the corresponding services are accessible
 * from applications that use the JNDI.
 * 
 * The javax.naming.spi package allows different implementations to be
 * plugged in dynamically. These implementations include those for the initial
 * context and for contexts that can be reached from the initial context.
 * 
 * The first thing you must do when you use a JNDI naming service is to
 * obtain a context in which you can add and find names. The context that
 * represents the entire name space is known as the initial context. You need
 * to have an initial context, since all of the operations that you can
 * perform on naming and directory services are performed relative to a
 * context.
 * 
 * In Java code, you represent the initial context with an instance of
 * the javax.naming.InitialContext class. As mentioned earlier in this
 * chapter, this class implements the javax.naming.Context interface that
 * defines methods for examining and updating bindings within a naming
 * service.
 *
 */
public class SimpleContextFactory implements InitialContextFactory {
    /**
     * Get the initial context
     *
     * @param env the environment for the initial context
     *
     * @return the initial context
     */
    @Override
    public Context getInitialContext(Hashtable env) {
        return new SimpleContext(env);
    }
}
