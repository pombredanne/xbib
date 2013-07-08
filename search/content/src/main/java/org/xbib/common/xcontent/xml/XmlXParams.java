/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.xbib.common.xcontent.xml;

import org.xbib.xml.XMLNamespaceContext;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

/**
 * XML parameters for XML XContent
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class XmlXParams {

    private final static QName getDefaultRoot() {
        return new QName("http://elasticsearch.org/namespaces/", "root", "");
    }

    private final static XmlXParams DEFAULT_PARAMS = new XmlXParams(getDefaultRoot(), XMLNamespaceContext.getInstance());

    private QName root;

    private XMLNamespaceContext namespaceContext;

    public XmlXParams(QName root) {
        this(root, XMLNamespaceContext.getInstance());
    }

    public XmlXParams(QName root, XMLNamespaceContext namespaceContext) {
        this.root = root;
        this.namespaceContext = namespaceContext;
    }

    public QName getQName() {
        return root;
    }

    public XMLNamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    public static XmlXParams getDefaultParams() {
        return DEFAULT_PARAMS;
    }
}
