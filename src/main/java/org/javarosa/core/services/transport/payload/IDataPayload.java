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

package org.javarosa.core.services.transport.payload;

import org.javarosa.core.util.externalizable.Externalizable;

import java.io.IOException;
import java.io.InputStream;

/**
 * IDataPayload is an interface that specifies a piece of data
 * that will be transmitted over the wire to
 *
 * @author Clayton Sims
 * @date Dec 18, 2008
 *
 */
public interface IDataPayload extends Externalizable {

    /**
     * Data payload codes
     */
    int PAYLOAD_TYPE_TEXT = 0;
    int PAYLOAD_TYPE_XML = 1;
    int PAYLOAD_TYPE_JPG = 2;
    int PAYLOAD_TYPE_HEADER = 3;
    int PAYLOAD_TYPE_MULTI = 4;
    int PAYLOAD_TYPE_SMS = 5; // sms's are a variant of TEXT having a limit on length.

    /**
     * Gets the stream for this payload.
     *
     * @return A stream for the data in this payload.
     * @throws IOException
     */
    InputStream getPayloadStream() throws IOException;

    /**
     * @return A string identifying the contents of the payload
     */
    String getPayloadId();

    /**
     * @return The type of the data encapsulated by this
     * payload.
     */
    int getPayloadType();

    /**
     * Visitor pattern accept method.
     * @param visitor The visitor to visit this payload.
     */
    <T> T accept(IDataPayloadVisitor<T> visitor);

    long getLength();

    /**
     * @return A unique Id for the transport manager to use to be able to identify
     * the status of transmissions related to this payload
     */
    int getTransportId();
}
