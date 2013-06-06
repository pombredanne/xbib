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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;


/**
 * A simple context that implements a flat context in memory.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class SimpleContext implements Context {
    /** the name parser is a flat name parser */
    private static NameParser myParser = new FlatNameParser();

    /** the bindings are stored in a hashtable */
    private Hashtable bindings = new Hashtable(11);

    /** the environment is a hash table */
    private Hashtable myEnv;

    /**
     * Creates a new SimpleContext object.
     *
     * @param environment environment
     */
    SimpleContext(Hashtable environment) {
        myEnv = (environment != null) ? (Hashtable) (environment.clone()) : null;
    }

    /**
     * Add a property to environment
     *
     * @param propName
     * @param propVal
     *
     * @return the old object
     *
     * @throws NamingException
     */
    public Object addToEnvironment(String propName, Object propVal)
        throws NamingException {
        if (myEnv == null) {
            this.myEnv = new Hashtable();
        }

        return myEnv.put(propName, propVal);
    }

    /**
     * Binds a name to an object. See {@link #bind(Name, Object)} for
     * details.
     *
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     *
     * @throws NamingException if a naming exception is encountered
     * @throws InvalidNameException DOCUMENT ME!
     * @throws NameAlreadyBoundException DOCUMENT ME!
     */
    public void bind(String name, Object obj) throws NamingException {
        if (name.equals("")) {
            throw new InvalidNameException("Cannot bind empty name");
        }

        if (bindings.get(name) != null) {
            throw new NameAlreadyBoundException("Use rebind to override");
        }

        bindings.put(name, obj);
    }

    /**
     * Binds a name to an object. All intermediate contexts and the
     * target context (that named by all but terminal atomic component of the
     * name) must already exist.
     *
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     *
     * @throws NamingException if a naming exception is encountered
     */
    public void bind(Name name, Object obj) throws NamingException {
        // Flat namespace; no federation; just call string version
        bind(name.toString(), obj);
    }

    /**
     * Close the context
     *
     * @throws NamingException
     */
    public void close() throws NamingException {
        myEnv = null;
        bindings = null;
    }

    /**
     * Compose Name
     *
     * @param name
     * @param prefix
     *
     * @return the composed name
     *
     * @throws NamingException
     */
    public String composeName(String name, String prefix)
        throws NamingException {
        Name result = composeName(new CompositeName(name), new CompositeName(prefix));

        return result.toString();
    }

    /**
     * Compose name
     *
     * @param name
     * @param prefix
     *
     * @return the composed name
     *
     * @throws NamingException
     */
    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name) (prefix.clone());

        result.addAll(name);

        return result;
    }

    /**
     * Create subcontext
     *
     * @param name
     *
     * @return the context
     *
     * @throws NamingException
     * @throws OperationNotSupportedException DOCUMENT ME!
     */
    public Context createSubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException("SimpleContext does not support subcontexts");
    }

    /**
     * Create subcontext
     *
     * @param name
     *
     * @return the context
     *
     * @throws NamingException
     */
    public Context createSubcontext(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return createSubcontext(name.toString());
    }

    /**
     * Destroy subcontext
     *
     * @param name
     *
     * @throws NamingException
     * @throws OperationNotSupportedException
     */
    public void destroySubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException("SimpleContext does not support subcontexts");
    }

    /**
     * Destroy subcontext
     *
     * @param name
     *
     * @throws NamingException
     */
    public void destroySubcontext(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        destroySubcontext(name.toString());
    }

    /**
     * Get environment
     *
     * @return the environment hash table
     *
     * @throws NamingException
     */
    public Hashtable getEnvironment() throws NamingException {
        if (myEnv == null) {
            this.myEnv = new Hashtable();
        }

        return myEnv;
    }

    /**
     * Get name in namespace
     *
     * @return the name
     *
     * @throws NamingException
     */
    public String getNameInNamespace() throws NamingException {
        return "";
    }

    /**
     * Get name parser
     *
     * @param name
     *
     * @return the name parser
     *
     * @throws NamingException
     */
    public NameParser getNameParser(String name) throws NamingException {
        return myParser;
    }

    /**
     * Get name parser
     *
     * @param name
     *
     * @return the name parser
     *
     * @throws NamingException
     */
    public NameParser getNameParser(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return getNameParser(name.toString());
    }

    /**
     * List a name
     *
     * @param name
     *
     * @return the naming enumeration
     *
     * @throws NamingException
     * @throws NotContextException
     */
    public NamingEnumeration list(String name) throws NamingException {
        if (name.equals("")) {
            // listing this context
            return new FlatNames(bindings.keys());
        }
        // Perhaps 'name' names a context
        Object target = lookup(name);
        if (target instanceof Context) {
            return ((Context) target).list("");
        }
        throw new NotContextException(name + " cannot be listed");
    }

    /**
     * List a name
     *
     * @param name
     *
     * @return the naming enumeration
     *
     * @throws NamingException
     */
    public NamingEnumeration list(Name name) throws NamingException {
        // Flat name space; no federation; just call string version
        return list(name.toString());
    }

    /**
     * List bindings
     *
     * @param name
     *
     * @return the naming enumeration
     *
     * @throws NamingException
     * @throws NotContextException
     */
    public NamingEnumeration listBindings(String name)
        throws NamingException {
        if (name.equals("")) {
            // listing this context
            return new FlatBindings(bindings.keys());
        }
        // Perhaps 'name' names a context
        Object target = lookup(name);
        if (target instanceof Context) {
            return ((Context) target).listBindings("");
        }
        throw new NotContextException(name + " cannot be listed");
    }

    /**
     * List bindings
     *
     * @param name
     *
     * @return the naming enumeration
     *
     * @throws NamingException
     */
    public NamingEnumeration listBindings(Name name) throws NamingException {
        // Flat name space; no federation; just call string version
        return listBindings(name.toString());
    }

    /**
     * Retrieves the named object. See lookup(Name) for details.
     *
     * @param name the name of the object to look up
     *
     * @return the object bound to <code>name</code>
     *
     * @throws NamingException
     * @throws NameNotFoundException
     */
    public Object lookup(String name) throws NamingException {
        if (name.equals("")) {
            // Asking to look up this context itself.  Create and return
            // a new instance with its own independent environment.
            return (new SimpleContext(myEnv));
        }
        Object answer = bindings.get(name);
        if (answer == null) {
            throw new NameNotFoundException(name + " not found");
        }
        return answer;
    }

    /**
     * <p>Retrieves the named object. If name is empty, returns a new
     * instance of this context (which represents the same naming context as
     * this context, but its environment may be modified independently and it
     * may be accessed concurrently).</p>
     *
     * @param name the name of the object to look up
     *
     * @return the object bound to <code>name</code>
     *
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookup(name.toString());
    }

    /**
     * Lookup link
     *
     * @param name
     *
     * @return the object
     *
     * @throws NamingException
     */
    public Object lookupLink(String name) throws NamingException {
        // This flat context does not treat links specially
        return lookup(name);
    }

    /**
     * Lookup link
     *
     * @param name
     *
     * @return the object
     *
     * @throws NamingException
     */
    public Object lookupLink(Name name) throws NamingException {
        // Flat name space; no federation; just call string version
        return lookupLink(name.toString());
    }

    /**
     * Rebind a name to an object.
     *
     * @param name the name to rebind
     * @param obj the object to rebind
     *
     * @throws NamingException if a naming exception is encountered
     * @throws InvalidNameException
     */
    public void rebind(String name, Object obj) throws NamingException {
        if (name.equals("")) {
            throw new InvalidNameException("Cannot bind empty name");
        }

        bindings.put(name, obj);
    }

    /**
     * Rebind a name to an object.
     *
     * @param name
     * @param obj
     *
     * @throws NamingException
     */
    public void rebind(Name name, Object obj) throws NamingException {
        // Flat namespace; no federation; just call string version
        rebind(name.toString(), obj);
    }

    /**
     * Remove from environment
     *
     * @param propName
     *
     * @return the object removed
     *
     * @throws NamingException
     */
    public Object removeFromEnvironment(String propName)
        throws NamingException {
        if (myEnv == null) {
            return null;
        }

        return myEnv.remove(propName);
    }

    /**
     * Rename from od name to new name
     *
     * @param oldname
     * @param newname
     *
     * @throws NamingException
     * @throws InvalidNameException 
     * @throws NameAlreadyBoundException 
     * @throws NameNotFoundException 
     */
    public void rename(String oldname, String newname)
        throws NamingException {
        if (oldname.equals("") || newname.equals("")) {
            throw new InvalidNameException("Cannot rename empty name");
        }

        // Check if new name exists
        if (bindings.get(newname) != null) {
            throw new NameAlreadyBoundException(newname + " is already bound");
        }

        // Check if old name is bound
        Object oldBinding = bindings.remove(oldname);

        if (oldBinding == null) {
            throw new NameNotFoundException(oldname + " not bound");
        }

        bindings.put(newname, oldBinding);
    }

    /**
     * Rename from old name to new name
     *
     * @param oldname
     * @param newname
     *
     * @throws NamingException
     */
    public void rename(Name oldname, Name newname) throws NamingException {
        // Flat namespace; no federation; just call string version
        rename(oldname.toString(), newname.toString());
    }

    /**
     * Unbind object
     *
     * @param name
     *
     * @throws NamingException
     * @throws InvalidNameException
     */
    public void unbind(String name) throws NamingException {
        if (name.equals("")) {
            throw new InvalidNameException("Cannot unbind empty name");
        }

        bindings.remove(name);
    }

    /**
     * Unbind object
     *
     * @param name
     *
     * @throws NamingException
     */
    public void unbind(Name name) throws NamingException {
        // Flat name space; no federation; just call string version
        unbind(name.toString());
    }

    /**
     * Class for enumerating bindings
     */
    class FlatBindings implements NamingEnumeration {
        private Enumeration names;

        /**
         * Contructor
         *
         * @param names names
         */
        FlatBindings(Enumeration names) {
            this.names = names;
        }

        /**
         * Close
         */
        public void close() {
        }

        /**
         * Test if there are more elements
         *
         * @return true if there are more elements
         *
         * @throws NamingException
         */
        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        /**
         * Test if there are more elements
         *
         * @return true if there are more elements
         */
        public boolean hasMoreElements() {
            return names.hasMoreElements();
        }

        /**
         * Return next element
         *
         * @return the element
         *
         * @throws NamingException
         */
        public Object next() throws NamingException {
            return nextElement();
        }

        /**
         * Return next element
         *
         * @return the element
         */
        public Object nextElement() {
            String name = (String) names.nextElement();

            return new Binding(name, bindings.get(name));
        }
    }

    /**
     * Class for flat name parsing
     */
    private static class FlatNameParser implements NameParser {
        /** syntax properties */
        private static Properties syntax = new Properties();

        static {
            syntax.put("jndi.syntax.direction", "flat");
            syntax.put("jndi.syntax.ignorecase", "false");
        }

        /**
         * Parse name
         *
         * @param name the name to parse
         *
         * @return the parsed name object
         *
         * @throws NamingException if name can't be parsed
         */
        public Name parse(String name) throws NamingException {
            return new CompoundName(name, syntax);
        }
    }

    /**
     * Class for enumerating name/class pairs
     */
    class FlatNames implements NamingEnumeration {
        private Enumeration names;

        /**
         * Creates a new flat names object.
         *
         * @param names names
         */
        FlatNames(Enumeration names) {
            this.names = names;
        }

        /**
         * Close this naming enumeration
         */
        public void close() {
        }

        /**
         * Alias for hasMoreElements
         *
         * @return true if there are more elements
         *
         * @throws NamingException
         */
        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        /**
         * Test if there are more elements
         *
         * @return true if there are more elements
         */
        public boolean hasMoreElements() {
            return names.hasMoreElements();
        }

        /**
         * Alias for next element
         *
         * @return next element
         *
         * @throws NamingException
         */
        public Object next() throws NamingException {
            return nextElement();
        }

        /**
         * Return next element
         *
         * @return the element
         */
        public Object nextElement() {
            String name = (String) names.nextElement();
            String className = bindings.get(name).getClass().getName();

            return new NameClassPair(name, className);
        }
    }
}
