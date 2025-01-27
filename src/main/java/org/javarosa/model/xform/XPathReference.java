/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 *
 */
package org.javarosa.model.xform;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.XPathException;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.javarosa.core.util.externalizable.ExtUtil.nullIfEmpty;
import static org.javarosa.core.util.externalizable.ExtUtil.read;
import static org.javarosa.core.util.externalizable.ExtUtil.write;
import static org.javarosa.core.util.externalizable.ExtUtil.writeString;

public class XPathReference implements IDataReference {
    private static final Logger logger = LoggerFactory.getLogger(XPathReference.class);

    private TreeReference ref;
    private String nodeset;

    public XPathReference () {

    }

    public XPathReference (String nodeset) {
        ref = getPathExpr(nodeset).getReference();
        this.nodeset = nodeset;
    }

    public static XPathPathExpr getPathExpr (String nodeset) {
        XPathExpression path;
        boolean validNonPathExpr = false;

        try {
        path = XPathParseTool.parseXPath(nodeset);
        if (!(path instanceof XPathPathExpr)) {
            validNonPathExpr = true;
            throw new XPathSyntaxException();
        }

        } catch (XPathSyntaxException xse) {
            //make these checked exceptions?
            if (validNonPathExpr) {
                throw new XPathTypeMismatchException("Expected XPath path, got XPath expression: [" + nodeset + "]," + xse.getMessage());
            } else {
                logger.error("Error", xse);
                throw new XPathException("Parse error in XPath path: [" + nodeset + "]." + (xse.getMessage() == null ? "" : "\n" + xse.getMessage()));
            }
        }

        return (XPathPathExpr)path;
    }

    public XPathReference (XPathPathExpr path) {
        ref = path.getReference();
    }

    public XPathReference (TreeReference ref) {
        this.ref = ref;
    }

    public Object getReference () {
        return ref;
    }

    public void setReference (Object o) {
        //do nothing
    }

    public boolean equals (Object o) {
        if (o instanceof XPathReference) {
            return ref.equals(((XPathReference)o).ref);
        } else {
            return false;
        }
    }

    public int hashCode () {
        return ref.hashCode();
    }

    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        nodeset = nullIfEmpty(ExtUtil.readString(in));
        ref = (TreeReference) read(in, TreeReference.class, pf);
    }

    public void writeExternal(DataOutputStream out) throws IOException {
        writeString(out, ExtUtil.emptyIfNull(nodeset));
        write(out, ref);
    }
}
