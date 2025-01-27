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

package org.javarosa.xpath;

public class XPathException extends RuntimeException {

    private static final long serialVersionUID = 1204382841959734320L;

    //A reference to the "Source" of this message helpful
    //for tracking down where the invalid xpath was declared
    private String sourceRef;

    public XPathException (String message) {
        super("XPath evaluation: " + message);
    }

    public XPathException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public void setSource(String source) {
        sourceRef = source;
    }

    public String getSource() {
        return sourceRef;
    }

    public String getMessage() {
        if(sourceRef == null) {
            return super.getMessage();
        } else {
            return "The problem was located in " + sourceRef + "\n" + super.getMessage();
        }
    }
}
