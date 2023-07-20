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

import org.javarosa.core.util.MultiInputStream;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.PrototypeFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MultiMessagePayload implements IDataPayload {
   List<IDataPayload> payloads = new ArrayList<>(1);

    /**
     * Note: Only useful for serialization.
     */
    public MultiMessagePayload() {
        //ONLY FOR SERIALIZATION
    }

    public void addPayload(IDataPayload payload) {
        payloads.add(payload);
    }

    public InputStream getPayloadStream() throws IOException {
        MultiInputStream bigStream = new MultiInputStream();
      for (IDataPayload payload : payloads) {
         bigStream.addStream(payload.getPayloadStream());
      }
        bigStream.prepare();
        return bigStream;
    }

    public void readExternal(DataInputStream in, PrototypeFactory pf)
            throws IOException, DeserializationException {
        payloads = (List)ExtUtil.read(in, new ExtWrapListPoly(), pf);
    }

    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.write(out, new ExtWrapListPoly(payloads));
    }

    public <T> T accept(IDataPayloadVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public String getPayloadId() {
        return null;
    }

    public int getPayloadType() {
        return IDataPayload.PAYLOAD_TYPE_MULTI;
    }

    public int getTransportId() {
        return -1;
    }

    public long getLength() {
        int len = 0;
      for (IDataPayload payload : payloads) {
         len += payload.getLength();
      }
        return len;
    }
}

