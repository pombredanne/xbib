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
package org.xbib.tools.opt.internal;

import org.xbib.tools.opt.ValueConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static org.xbib.util.Classes.wrapperOf;

/**
 * <p>Helper methods for reflection.</p>
 *
 */
public final class Reflection {
    private Reflection() {
        throw new UnsupportedOperationException();
    }

    /**
     * Finds an appropriate value converter for the given class.
     *
     * @param <V>   a constraint on the class object to introspect
     * @param clazz class to introspect on
     * @return a converter method or constructor
     */
    public static <V> ValueConverter<V> findConverter(Class<V> clazz) {
        Class<V> maybeWrapper = wrapperOf(clazz);

        ValueConverter<V> valueOf = valueOfConverter(maybeWrapper);
        if (valueOf != null) {
            return valueOf;
        }

        ValueConverter<V> constructor = constructorConverter(maybeWrapper);
        if (constructor != null) {
            return constructor;
        }

        throw new IllegalArgumentException(clazz + " is not a value type");
    }

    private static <V> ValueConverter<V> valueOfConverter(Class<V> clazz) {
        try {
            Method valueOf = clazz.getDeclaredMethod("valueOf", String.class);
            if (meetsConverterRequirements(valueOf, clazz)) {
                return new MethodInvokingValueConverter<V>(valueOf, clazz);
            }

            return null;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static <V> ValueConverter<V> constructorConverter(Class<V> clazz) {
        try {
            return new ConstructorInvokingValueConverter<V>(clazz.getConstructor(String.class));
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    /**
     * Invokes the given constructor with the given arguments.
     *
     * @param <T>         constraint on the type of the objects yielded by the constructor
     * @param constructor constructor to invoke
     * @param args        arguments to hand to the constructor
     * @return the result of invoking the constructor
     * @throws ReflectionException in lieu of the gaggle of reflection-related exceptions
     */
    public static <T> T instantiate(Constructor<T> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (Exception ex) {
            throw reflectionException(ex);
        }
    }

    /**
     * Invokes the given static method with the given arguments.
     *
     * @param method method to invoke
     * @param args   arguments to hand to the method
     * @return the result of invoking the method
     * @throws ReflectionException in lieu of the gaggle of reflection-related exceptions
     */
    public static Object invoke(Method method, Object... args) {
        try {
            return method.invoke(null, args);
        } catch (Exception ex) {
            throw reflectionException(ex);
        }
    }

    private static boolean meetsConverterRequirements(Method method, Class<?> expectedReturnType) {
        int modifiers = method.getModifiers();
        return isPublic(modifiers) && isStatic(modifiers) && expectedReturnType.equals(method.getReturnType());
    }

    private static RuntimeException reflectionException(Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            return new ReflectionException(ex);
        }
        if (ex instanceof InvocationTargetException) {
            return new ReflectionException(ex.getCause());
        }
        if (ex instanceof RuntimeException) {
            return (RuntimeException) ex;
        }

        return new ReflectionException(ex);
    }
}
