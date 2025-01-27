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

package org.javarosa.core.util.externalizable;

/**
 * Thrown when trying to create an object during serialization, but object cannot be created because:
 * 
 * 1) We don't know what object to create
 *  
 * @author Clayton Sims
 *
 */
public class DeserializationException extends Exception {
    private static final long serialVersionUID = 8431704386147851563L;

    //TODO -don't bury the underlying exception
    public DeserializationException(String message) {
        super(message);
    }
    public DeserializationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
