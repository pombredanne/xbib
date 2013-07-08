/*
 *  Copyright 2011 BigData Mx
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.xbib.map;

public interface AnyObject {

    AnyObject getAnyObject(String key);

    Double getDouble(String key);

    Float getFloat(String key);

    Long getLong(String key);

    Integer getInteger(String key);

    String getString(String key);

    Boolean getBoolean(String key);

    <T> Iterable<T> getIterable(String key);

    Double getDouble(String key, Double defValue);

    Float getFloat(String key, Float defValue);

    Long getLong(String key, Long defValue);

    Integer getInteger(String key, Integer defValue);

    String getString(String key, String defValue);

    Boolean getBoolean(String key, Boolean defValue);

    Iterable<AnyTuple> getTuples();

}