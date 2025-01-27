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

package org.javarosa.core.services.locale;

import org.javarosa.core.util.OrderedMap;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author Clayton Sims
 * @date Jun 1, 2009
 *
 */
public class ResourceFileDataSource implements LocaleDataSource {
    private static final Logger logger = LoggerFactory.getLogger(ResourceFileDataSource.class);

    String resourceURI;

    /**
     * NOTE: FOR SERIALIZATION ONLY!
     */
    public ResourceFileDataSource() {

    }

    /**
     * Creates a new Data Source for Locale data with the given resource URI.
     *
     * @param resourceURI a URI to the resource file from which data should be loaded
     * @throws NullPointerException if resourceURI is null
     */
    public ResourceFileDataSource(String resourceURI) {
        if(resourceURI == null) {
            throw new NullPointerException("Resource URI cannot be null when creating a Resource File Data Source");
        }
        this.resourceURI = resourceURI;
    }

    /* (non-Javadoc)
     * @see org.javarosa.core.services.locale.LocaleDataSource#getLocalizedText()
     */
    public OrderedMap<String,String> getLocalizedText() {
        return loadLocaleResource(resourceURI);
    }

    /* (non-Javadoc)
     * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
     */
    public void readExternal(DataInputStream in, PrototypeFactory pf)
            throws IOException, DeserializationException {
        resourceURI = in.readUTF();
    }

    /* (non-Javadoc)
     * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
     */
    public void writeExternal(DataOutputStream out) throws IOException {
        out.writeUTF(resourceURI);
    }

    /**
     * @param resourceName A path to a resource file provided in the current environment
     *
     * @return a dictionary of key/value locale pairs from a file in the resource directory
     */
    private OrderedMap<String,String> loadLocaleResource(String resourceName) {
        InputStream is = System.class.getResourceAsStream(resourceName);
        // TODO: This might very well fail. Best way to handle?
        OrderedMap<String,String> locale = new OrderedMap<>();
        int chunk = 100;
        InputStreamReader isr;
        try {
            isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load locale resource " + resourceName + ". Is it in the jar?");
        }
        boolean done = false;
        char[] cbuf = new char[chunk];
        int offset = 0;
        int curline = 0;

        try {
            String line = "";
            while (!done) {
                int read = isr.read(cbuf, offset, chunk - offset);
                if(read == -1) {
                    done = true;
                    if(line.length() != 0) {
                        parseAndAdd(locale, line, curline);
                    }
                    break;
                }
                String stringchunk = String.valueOf(cbuf,offset,read);

                int index = 0;

                while(index != -1) {
                    int nindex = stringchunk.indexOf('\n',index);
                    //UTF-8 often doesn't encode with newline, but with CR, so if we
                    //didn't find one, we'll try that
                    if(nindex == -1) { nindex = stringchunk.indexOf('\r',index); }
                    if(nindex == -1) {
                        line += stringchunk.substring(index);
                        break;
                    }
                    else {
                        line += stringchunk.substring(index,nindex);
                        //Newline. process our string and start the next one.
                        curline++;
                        parseAndAdd(locale, line, curline);
                        line = "";
                    }
                    index = nindex + 1;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("Error", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                logger.info("Input Stream for resource file {} failed to close. This will eat up " +
                    "your memory! Fix Problem! [{}]", resourceURI, e.getMessage());
                logger.error("Error", e);
            }
        }
        return locale;
    }

    private void parseAndAdd(OrderedMap<String,String> locale, String line, int curline) {
        line = line.trim();

        //clear comments
        while(line.contains("#")) {
            line = line.substring(0, line.indexOf("#"));
        }

        if (line.indexOf('=') != -1) {
            //Check to see if there's anything after the '=' first. Otherwise there
            //might be some big problems.
            if(line.indexOf('=') != line.length()-1) {
                String value = line.substring(line.indexOf('=') + 1);
                locale.put(line.substring(0, line.indexOf('=')), value);
            }
             else {
                logger.info("Invalid line (#{}) read: '{}'. No value follows the '='.", curline, line);
            }
        }
    }

}
